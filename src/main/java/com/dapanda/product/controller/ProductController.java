package com.dapanda.product.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.request.MobileDataCursorRequest;
import com.dapanda.product.dto.request.UpdateMobileDataRequest;
import com.dapanda.product.dto.request.UpdateWifiRequest;
import com.dapanda.product.dto.request.WifiCursorRequest;
import com.dapanda.product.dto.response.MobileDataInfoResponse;
import com.dapanda.product.dto.response.UpdateMobileDataResponse;
import com.dapanda.product.dto.response.UpdateWifiResponse;
import com.dapanda.product.dto.response.WifiInfoResponse;
import com.dapanda.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

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
}
