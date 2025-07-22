package com.dapanda.trade.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.trade.dto.request.DefaultPurchaseMobileDataRequest;
import com.dapanda.trade.dto.request.PurchaseWifiRequest;
import com.dapanda.trade.dto.request.ScrapPurchaseMobileDataRequest;
import com.dapanda.trade.dto.response.FindCashHistoryResponse;
import com.dapanda.trade.dto.response.FindMobileDataScrapResponse;
import com.dapanda.trade.dto.response.FindTradeHistoryResponse;
import com.dapanda.trade.dto.response.TradeProductResponse;
import com.dapanda.trade.service.TradeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
	public CommonResponse<TradeProductResponse> defaultPurchaseMobileData(
			@RequestBody @Valid DefaultPurchaseMobileDataRequest request,
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
	public CommonResponse<TradeProductResponse> scrapPurchaseMobileData(
			@RequestBody @Valid ScrapPurchaseMobileDataRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(
				tradeService.scrapPurchaseMobileData(userDetails.getId(), request));
	}

	@PostMapping("/trades/wifi")
	public CommonResponse<TradeProductResponse> purchaseWifi(
			@RequestBody @Valid PurchaseWifiRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(tradeService.purchaseWifi(userDetails.getId(), request));
	}

	@GetMapping("/trades/purchase-history")
	public CommonResponse<FindTradeHistoryResponse> getTradeHistory(
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) Integer size,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(
				tradeService.findTradeHistory(cursorId, size, userDetails.getId()));
	}

	@GetMapping("/trades/cash-history")
	public CommonResponse<FindCashHistoryResponse> cashHistory(
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) Integer size,
			@RequestParam Integer year,
			@RequestParam Integer month,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(
				tradeService.findCashHistory(cursorId, size, userDetails.getId(), year, month));
	}
}
