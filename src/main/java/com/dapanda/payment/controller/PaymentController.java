package com.dapanda.payment.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.payment.dto.request.*;
import com.dapanda.payment.dto.response.ChargeCashResponse;
import com.dapanda.payment.dto.response.RefundCashResponse;
import com.dapanda.payment.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/payments/save-amount")
	public CommonResponse<Void> saveAmount(HttpSession session,
			@RequestBody AmountRequest request) {

		session.setAttribute(request.orderId(), request.amount());

		return CommonResponse.success(null);
	}

	@PostMapping("/payments/verify-amount")
	public CommonResponse<Void> verifyAmount(HttpSession session,
			@RequestBody AmountRequest request) {

		String amount = String.valueOf(session.getAttribute(request.orderId()));

		if (amount == null) {
			return new CommonResponse<>(ResultCode.INVALID_PAYMENT_AMOUNT);
		}
		if (!amount.equals(String.valueOf(request.amount()))) {
			return new CommonResponse<>(ResultCode.PAYMENT_AMOUNT_MISMATCH);
		}

		return CommonResponse.success(null);
	}

	@PostMapping("/payments/charge")
	public CommonResponse<ChargeCashResponse> chargeCash(
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@RequestBody ChargeCashRequest request) {

		Long memberId = customUserDetails.getId();

		ChargeCashResponse response = paymentService.chargeCash(memberId, request);

		return CommonResponse.success(response);
	}

	@PostMapping("/payments/refund")
	public CommonResponse<RefundCashResponse> refundCash(
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@RequestBody RefundCashRequest request) {

		Long memberId = customUserDetails.getId();

		RefundCashResponse response = paymentService.refundCash(memberId, request);

		return CommonResponse.success(response);
	}
}
