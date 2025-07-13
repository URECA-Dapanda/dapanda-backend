package com.dapanda.product.controller;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.request.MobileDataCursorRequest;
import com.dapanda.product.dto.request.WifiCursorRequest;
import com.dapanda.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping("/products/mobile-data")
	public CommonResponse<CursorPageResponse<MobileDataSummary>> getMobileDataByCursor(
			@RequestBody @Valid MobileDataCursorRequest request) {

		return CommonResponse.success(productService.findMobileDataByCursor(request));
	}

	@GetMapping("/products/wifi")
	public CommonResponse<CursorPageResponse<WifiSummary>> getWifiByCursor(
			@RequestBody @Valid WifiCursorRequest request) {

		return CommonResponse.success(productService.findWifiByCursor(request));
	}
}
