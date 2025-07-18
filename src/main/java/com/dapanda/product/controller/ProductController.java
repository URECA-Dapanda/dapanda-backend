package com.dapanda.product.controller;

import com.dapanda.auth.entity.CustomUserDetails;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping("/members/{memberId}/selling-products")
	public CommonResponse<CursorPageResponse<ReadSellingProductResponse>> readSellingProductHistory(

			@PathVariable Long memberId,
			@RequestParam ProductState productState,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) Integer size) {

		ReadSellingProductRequest request = new ReadSellingProductRequest(cursorId, size, memberId, productState);

		return CommonResponse.success(productService.readSellingProduct(request));
	}

	@PostMapping("/products/mobile-data")
	public CommonResponse<CursorPageResponse<MobileDataSummary>> getMobileDataByCursor(
			@RequestBody @Valid MobileDataCursorRequest request) {

		return CommonResponse.success(productService.findMobileDataByCursor(request));
	}

	@PostMapping("/products/wifi")
	public CommonResponse<CursorPageResponse<WifiSummary>> getWifiByCursor(
			@RequestBody @Valid WifiCursorRequest request) {

		return CommonResponse.success(productService.findWifiByCursor(request));
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
}
