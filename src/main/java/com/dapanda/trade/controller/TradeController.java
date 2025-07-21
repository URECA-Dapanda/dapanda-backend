package com.dapanda.trade.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.trade.dto.request.TradeMobileDataDefaultRequest;
import com.dapanda.trade.dto.request.TradeMobileDataScrapRequest;
import com.dapanda.trade.dto.request.TradeWifiRequest;
import com.dapanda.trade.dto.response.FindMobileDataScrapResponse;
import com.dapanda.trade.dto.response.TradeMobileDataResponse;
import com.dapanda.trade.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TradeController {

	private final TradeService tradeService;

	@PostMapping("/trades/mobile-data/default")
	public CommonResponse<TradeMobileDataResponse> defaultPurchaseMobileData(
			@RequestBody @Valid TradeMobileDataDefaultRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(
				tradeService.defaultPurchaseMobileData(userDetails.getId(), request));
	}

	@GetMapping("/trades/mobile-data/scrap")
	public CommonResponse<FindMobileDataScrapResponse> defaultPurchaseMobileData(
			@RequestParam Float dataAmount) {

		return CommonResponse.success(tradeService.findMobileDataScrap(dataAmount));
	}

	@PostMapping("/trades/mobile-data/scrap")
	public CommonResponse<TradeMobileDataResponse> scrapPurchaseMobileData(
			@RequestBody @Valid TradeMobileDataScrapRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(
				tradeService.scrapPurchaseMobileData(userDetails.getId(), request));
	}

	@PostMapping("/trades/wifi")
	public CommonResponse<TradeMobileDataResponse> purchaseWifi(
			@RequestBody @Valid TradeWifiRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(tradeService.purchaseWifi(userDetails.getId(), request));
	}
}
