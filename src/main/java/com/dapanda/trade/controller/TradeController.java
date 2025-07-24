package com.dapanda.trade.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.trade.dto.request.*;
import com.dapanda.trade.dto.response.*;
import com.dapanda.trade.service.TradeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam BigDecimal dataAmount) {

		return CommonResponse.success(
				tradeService.findMobileDataScrap(dataAmount, userDetails.getId()));
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
