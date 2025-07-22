package com.dapanda.product.service;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.request.CreateMobileDataRequest;
import com.dapanda.product.dto.request.CreateWifiRequest;
import com.dapanda.product.dto.request.ReadSellingProductRequest;
import com.dapanda.product.dto.request.UpdateMobileDataRequest;
import com.dapanda.product.dto.request.UpdateWifiRequest;
import com.dapanda.product.dto.response.FindMarketPriceResponse;
import com.dapanda.product.dto.response.MobileDataInfoResponse;
import com.dapanda.product.dto.response.ReadSellingProductResponse;
import com.dapanda.product.dto.response.UpdateMobileDataResponse;
import com.dapanda.product.dto.response.UpdateWifiResponse;
import com.dapanda.product.dto.response.WifiInfoResponse;
import com.dapanda.product.entity.ItemType;
import com.dapanda.product.entity.MobileData;
import com.dapanda.product.entity.Product;
import com.dapanda.product.entity.ProductSortOption;
import com.dapanda.product.entity.ProductState;
import com.dapanda.product.entity.Wifi;
import com.dapanda.product.repository.MobileDataRepository;
import com.dapanda.product.repository.ProductRepository;
import com.dapanda.product.repository.WifiRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;

	private final MobileDataRepository mobileDataRepository;

	private final WifiRepository wifiRepository;

	private final MemberRepository memberRepository;

	public CursorPageResponse<ReadSellingProductResponse> readSellingProduct(
			ReadSellingProductRequest request) {

		validateMemberId(request.memberId());

		List<ReadSellingProductResponse> response = productRepository.findSellingProduct(request);

		boolean hasNext = response.size() > request.size();

		if (hasNext) {
			response = response.subList(0, request.size());
		}

		Long nextCursorId = hasNext && !response.isEmpty()
				? response.get(response.size() - 1).getProductId()
				: null;

		CursorPageResponse.PageInfo pageInfo = CursorPageResponse.PageInfo.of(
				nextCursorId,
				hasNext,
				request.size()
		);

		return CursorPageResponse.of(response, pageInfo);
	}

//	public CursorPageResponse<MobileDataSummary> findMobileDataByCursor(
//			MobileDataCursorRequest request) {

	public CursorPageResponse<MobileDataSummary> findMobileDataByCursor(Long cursorId, Integer size,
			String productSortOption, Float dataAmount) {

		return productRepository.findMobileDataByCursor(cursorId, size,
				ProductSortOption.from(productSortOption), dataAmount);
	}

	public CursorPageResponse<WifiSummary> findWifiByCursor(Long cursorId, Integer size,
			String productSortOption, boolean open, Double latitude, Double longitude) {

		return productRepository.findWifiByCursor(cursorId, size,
				ProductSortOption.from(productSortOption), open, latitude, longitude);
	}

	public MobileDataInfoResponse findMobileDataInfo(Long productId) {

		if (!productRepository.existsById(productId)) {
			throw new GlobalException(ResultCode.PRODUCT_NOT_FOUND);
		}

		MobileDataInfoResponse response = productRepository.findMobileDataInfo(productId);

		if (response == null) {
			throw new GlobalException(ResultCode.INVALID_PRODUCT);
		}

		return response;
	}

	public WifiInfoResponse findWifiInfo(Long productId) {

		if (!productRepository.existsById(productId)) {
			throw new GlobalException(ResultCode.PRODUCT_NOT_FOUND);
		}

		WifiInfoResponse response = productRepository.findWifiInfo(productId);

		if (response == null) {
			throw new GlobalException(ResultCode.INVALID_PRODUCT);
		}

		List<String> wifiImages = productRepository.findWifiImages(response.getItemId());

		return response.withImageUrls(wifiImages);
	}

	@Transactional
	public void createMobileData(CreateMobileDataRequest request, Long memberId) {

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		Float soldAmount = productRepository.sumSoldMobileDataAmountByMemberId(member.getId());
		if (soldAmount == null) {
			soldAmount = 0f;
		}

		float willSellAmount = request.getDataAmount();
		if (soldAmount + willSellAmount > MobileData.MAX_TRANSFERABLE_DATA_AMOUNT * 1000) {
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
	public void createWifi(CreateWifiRequest request, Long memberId) {

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		Wifi savedWifi = wifiRepository.save(
				Wifi.of(
						request.getTitle(),
						request.getContent(),
						request.getLatitude(),
						request.getLongitude(),
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

		validateProductOwner(savedProduct, memberId);
	}


	@Transactional
	public UpdateMobileDataResponse updateMobileData(UpdateMobileDataRequest request,
			Long memberId) {

		Product savedProduct = productRepository.findById(request.productId())
				.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));

		MobileData savedMobileData = mobileDataRepository.findById(savedProduct.getItemId())
				.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));

		validateProductOwner(savedProduct, memberId);
		validateDataAmount(request.changedAmount(), savedMobileData, memberId);

		int changedPricePer100MB = (int) (request.price() / (savedMobileData.getDataAmount()
				* 1000));

		savedProduct.updatePrice(request.price());
		savedMobileData.updateMobileData(request.changedAmount(), changedPricePer100MB,
				request.isSplitType());

		return UpdateMobileDataResponse.from(savedProduct.getId());
	}

	@Transactional
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
				request.longitude(), request.startTime(), request.endTime());

		return UpdateWifiResponse.from(savedProduct.getId());
	}

	@Transactional
	public void deleteProduct(Long productId, Long memberId) {

		Product savedProduct = productRepository.findById(productId)
				.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));

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

	private void validateDataAmount(float changedDataAmount, MobileData savedMobileData,
			Long memberId) {

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		float resultDataAmount = savedMobileData.getDataAmount() + changedDataAmount;

		if (resultDataAmount <= 0 || resultDataAmount > MobileData.MAX_TRANSFERABLE_DATA_AMOUNT) {
			throw new GlobalException(ResultCode.INVALID_DATA_TRANSFER_AMOUNT);
		}

		if (member.getSellingData() + changedDataAmount > MobileData.MAX_TRANSFERABLE_DATA_AMOUNT) {
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
}
