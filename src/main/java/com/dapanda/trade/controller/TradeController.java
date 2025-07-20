package com.dapanda.trade.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.trade.dto.request.TradeMobileDataDefaultRequest;
import com.dapanda.trade.dto.response.TradeMobileDataDefaultResponse;
import com.dapanda.trade.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TradeController {

	private final TradeService tradeService;

	@PostMapping("/trades/mobile-data/default")
	public CommonResponse<TradeMobileDataDefaultResponse> mobileDataDefaultPurchase(
			@RequestBody @Valid TradeMobileDataDefaultRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(tradeService.mobileDataDefault(userDetails.getId(), request));
	}
}
