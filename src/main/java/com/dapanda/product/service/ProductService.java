package com.dapanda.product.service;

import com.dapanda.common.dto.response.CountCursorPageResponse;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.common.service.S3Service;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.plan.entity.Plan;
import com.dapanda.plan.repository.PlanRepository;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.request.*;
import com.dapanda.product.dto.response.*;
import com.dapanda.product.entity.*;
import com.dapanda.product.repository.*;
import com.dapanda.trade.repository.TradeRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final MobileDataRepository mobileDataRepository;
	private final WifiRepository wifiRepository;
	private final MemberRepository memberRepository;
	private final S3Service s3Service;
	private final PlanRepository planRepository;
	private final TradeRepository tradeRepository;

	public CountCursorPageResponse<ReadSellingProductResponse> readSellingProduct(
			ReadSellingProductRequest request) {

		validateMemberId(request.memberId());

		List<ReadSellingProductResponse> response = productRepository.findSellingProduct(request);

		Long count = productRepository.countSellingProduct(request);

		boolean hasNext = response.size() > request.size();

		if (hasNext) {
			response = response.subList(0, request.size());
		}

		Long nextCursorId = hasNext && !response.isEmpty()
				? response.get(response.size() - 1).getProductId()
				: null;

		CountCursorPageResponse.PageInfo pageInfo = CountCursorPageResponse.PageInfo.of(
				nextCursorId,
				hasNext,
				request.size()
		);

		return CountCursorPageResponse.of(response, pageInfo, count);
	}

	@Cacheable(
			value = "mobileDataByCursor",
			key = "'cursor=' + #cursorId + ':sort=' + #productSortOption"
	)
	public CursorPageResponse<MobileDataSummary> findMobileDataByCursor(Long cursorId, Integer size,
			String productSortOption, BigDecimal dataAmount) {

		return productRepository.findMobileDataByCursor(cursorId, size,
				ProductSortOption.from(productSortOption), dataAmount);
	}

	@Cacheable(
			cacheNames = "#{'wifiByCursor' + (#productSortOption == 'DISTANCE_ASC' ? 'Geo' : '')}",
			key = "'cursor=' + #cursorId + ':sort=' + #productSortOption + ':open=' + #open",
			condition = "#productSortOption == 'PRICE_ASC' or #productSortOption == 'AVERAGE_RATE_DESC' or #productSortOption == 'DISTANCE_ASC'"
	)
	public CursorPageResponse<WifiSummary> findWifiByCursor(Long cursorId, Integer size,
			String productSortOption, boolean open, Double latitude, Double longitude) {

		return productRepository.findWifiByCursor(cursorId, size,
				ProductSortOption.from(productSortOption), open, latitude, longitude);
	}

	public MobileDataInfoResponse findMobileDataInfo(Long productId, Long memberId) {

		if (!productRepository.existsById(productId)) {
			throw new GlobalException(ResultCode.PRODUCT_NOT_FOUND);
		}

		MobileDataInfoResponse response = productRepository.findMobileDataInfo(productId, memberId);

		if (response == null) {
			throw new GlobalException(ResultCode.INVALID_PRODUCT);
		}

		return response;
	}

	public WifiInfoResponse findWifiInfo(Long productId, Long memberId) {

		if (!productRepository.existsById(productId)) {
			throw new GlobalException(ResultCode.PRODUCT_NOT_FOUND);
		}

		WifiInfoResponse response = productRepository.findWifiInfo(productId, memberId);

		if (response == null) {
			throw new GlobalException(ResultCode.INVALID_PRODUCT);
		}

		List<String> wifiImages = productRepository.findWifiImages(response.getItemId());

		return response.withImageUrls(wifiImages);
	}

	@Transactional
	@CacheEvict(
			value = "mobileDataByCursor",
			allEntries = true
	)
	public void createMobileData(CreateMobileDataRequest request, Long memberId) {

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		BigDecimal soldAmount = productRepository.sumSoldMobileDataAmountByMemberId(member.getId());
		if (soldAmount == null) {
			soldAmount = BigDecimal.ZERO;
		}
		BigDecimal dataAmount = request.getDataAmount();

		Plan plan = planRepository.findByMemberId(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.NOT_FOUND_PLAN));
		if (plan.getCurrentDataAmount().compareTo(dataAmount) < 0) {
			throw new GlobalException(ResultCode.NOT_ENOUGH_DATA);
		}

		BigDecimal willSellAmount = request.getDataAmount();
		if (soldAmount.add(willSellAmount)
				.compareTo(BigDecimal.valueOf(MobileData.MAX_TRANSFERABLE_DATA_AMOUNT)) > 0) {
			throw new GlobalException(ResultCode.EXCEEDED_TRANSFER_LIMIT);
		}

		MobileData savedMobileData = mobileDataRepository.save(
				MobileData.singleOf(
						request.getDataAmount(),
						request.getPrice(),
						request.getIsSplitType()
				)
		);

		Product savedProduct = productRepository.save(
				Product.of(ProductState.ACTIVE, request.getPrice(), savedMobileData.getId(),
						ItemType.MOBILE_DATA,
						member)
		);

		validateProductOwner(savedProduct, memberId);
	}

	@Transactional
	@CacheEvict(
			value = "wifiByCursor",
			allEntries = true
	)
	public void createWifi(CreateWifiRequest request, Long memberId) {

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		Wifi savedWifi = wifiRepository.save(
				Wifi.of(
						request.getTitle(),
						request.getContent(),
						request.getLatitude(),
						request.getLongitude(),
						request.getAddress(),
						request.getStartTime(),
						request.getEndTime()
				)
		);

		Product savedProduct = productRepository.save(
				Product.of(
						ProductState.ACTIVE,
						request.getPrice(),
						savedWifi.getId(),
						ItemType.WIFI,
						member
				)
		);

		List<String> images = request.getImages();
		if (images != null && !images.isEmpty()) {
			int idx = 0;
			for (String imgUrl : images) {
				// 확장자 체크 (jpg, jpeg, png만 허용)
				if (s3Service.isNotValidImageExtension(imgUrl)) {
					throw new GlobalException(ResultCode.INVALID_IMAGE_FORMAT);
				}
				ProductImage productImage = ProductImage.of(
						imgUrl,
						idx++, // 리스트 순서가 priority
						savedWifi.getId()
				);
				productImageRepository.save(productImage);
			}
		}

		validateProductOwner(savedProduct, memberId);
	}


	@Transactional
	@CacheEvict(
			value = "mobileDataByCursor",
			allEntries = true
	)
	public UpdateMobileDataResponse updateMobileData(UpdateMobileDataRequest request,
			Long memberId) {

		Product savedProduct = productRepository.findById(request.productId())
				.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));

		MobileData savedMobileData = mobileDataRepository.findById(savedProduct.getItemId())
				.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));

		validateProductTradeAvailability(request.productId(), savedMobileData);
		validateProductOwner(savedProduct, memberId);
		validateDataAmount(request.changedAmount(), savedMobileData, memberId);

		Plan plan = planRepository.findByMemberId(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.NOT_FOUND_PLAN));

		BigDecimal beforeAmount = savedMobileData.getDataAmount();
		plan.addMobileData(beforeAmount);

		BigDecimal afterAmount = request.changedAmount();
		if (plan.getCurrentDataAmount().compareTo(afterAmount) < 0) {
			throw new GlobalException(ResultCode.NOT_ENOUGH_DATA);
		}
		plan.deductMobileData(afterAmount);

		int changedPricePer100MB = new BigDecimal(request.price())
				.divide(request.changedAmount().multiply(BigDecimal.TEN), 0,
						java.math.RoundingMode.CEILING).intValue();

		savedProduct.updatePrice(request.price());
		savedMobileData.updateMobileData(request.changedAmount(), changedPricePer100MB,
				request.isSplitType());

		return UpdateMobileDataResponse.from(savedProduct.getId());
	}

	@Transactional
	@CacheEvict(
			value = "wifiByCursor",
			allEntries = true
	)
	public UpdateWifiResponse updateWifi(UpdateWifiRequest request,
			Long memberId) {

		Product savedProduct = productRepository.findById(request.productId())
				.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));

		Wifi savedWifi = wifiRepository.findById(savedProduct.getItemId())
				.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));

		validateProductOwner(savedProduct, memberId);
		validateTime(request.startTime(), request.endTime());

		savedProduct.updatePrice(request.price());
		savedWifi.updateWifi(request.title(), request.content(), request.latitude(),
				request.longitude(), request.address(), request.startTime(), request.endTime());

		// 기존에 저장된 와이파이 이미지들 삭제
		productImageRepository.removeProductImagesById(savedWifi.getId());

		// 새로운 이미지 등록
		List<String> images = request.imageUrls();
		if (images != null && !images.isEmpty()) {
			int idx = 0;
			for (String imgUrl : images) {
				// 확장자 체크 (jpg, jpeg, png만 허용)
				if (s3Service.isNotValidImageExtension(imgUrl)) {
					throw new GlobalException(ResultCode.INVALID_IMAGE_FORMAT);
				}
				ProductImage productImage = ProductImage.of(
						imgUrl,
						idx++, // 리스트 순서가 priority
						savedWifi.getId()
				);
				productImageRepository.save(productImage);
			}
		}

		return UpdateWifiResponse.from(savedProduct.getId());
	}

	@Transactional
	@CacheEvict(
			value = "mobileDataByCursor",
			allEntries = true
	)
	public void deleteProduct(Long productId, Long memberId) {

		Product savedProduct = productRepository.findById(productId)
				.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));

		MobileData refundMobileData = mobileDataRepository.findById(savedProduct.getItemId())
				.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));
		BigDecimal refundAmount = refundMobileData.getRemainAmount();

		Plan plan = planRepository.findByMemberId(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.NOT_FOUND_PLAN));

		plan.addMobileData(refundAmount);

		validateProductOwner(savedProduct, memberId);
		validateProductState(savedProduct);

		savedProduct.changeState(ProductState.DELETED);
	}

	private void validateProductState(Product savedProduct) {

		if (savedProduct.getState().equals(ProductState.DELETED)) {

			throw new GlobalException(ResultCode.ALREADY_DELETED_PRODUCT);
		}
	}

	private void validateTime(LocalDateTime startTime, LocalDateTime endTime) {

		if (startTime.isAfter(endTime)) {
			throw new GlobalException(ResultCode.INVALID_TIME);
		}
	}

	private void validateDataAmount(BigDecimal changedDataAmount, MobileData savedMobileData,
			Long memberId) {

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		BigDecimal resultDataAmount = savedMobileData.getDataAmount().add(changedDataAmount);

		if (resultDataAmount.compareTo(BigDecimal.ZERO) <= 0 ||
				resultDataAmount.compareTo(
						BigDecimal.valueOf(MobileData.MAX_TRANSFERABLE_DATA_AMOUNT)) > 0) {
			throw new GlobalException(ResultCode.INVALID_DATA_TRANSFER_AMOUNT);
		}

		if (member.getSellingData().add(changedDataAmount)
				.compareTo(BigDecimal.valueOf(MobileData.MAX_TRANSFERABLE_DATA_AMOUNT)) > 0) {
			throw new GlobalException(ResultCode.EXCEEDED_TRANSFER_LIMIT);
		}
	}

	private void validateProductOwner(Product savedProduct, Long memberId) {

		if (!savedProduct.getMember().getId().equals(memberId)) {
			throw new GlobalException(ResultCode.OTHER_PRODUCT);
		}
	}

	private void validateMemberId(Long memberId) {

		if (!memberRepository.existsById(memberId)) {

			throw new GlobalException(ResultCode.MEMBER_NOT_FOUND);
		}
	}

	public FindMarketPriceResponse findMarketPrice(String productType) {

		return productRepository.findMarketPrice(ItemType.valueOf(productType));
	}

	@Transactional
	public void hidePreviousMobileDataProducts() {

		productRepository.updateAllBeforeThisMonthAndIsActive();
	}

	private void validateProductTradeAvailability(Long productId, MobileData mobileData) {
		if (tradeRepository.existsByProductId(productId) && mobileData.isSplitType()) {
			throw new GlobalException(ResultCode.PRODUCT_CANNOT_TRADE);
		}
	}

	public Long findMemberIdByProductId(Long productId) {

		return productRepository.findMemberIdByProductId(productId)
				.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));
	}
}
