package com.dapanda.product.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.dto.response.CountCursorPageResponse;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.request.*;
import com.dapanda.product.dto.response.*;
import com.dapanda.product.entity.ProductState;
import com.dapanda.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping("/members/{memberId}/selling-products")
	public CommonResponse<CountCursorPageResponse<ReadSellingProductResponse>> readSellingProductHistory(

			@PathVariable Long memberId,
			@RequestParam ProductState productState,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) Integer size) {

		ReadSellingProductRequest request = new ReadSellingProductRequest(cursorId, size, memberId,
				productState);

		return CommonResponse.success(productService.readSellingProduct(request));
	}

	@GetMapping("/selling-products")
	public CommonResponse<CountCursorPageResponse<ReadSellingProductResponse>> readMySellingProductHistory(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam ProductState productState,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) Integer size) {

		ReadSellingProductRequest request = new ReadSellingProductRequest(cursorId, size,
				userDetails.getId(),
				productState);

		return CommonResponse.success(productService.readSellingProduct(request));
	}

	@GetMapping("/products/mobile-data")
	public CommonResponse<CursorPageResponse<MobileDataSummary>> getMobileDataByCursor(
			@RequestParam(required = false) Long cursorId,
			@RequestParam @Min(1) Integer size,
			@RequestParam String productSortOption,
			@RequestParam(required = false) BigDecimal dataAmount) {

		return CommonResponse.success(
				productService.findMobileDataByCursor(cursorId, size, productSortOption,
						dataAmount));
	}

	@GetMapping("/products/wifi")
	public CommonResponse<CursorPageResponse<WifiSummary>> getWifiByCursor(
			@RequestParam(required = false) Long cursorId,
			@RequestParam @Min(1) Integer size,
			@RequestParam String productSortOption,
			@RequestParam(required = false) boolean open,
			@RequestParam Double latitude,
			@RequestParam Double longitude) {

		return CommonResponse.success(
				productService.findWifiByCursor(cursorId, size, productSortOption, open, latitude,
						longitude));
	}

	@GetMapping("/products/mobile-data/{productId}")
	public CommonResponse<MobileDataInfoResponse> getMobileDataInfo(
			@PathVariable("productId") Long productId) {

		return CommonResponse.success(productService.findMobileDataInfo(productId));
	}

	@GetMapping("/products/wifi/{productId}")
	public CommonResponse<WifiInfoResponse> getWifiInfo(
			@PathVariable("productId") Long productId) {

		return CommonResponse.success(productService.findWifiInfo(productId));
	}

	@PostMapping("/products/mobile-data")
	public CommonResponse<Void> createMobileData(
			@RequestBody @Valid CreateMobileDataRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails
	) {

		productService.createMobileData(request, userDetails.getId());

		return CommonResponse.success(null);
	}

	@PostMapping("/products/wifi")
	public CommonResponse<Void> createWifi(
			@RequestBody @Valid CreateWifiRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails
	) {

		productService.createWifi(request, userDetails.getId());

		return CommonResponse.success(null);
	}

	@PutMapping("/products/mobile-data")
	public CommonResponse<UpdateMobileDataResponse> updateMobileData(
			@RequestBody @Valid UpdateMobileDataRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(
				productService.updateMobileData(request, userDetails.getId()));
	}

	@PutMapping("/products/wifi")
	public CommonResponse<UpdateWifiResponse> updateWifi(
			@RequestBody @Valid UpdateWifiRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(
				productService.updateWifi(request, userDetails.getId()));
	}

	@DeleteMapping("/products/{productId}")
	public CommonResponse<Void> deleteProduct(
			@PathVariable("productId") Long productId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		productService.deleteProduct(productId, userDetails.getId());

		return CommonResponse.success(null);
	}

	@GetMapping("/products/market-price")
	public CommonResponse<FindMarketPriceResponse> getMarketPrice(
			@RequestParam String productType) {

		return CommonResponse.success(productService.findMarketPrice(productType));
	}
}
