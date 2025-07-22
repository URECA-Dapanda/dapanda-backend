package com.dapanda.product.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
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
import com.dapanda.product.entity.ProductState;
import com.dapanda.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping("/members/{memberId}/selling-products")
	public ResponseEntity<CommonResponse<CursorPageResponse<ReadSellingProductResponse>>> readSellingProductHistory(

			@PathVariable Long memberId,
			@RequestParam ProductState productState,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) Integer size) {

		ReadSellingProductRequest request = new ReadSellingProductRequest(cursorId, size, memberId,
				productState);

		return ResponseEntity.ok(
				CommonResponse.success(productService.readSellingProduct(request)));
	}

	@GetMapping("/selling-products")
	public ResponseEntity<CommonResponse<CursorPageResponse<ReadSellingProductResponse>>> readMySellingProductHistory(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam ProductState productState,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) Integer size) {

		ReadSellingProductRequest request = new ReadSellingProductRequest(cursorId, size,
				userDetails.getId(),
				productState);

		return ResponseEntity.ok(
				CommonResponse.success(productService.readSellingProduct(request)));
	}

	@GetMapping("/products/mobile-data")
	public ResponseEntity<CommonResponse<CursorPageResponse<MobileDataSummary>>> getMobileDataByCursor(
			@RequestParam(required = false) Long cursorId,
			@RequestParam @Min(1) Integer size,
			@RequestParam String productSortOption,
			@RequestParam(required = false) Float dataAmount) {

		return ResponseEntity.ok(CommonResponse.success(
				productService.findMobileDataByCursor(cursorId, size, productSortOption,
						dataAmount)));
	}

	@GetMapping("/products/wifi")
	public ResponseEntity<CommonResponse<CursorPageResponse<WifiSummary>>> getWifiByCursor(
			@RequestParam(required = false) Long cursorId,
			@RequestParam @Min(1) Integer size,
			@RequestParam String productSortOption,
			@RequestParam(required = false) boolean open,
			@RequestParam Double latitude,
			@RequestParam Double longitude) {

		return ResponseEntity.ok(CommonResponse.success(
				productService.findWifiByCursor(cursorId, size, productSortOption, open, latitude,
						longitude)));
	}

	@GetMapping("/products/mobile-data/{productId}")
	public ResponseEntity<CommonResponse<MobileDataInfoResponse>> getMobileDataInfo(
			@PathVariable("productId") Long productId) {

		return ResponseEntity.ok(
				CommonResponse.success(productService.findMobileDataInfo(productId)));
	}

	@GetMapping("/products/wifi/{productId}")
	public ResponseEntity<CommonResponse<WifiInfoResponse>> getWifiInfo(
			@PathVariable("productId") Long productId) {

		return ResponseEntity.ok(CommonResponse.success(productService.findWifiInfo(productId)));
	}

	@PostMapping("/products/mobile-data")
	public ResponseEntity<CommonResponse<Void>> createMobileData(
			@RequestBody @Valid CreateMobileDataRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails
	) {

		if (userDetails == null) {
			throw new GlobalException(ResultCode.UNAUTHORIZED);
		}

		productService.createMobileData(request, userDetails.getId());

		return ResponseEntity.ok(CommonResponse.success(null));
	}

	@PostMapping("/products/wifi")
	public ResponseEntity<CommonResponse<Void>> createWifi(
			@RequestBody @Valid CreateWifiRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails
	) {

		if (userDetails == null) {
			throw new GlobalException(ResultCode.UNAUTHORIZED);
		}

		productService.createWifi(request, userDetails.getId());

		return ResponseEntity.ok(CommonResponse.success(null));
	}

	@PutMapping("/products/mobile-data")
	public ResponseEntity<CommonResponse<UpdateMobileDataResponse>> updateMobileData(
			@RequestBody @Valid UpdateMobileDataRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ResponseEntity.ok(CommonResponse.success(
				productService.updateMobileData(request, userDetails.getId())));
	}

	@PutMapping("/products/wifi")
	public ResponseEntity<CommonResponse<UpdateWifiResponse>> updateWifi(
			@RequestBody @Valid UpdateWifiRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ResponseEntity.ok(
				CommonResponse.success(productService.updateWifi(request, userDetails.getId())));
	}

	@DeleteMapping("/products/{productId}")
	public ResponseEntity<CommonResponse<Void>> deleteProduct(
			@PathVariable("productId") Long productId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		productService.deleteProduct(productId, userDetails.getId());

		return ResponseEntity.ok(CommonResponse.success(null));
	}

	@GetMapping("/products/market-price")
	public ResponseEntity<CommonResponse<FindMarketPriceResponse>> getMarketPrice(
			@RequestParam String productType) {

		return ResponseEntity.ok(
				CommonResponse.success(productService.findMarketPrice(productType)));
	}
}
