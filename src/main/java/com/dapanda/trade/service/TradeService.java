package com.dapanda.trade.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.plan.entity.Plan;
import com.dapanda.plan.repository.PlanRepository;
import com.dapanda.product.entity.MobileData;
import com.dapanda.product.entity.Product;
import com.dapanda.product.entity.ProductState;
import com.dapanda.product.repository.MobileDataRepository;
import com.dapanda.product.repository.ProductRepository;
import com.dapanda.trade.dto.MobileDataScrap;
import com.dapanda.trade.dto.request.TradeMobileDataDefaultRequest;
import com.dapanda.trade.dto.response.FindMobileDataScrapResponse;
import com.dapanda.trade.dto.response.TradeMobileDataDefaultResponse;
import com.dapanda.trade.entity.Trade;
import com.dapanda.trade.entity.TradeDetails;
import com.dapanda.trade.entity.TradeType;
import com.dapanda.trade.repository.TradeDetailsRepository;
import com.dapanda.trade.repository.TradeRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeService {

	private static final int MAX_COMBINATION_CANDIDATES = 5;

	private final TradeRepository tradeRepository;
	private final ProductRepository productRepository;
	private final MobileDataRepository mobileDataRepository;
	private final MemberRepository memberRepository;
	private final TradeDetailsRepository tradeDetailsRepository;
	private final PlanRepository planRepository;

	/**
	 * 1. 데이터 일반 상품 구매 요청
	 * 2. 해당 상품 재고 조회
	 * 3. 재고 유효하면 Lock 걸기, 재고 유효하지 않으면 Exception
	 * 4. 캐시 결제 -> 캐시에 Lock, 결제 완료되면 Lock 해제 // 캐시 잔고 부족하면 예외
	 * 5. 해당 상품 SOLD_OUT 처리
	 * 6. Trade, TradeDetails 순서대로 생성
	 * 7. 구매자: 데이터 추가 / 판매자: 데이터 차감 / 공통 필드 갱신
	 * 8. 알림, 로그 -> EventListener(트랜잭션 이후)
	 * 9. Response: 구매 데이터양, 내 총 데이터양
	 */
	@Transactional
	public TradeMobileDataDefaultResponse mobileDataDefault(
			Long buyerId, TradeMobileDataDefaultRequest request) {

		Product product = productRepository.findByIdForUpdate(request.productId())
				.orElseThrow();

		if (product.getState().equals(ProductState.SOLD_OUT)) {
			throw new GlobalException(ResultCode.ALREADY_SOLD_OUT);
		}

		if (product.getMember().getId().equals(buyerId)) {
			throw new GlobalException(ResultCode.CANNOT_PURCHASE_OWN_PRODUCT);
		}

		MobileData mobileData = mobileDataRepository.findById(product.getItemId()).orElseThrow();

		if (mobileData.isSplitType()) {

			int price = (int) (mobileData.getPricePer100MB() * request.dataAmount() * 10);

			return handlePartialPurchaseProduct(product, mobileData, buyerId, request.dataAmount(),
					price);
		}

		return handleFullPurchaseProduct(product, mobileData, buyerId);
	}

	/**
	 * 데이터 통합 상품 일반 구매
	 */
	private TradeMobileDataDefaultResponse handleFullPurchaseProduct(Product product,
			MobileData mobileData, Long buyerId) {

		// Lock 건 상태로 구매자, 판매자 조회
		Member buyer = memberRepository.findByIdForUpdate(buyerId).orElseThrow();
		Member seller = memberRepository.findByIdForUpdate(product.getMember().getId())
				.orElseThrow();

		deductBuyerCashAndUpdateState(buyer, product, mobileData, product.getPrice(),
				mobileData.getDataAmount());

		Trade trade = createTradeAndTradeDetails(product, mobileData, buyer, product.getPrice());

		updateBuyerAndSellerData(buyer, seller, mobileData.getDataAmount());

		return TradeMobileDataDefaultResponse.of(trade.getId());
	}

	/**
	 * 데이터 분할 상품 일반 구매
	 */
	private TradeMobileDataDefaultResponse handlePartialPurchaseProduct(Product product,
			MobileData mobileData, Long buyerId, float dataAmount, int price) {

		// Lock 건 상태로 구매자, 판매자 조회
		Member buyer = memberRepository.findByIdForUpdate(buyerId).orElseThrow();
		Member seller = memberRepository.findByIdForUpdate(product.getMember().getId())
				.orElseThrow();

		deductBuyerCashAndUpdateState(buyer, product, mobileData, price, dataAmount);

		Trade trade = createTradeAndTradeDetails(product, mobileData, buyer, product.getPrice());

		updateBuyerAndSellerData(buyer, seller, dataAmount);

		return TradeMobileDataDefaultResponse.of(trade.getId());
	}

	private void deductBuyerCashAndUpdateState(Member buyer, Product product, MobileData mobileData,
			int price, float dataAmount) {

		if (mobileData.getRemainAmount() < dataAmount) {
			throw new GlobalException(ResultCode.INVALID_REMAIN_DATA_AMOUNT);
		}
		if (buyer.getCash() < price) {
			throw new GlobalException(ResultCode.INSUFFICIENT_CASH);
		}

		buyer.deductCash(price);
		buyer.addBuyingData(dataAmount);
		mobileData.deductRemainAmount(dataAmount);
		if (mobileData.getRemainAmount() == 0) {
			product.changeState(ProductState.SOLD_OUT);
		}
	}

	private Trade createTradeAndTradeDetails(Product product, MobileData mobileData, Member buyer,
			int price) {

		Trade trade = Trade.of(mobileData.getDataAmount(), null, price, TradeType.PURCHASE_SINGLE,
				buyer);
		tradeRepository.save(trade);
		TradeDetails tradeDetails = TradeDetails.of(product, trade);
		tradeDetailsRepository.save(tradeDetails);

		return trade;
	}

	private void updateBuyerAndSellerData(Member buyer, Member seller, float dataAmount) {

		seller.addSellingData(dataAmount);

		Plan sellerPlan = planRepository.findByMember(seller).orElseThrow();
		Plan buyerPlan = planRepository.findByMember(buyer).orElseThrow();

		buyerPlan.addMobileData(dataAmount);
		sellerPlan.deductMobileData(dataAmount);
	}

	public FindMobileDataScrapResponse findMobileDataScrap(Float dataAmount) {

		// 1. 정렬된 상품 목록 조회 (단가 낮은순, 용량 많은순, 일반우선)
		List<MobileDataScrap> sortedList = productRepository.findMobileDataScrap(dataAmount);

		// 2. 가능한 조합들을 저장할 리스트
		List<List<MobileDataScrap>> candidates = new ArrayList<>(); // 가능한 조합들을 저장하는 리스트
		int sortedListSize = sortedList.size();
		float target = dataAmount; // 사용자가 구매하고자 하는 목표 데이터 용량 (GB 단위)

		// 3. 시작 인덱스를 0부터 n-1까지 순차적으로 이동하며 탐색
		for (int start = 0; start < sortedListSize; start++) {
			float sumAmount = 0; // 현재 조합에 포함된 상품들로 누적한 총 데이터 용량 (GB 단위)
			int sumPrice = 0;
			List<MobileDataScrap> temp = new ArrayList<>(); // 현재 조합 중인 상품 목록을 저장하는 임시 리스트

			// 4. 현재 start 위치부터 하나씩 상품을 누적하며 조합을 시도
			for (int i = start; i < sortedListSize; i++) {
				MobileDataScrap item = sortedList.get(i);
				float amount = item.getRemainAmount();

				// 4-1. 분할 가능한 상품이고, 목표 용량을 초과한다면 필요한 만큼만 구매
				if (item.isSplitType() && sumAmount + amount > target) {
					float needed = target - sumAmount;
					sumAmount += needed;
					sumPrice += (int) (needed * 10 * item.getPricePer100MB());
					temp.add(item);
				} else { // 4-2. 일반 상품이거나, 전체를 써도 용량 초과하지 않는 경우 전부 사용
					sumAmount += amount;
					sumPrice += item.getPrice();
					temp.add(item);
				}

				if (sumAmount == target) { // 5. 목표 용량을 정확히 채운 조합은 후보군에 추가
					candidates.add(temp);
					break;
				}
				if (sumAmount > target) { // 6. 목표 용량을 초과하면 더 이상 탐색하지 않음
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

			return FindMobileDataScrapResponse.of(0, 0, Collections.emptyList());
		}

		// 10. 최적 조합이 존재하면 최종 응답 생성
		List<MobileDataScrap> bestCombination = best.get();
		int totalPrice = calculateTotalPrice(bestCombination, dataAmount);

		return FindMobileDataScrapResponse.of(dataAmount, totalPrice, bestCombination);
	}

	private int calculateTotalPrice(List<MobileDataScrap> scrapList, float dataAmount) {

		float sumAmount = 0;
		int total = 0;

		// 조합된 상품 리스트를 순회하며, 실제로 필요한 만큼만 구매하고 총 가격 계산
		for (MobileDataScrap scrap : scrapList) {
			// 분할 상품의 경우: 필요한 만큼만 구매 (단가 적용)
			if (scrap.isSplitType()) {
				float needed = Math.min(scrap.getRemainAmount(), dataAmount - sumAmount);
				total += (int) (needed * 10 * scrap.getPricePer100MB());
				sumAmount += needed;
			} else { // 일반 상품의 경우: 상품 전체를 사용하며 고정 가격 적용
				total += scrap.getPrice();
				sumAmount += scrap.getRemainAmount();
			}

			// 목표 용량을 채웠으면 반복 종료
			if (sumAmount >= dataAmount) {
				break;
			}
		}

		return total;
	}
}
