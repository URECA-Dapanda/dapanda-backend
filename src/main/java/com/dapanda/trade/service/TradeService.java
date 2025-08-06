package com.dapanda.trade.service;

import com.dapanda.alarm.scheduler.WifiTradeNotificationScheduler;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.fcmToken.service.FcmTokenService;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.plan.entity.Plan;
import com.dapanda.plan.repository.PlanRepository;
import com.dapanda.product.entity.*;
import com.dapanda.product.repository.*;
import com.dapanda.trade.dto.*;
import com.dapanda.trade.dto.request.*;
import com.dapanda.trade.dto.response.*;
import com.dapanda.trade.entity.*;
import com.dapanda.trade.repository.TradeDetailsRepository;
import com.dapanda.trade.repository.TradeRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeService {

	private static final int MAX_COMBINATION_CANDIDATES = 5;

	private final TradeRepository tradeRepository;
	private final ProductRepository productRepository;
	private final MobileDataRepository mobileDataRepository;
	private final WifiRepository wifiRepository;
	private final MemberRepository memberRepository;
	private final TradeDetailsRepository tradeDetailsRepository;
	private final PlanRepository planRepository;
	private final FcmTokenService fcmTokenService;
	private final WifiTradeNotificationScheduler wifiTradeNotificationScheduler;

	/**
	 * 1. 데이터 일반 상품 구매 요청 2. 해당 상품 재고 조회 3. 재고 유효하면 Lock 걸기, 재고 유효하지 않으면 Exception 4. 캐시 결제 -> 캐시에
	 * Lock, 결제 완료되면 Lock 해제 // 캐시 잔고 부족하면 예외 5. 해당 상품 SOLD_OUT 처리 6. Trade, TradeDetails 순서대로 생성 7.
	 * 구매자: 데이터 추가 / 판매자: 데이터 차감 / 공통 필드(캐시) 갱신 8. 알림, 로그 -> EventListener(트랜잭션 이후) 9. Response: 구매
	 * 데이터양, 내 총 데이터양
	 */
	@Transactional
	@CacheEvict(
			value = "mobileDataByCursor",
			allEntries = true
	)
	public TradeProductResponse defaultPurchaseMobileData(Long buyerId,
			DefaultPurchaseMobileDataRequest request) {

		validateProduct(request.productId(), buyerId);

		Product product = productRepository.findById(request.productId()).orElseThrow();
		MobileData mobileData = mobileDataRepository.findById(product.getItemId()).orElseThrow();

		if (mobileData.isSplitType()) {

			int price = BigDecimal.valueOf(mobileData.getPricePer100MB())
					.multiply(request.dataAmount())
					.multiply(BigDecimal.TEN)
					.setScale(0, RoundingMode.CEILING)
					.intValue();

			return handlePartialPurchaseProduct(product, mobileData, buyerId, request.dataAmount(),
					price);
		}

		return handleFullPurchaseProduct(product, mobileData, buyerId);
	}

	/**
	 * 데이터 통합 상품 일반 구매
	 */
	private TradeProductResponse handleFullPurchaseProduct(Product product,
			MobileData mobileData, Long buyerId) {

		validateMobileDataLimit(buyerId, mobileData.getDataAmount());

		updateBuyerCashAndDataAmount(buyerId, product.getPrice(),
				mobileData.getDataAmount());
		updateSellerCashAndDataAmount(product.getMember().getId(), product.getPrice(),
				mobileData.getDataAmount());
		updateMobileDataProduct(mobileData, product, product.getPrice(),
				mobileData.getDataAmount());

		Trade trade = createMobileDataTradeAndTradeDetails(product, mobileData.getDataAmount(),
				buyerId, product.getMember().getId(), product.getPrice());

		return TradeProductResponse.of(trade.getId());
	}

	/**
	 * 데이터 분할 상품 일반 구매
	 */
	private TradeProductResponse handlePartialPurchaseProduct(Product product,
			MobileData mobileData, Long buyerId, BigDecimal dataAmount, int price) {

		validateMobileDataLimit(buyerId, dataAmount);

		updateBuyerCashAndDataAmount(buyerId, price, dataAmount);
		updateSellerCashAndDataAmount(product.getMember().getId(), price, dataAmount);
		updateMobileDataProduct(mobileData, product, price, dataAmount);

		Trade trade = createMobileDataTradeAndTradeDetails(product, dataAmount, buyerId,
				product.getMember().getId(), price);

		return TradeProductResponse.of(trade.getId());
	}

	@Cacheable(
			value = "mobileDataScrap",
			key = "'dataAmount=' + #dataAmount"
	)
	public FindMobileDataScrapResponse findMobileDataScrap(BigDecimal dataAmount, Long memberId) {

		// 1. 정렬된 상품 목록 조회 (단가 낮은순, 용량 많은순, 일반우선)
		List<MobileDataScrap> sortedList = productRepository.findMobileDataScrap(dataAmount,
				memberId);

		// 2. 가능한 조합들을 저장할 리스트
		List<List<MobileDataScrap>> candidates = new ArrayList<>(); // 가능한 조합들을 저장하는 리스트
		int sortedListSize = sortedList.size();
		BigDecimal target = dataAmount; // 사용자가 구매하고자 하는 목표 데이터 용량 (GB 단위)

		// 3. 시작 인덱스를 0부터 n-1까지 순차적으로 이동하며 탐색
		for (int start = 0; start < sortedListSize; start++) {
			BigDecimal sumAmount = BigDecimal.ZERO; // 현재 조합에 포함된 상품들로 누적한 총 데이터 용량 (GB 단위)
			int sumPrice = 0;
			List<MobileDataScrap> temp = new ArrayList<>(); // 현재 조합 중인 상품 목록을 저장하는 임시 리스트

			// 4. 현재 start 위치부터 하나씩 상품을 누적하며 조합을 시도
			for (int i = start; i < sortedListSize; i++) {
				MobileDataScrap item = sortedList.get(i);
				BigDecimal amount = item.getRemainAmount();

				// 4-1. 분할 가능한 상품이고, 목표 용량을 초과한다면 필요한 만큼만 구매
				if (item.isSplitType()) {
					BigDecimal needed = amount.min(target.subtract(sumAmount));

					// 필요한 만큼만 구매한 정보로 새 객체 생성
					MobileDataScrap partialScrap = new MobileDataScrap(
							item.getProductId(),
							item.getMobileDataId(),
							item.getMemberName(),
							item.getProfileImageUrl(),
							item.getPrice(),
							(int) (needed.doubleValue() * 10 * item.getPricePer100MB()),
							// purchasePrice
							item.getRemainAmount(),
							needed, // purchaseAmount
							item.getPricePer100MB(),
							true,
							item.getUpdatedAt()
					);

					sumAmount = sumAmount.add(needed);
					sumPrice += (int) (needed.doubleValue() * 10 * item.getPricePer100MB());
					temp.add(partialScrap);
				} else {
					MobileDataScrap fullScrap = new MobileDataScrap(
							item.getProductId(),
							item.getMobileDataId(),
							item.getMemberName(),
							item.getProfileImageUrl(),
							item.getPrice(),
							item.getPrice(), // purchasePrice
							item.getRemainAmount(),
							item.getRemainAmount(), // purchaseAmount
							item.getPricePer100MB(),
							false,
							item.getUpdatedAt()
					);

					sumAmount = sumAmount.add(amount);
					sumPrice += item.getPrice();
					temp.add(fullScrap);
				}

				if (sumAmount.compareTo(target) == 0) { // 5. 목표 용량을 정확히 채운 조합은 후보군에 추가
					candidates.add(temp);
					break;
				}
				if (sumAmount.compareTo(target) > 0) { // 6. 목표 용량을 초과하면 더 이상 탐색하지 않음
					break;
				}
			}

			// 7. 후보군이 5개 이상이면 더 이상 탐색하지 않음 (성능 최적화 목적)
			if (candidates.size() >= MAX_COMBINATION_CANDIDATES) {
				break;
			}
		}

		// 8. 후보 중에서 실제 가격이 가장 저렴한 조합을 선택
		Optional<List<MobileDataScrap>> best = candidates.stream()
				.min(Comparator.comparingInt(
						scrapList -> calculateTotalPrice(scrapList, dataAmount)));

		// 9. 후보가 없으면 빈 응답 반환
		if (best.isEmpty()) {

			return FindMobileDataScrapResponse.of(BigDecimal.ZERO, 0, Collections.emptyList());
		}

		// 10. 최적 조합이 존재하면 최종 응답 생성
		List<MobileDataScrap> bestCombination = best.get();
		int totalPrice = calculateTotalPrice(bestCombination, dataAmount);

		return FindMobileDataScrapResponse.of(dataAmount, totalPrice, bestCombination);
	}

	/**
	 * 데이터 상품 자투리 구매
	 */
	@Transactional
	@CacheEvict(
			value = "mobileDataByCursor",
			allEntries = true
	)
	public TradeProductResponse scrapPurchaseMobileData(Long buyerId,
			ScrapPurchaseMobileDataRequest request) {

		BigDecimal totalAmount = request.totalAmount();
		int totalPrice = request.totalPrice();

		// 1. 유효성 검사
		validateMobileDataLimit(buyerId, request.totalAmount());

		// 2. 구매자 캐시, 데이터양 업데이트
		updateBuyerCashAndDataAmount(buyerId, totalPrice, totalAmount);

		// 3. 거래 생성
		Member buyer = memberRepository.findById(buyerId).orElseThrow();

		Trade buyerTrade = Trade.of(totalAmount, totalPrice, TradeType.PURCHASE_MOBILE_COMPOSITE,
				buyer);
		tradeRepository.save(buyerTrade);

		// 4. 각 상품 조합 순회
		for (MobileDataScrap scrap : request.combinations()) {
			// 4-1. 상품, 데이터 정보 조회 및 Lock
			Product product = productRepository.findByIdForUpdate(scrap.getProductId())
					.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));

			MobileData mobileData = mobileDataRepository.findByIdForUpdate(scrap.getMobileDataId())
					.orElseThrow(() -> new GlobalException(ResultCode.MOBILE_DATA_NOT_FOUND));

			Member seller = memberRepository.findById(product.getMember().getId())
					.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

			// 4-2. 상품 가격, 데이터양
			BigDecimal purchaseAmount = scrap.getPurchaseAmount();
			int purchasePrice = scrap.getPurchasePrice();

			// 4-3. 상품 업데이트
			updateMobileDataProduct(mobileData, product, purchasePrice, purchaseAmount);

			// 4-4. 판매자 캐시, 데이터양 업데이트
			updateSellerCashAndDataAmount(product.getMember().getId(), purchasePrice,
					purchaseAmount);

			// 4-5. 거래 저장
			Trade sellerTrade = Trade.of(purchaseAmount, null, purchasePrice,
					TradeType.SALE_MOBILE_DATA, seller);
			tradeRepository.save(sellerTrade);

			TradeDetails buyerTradeDetails = TradeDetails.of(product, buyerTrade);
			TradeDetails sellerTradeDetails = TradeDetails.of(product, sellerTrade);
			tradeDetailsRepository.saveAll(List.of(buyerTradeDetails, sellerTradeDetails));
		}

		return TradeProductResponse.of(buyerTrade.getId());
	}

	/**
	 * 와이파이 상품 구매
	 */
	@Transactional
	@CacheEvict(
			value = "wifiByCursor",
			allEntries = true
	)
	public TradeProductResponse purchaseWifi(Long buyerId, PurchaseWifiRequest request) {

		// 1. 상품 조회
		Product product = productRepository.findByIdForUpdate(request.productId())
				.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));

		// 2. 와이파이 정보 조회
		Wifi wifi = wifiRepository.findById(request.wifiId())
				.orElseThrow(() -> new GlobalException(ResultCode.WIFI_NOT_FOUND));

		// 3. 유효성 검사
		LocalTime requestStart = request.startTime().toLocalTime();
		LocalTime requestEnd = request.endTime().toLocalTime();
		LocalTime wifiStart = wifi.getStartTime().toLocalTime();
		LocalTime wifiEnd = wifi.getEndTime().toLocalTime();

		int timeAmount = (int) Duration.between(requestStart, requestEnd).toMinutes();

		if (timeAmount < 0) {
			timeAmount += 24 * 60;
		}

		int totalPrice = product.getPrice() * timeAmount / 10;

		validateProduct(product.getId(), buyerId);
		validateWifiTime(requestStart, requestEnd, wifiStart, wifiEnd);

		// 4. 판매자/구매자 캐시 업데이트
		updateBuyerAndSellerCash(buyerId, product.getMember().getId(), totalPrice);

		// 5. 거래 저장
		Trade buyerTrade = createWifiTradeAndTradeDetails(product, buyerId,
				product.getMember().getId(), totalPrice, timeAmount);

		// 6. 알림 저장
		wifiTradeNotificationScheduler.scheduleNotification(buyerTrade.getId(),
				buyerId, requestStart, requestEnd);

		// 7. 응답 반환
		return TradeProductResponse.of(buyerTrade.getId());
	}

	public FindTradeHistoryResponse findTradeHistory(Long cursorId, Integer size,
			Long memberId) {

		Long tradeCount = tradeRepository.countTradeHistoryByMemberId(memberId);

		CursorPageResponse<PurchaseHistorySummary> purchaseHistory = tradeRepository.findTradeHistoryByCursor(
				cursorId, size, memberId);

		return FindTradeHistoryResponse.of(tradeCount, purchaseHistory);
	}

	public FindCashHistoryResponse findCashHistory(Long cursorId, Integer size,
			Long memberId, int year, int month) {

		CursorPageResponse<CashHistorySummary> cashHistorySummary = tradeRepository.findCashHistoryByCursor(
				cursorId, size, memberId, year, month);

		CashHistoryMonthlySummary monthlySummary = tradeRepository.calculateMonthlySummary(memberId,
				year, month);

		return FindCashHistoryResponse.of(monthlySummary, cashHistorySummary);
	}

	private void validateProduct(Long productId, Long buyerId) {

		Product product = productRepository.findByIdForUpdate(productId).orElseThrow();

		if (product.getState().equals(ProductState.SOLD_OUT)) {
			throw new GlobalException(ResultCode.ALREADY_SOLD_OUT);
		}

		if (product.getMember().getId().equals(buyerId)) {
			throw new GlobalException(ResultCode.CANNOT_PURCHASE_OWN_PRODUCT);
		}
	}

	private void validateMobileDataLimit(Long buyerId, BigDecimal dataAmount) {

		Member buyer = memberRepository.findById(buyerId).orElseThrow();

		if (buyer.getBuyingData().add(dataAmount).compareTo(
				BigDecimal.valueOf(MobileData.MAX_TRANSFERABLE_DATA_AMOUNT)) > 0) {
			throw new GlobalException(ResultCode.EXCEEDED_PURCHASE_LIMIT);
		}
	}

	private void validateWifiTime(LocalTime requestStart, LocalTime requestEnd,
			LocalTime wifiStart, LocalTime wifiEnd) {

		boolean isOverMidnight = wifiStart.isAfter(wifiEnd); // 영업 시간이 자정을 넘기는지 여부

		boolean isStartValid;
		boolean isEndValid;

		if (isOverMidnight) {
			isStartValid = !requestStart.isBefore(wifiStart) || !requestStart.isAfter(wifiEnd);
			isEndValid = !requestEnd.isBefore(wifiStart) || !requestEnd.isAfter(wifiEnd);
		} else {
			isStartValid = !requestStart.isBefore(wifiStart) && !requestStart.isAfter(wifiEnd);
			isEndValid = !requestEnd.isBefore(wifiStart) && !requestEnd.isAfter(wifiEnd);
		}
		if (!isStartValid || !isEndValid) {
			throw new GlobalException(ResultCode.INVALID_WIFI_OPERATION_TIME);
		}
	}

	private void updateBuyerCashAndDataAmount(Long buyerId, int price, BigDecimal dataAmount) {

		Member buyer = memberRepository.findByIdForUpdate(buyerId).orElseThrow();

		if (buyer.getCash() < price) {
			throw new GlobalException(ResultCode.INSUFFICIENT_CASH);
		}

		Plan buyerPlan = planRepository.findByMember(buyer).orElseThrow();

		buyer.deductCash(price);
		buyer.addBuyingData(dataAmount);
		buyerPlan.addMobileData(dataAmount);
	}

	private void updateSellerCashAndDataAmount(Long sellerId, int price, BigDecimal dataAmount) {

		Member seller = memberRepository.findByIdForUpdate(sellerId).orElseThrow();

		Plan sellerPlan = planRepository.findByMember(seller).orElseThrow();

		seller.addCash(price);
		seller.addSellingData(dataAmount);
		sellerPlan.deductMobileData(dataAmount);
	}

	private void updateMobileDataProduct(MobileData mobileData, Product product, int price,
			BigDecimal dataAmount) {

		if (mobileData.getRemainAmount().compareTo(dataAmount) < 0) {
			throw new GlobalException(ResultCode.INVALID_REMAIN_DATA_AMOUNT);
		}

		mobileData.deductRemainAmount(dataAmount);

		if (mobileData.getRemainAmount().compareTo(BigDecimal.ZERO) == 0) {
			product.changeState(ProductState.SOLD_OUT);

			fcmTokenService.notifyProductSold(
					product.getMember().getId(),
					product.getCreatedAt(),
					product.getItemType()
			);

		} else {
			product.updatePrice(product.getPrice() - price);
			mobileData.update100MBPerPrice(product.getPrice(), mobileData.getRemainAmount());
		}
	}

	private void updateBuyerAndSellerCash(Long buyerId, Long sellerId, int totalPrice) {

		Member buyer = memberRepository.findByIdForUpdate(buyerId).orElseThrow();
		Member seller = memberRepository.findByIdForUpdate(sellerId).orElseThrow();

		if (buyer.getCash() < totalPrice) {
			throw new GlobalException(ResultCode.INSUFFICIENT_CASH);
		}

		seller.addCash(totalPrice);
		buyer.deductCash(totalPrice);
	}

	private Trade createMobileDataTradeAndTradeDetails(Product product, BigDecimal dataAmount,
			Long buyerId, Long sellerId, int price) {

		Member buyer = memberRepository.findById(buyerId).orElseThrow();
		Member seller = memberRepository.findById(sellerId).orElseThrow();

		Trade buyerTrade = Trade.of(dataAmount, price, TradeType.PURCHASE_MOBILE_SINGLE, buyer);
		Trade sellerTrade = Trade.of(dataAmount, price, TradeType.SALE_MOBILE_DATA, seller);
		tradeRepository.saveAll(List.of(buyerTrade, sellerTrade));

		TradeDetails buyerTradeDetails = TradeDetails.of(product, buyerTrade);
		TradeDetails sellerTradeDetails = TradeDetails.of(product, sellerTrade);
		tradeDetailsRepository.saveAll(List.of(buyerTradeDetails, sellerTradeDetails));

		return buyerTrade;
	}

	private Trade createWifiTradeAndTradeDetails(Product product, Long buyerId, Long sellerId,
			int price, int timeAmount) {

		Member buyer = memberRepository.findById(buyerId).orElseThrow();
		Member seller = memberRepository.findById(sellerId).orElseThrow();

		Trade buyerTrade = Trade.of(timeAmount, price, TradeType.PURCHASE_WIFI, buyer);
		Trade sellerTrade = Trade.of(timeAmount, price, TradeType.SALE_WIFI, seller);
		tradeRepository.saveAll(List.of(buyerTrade, sellerTrade));

		TradeDetails buyerTradeDetails = TradeDetails.of(product, buyerTrade);
		TradeDetails sellerTradeDetails = TradeDetails.of(product, sellerTrade);
		tradeDetailsRepository.saveAll(List.of(buyerTradeDetails, sellerTradeDetails));

		return buyerTrade;
	}

	private int calculateTotalPrice(List<MobileDataScrap> scrapList, BigDecimal dataAmount) {

		BigDecimal sumAmount = BigDecimal.ZERO;
		int total = 0;

		// 조합된 상품 리스트를 순회하며, 실제로 필요한 만큼만 구매하고 총 가격 계산
		for (MobileDataScrap scrap : scrapList) {
			// 분할 상품의 경우: 필요한 만큼만 구매 (단가 적용)
			if (scrap.isSplitType()) {
				BigDecimal needed = scrap.getRemainAmount().min(dataAmount.subtract(sumAmount));
				total += (int) (needed.doubleValue() * 10 * scrap.getPricePer100MB());
				sumAmount = sumAmount.add(needed);
			} else { // 일반 상품의 경우: 상품 전체를 사용하며 고정 가격 적용
				total += scrap.getPrice();
				sumAmount = sumAmount.add(scrap.getRemainAmount());
			}

			// 목표 용량을 채웠으면 반복 종료
			if (sumAmount.compareTo(dataAmount) >= 0) {
				break;
			}
		}

		return total;
	}
}
