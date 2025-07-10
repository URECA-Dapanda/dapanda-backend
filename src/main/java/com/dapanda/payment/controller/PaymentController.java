package com.dapanda.payment.controller;

import com.dapanda.common.exception.CommonResponse;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.payment.dto.request.SaveAmountRequest;
import com.dapanda.payment.dto.request.TossConfirmRequest;
import com.dapanda.payment.dto.response.TossConfirmResponse;
import com.dapanda.payment.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payments/save-amount")
    public CommonResponse<Void> saveAmount(HttpSession session,
            @RequestBody SaveAmountRequest request) {

        session.setAttribute(request.orderId(), request.amount());

        return CommonResponse.success(null);
    }

    @PostMapping("/payments/verify-amount")
    public CommonResponse<Void> verifyAmount(HttpSession session,
            @RequestBody SaveAmountRequest request) {

        String amount = String.valueOf(session.getAttribute(request.orderId()));

        if (amount == null) {
            return new CommonResponse<>(ResultCode.INVALID_PAYMENT_AMOUNT);
        }
        if (!amount.equals(String.valueOf(request.amount()))) {
            return new CommonResponse<>(ResultCode.PAYMENT_AMOUNT_MISMATCH);
        }

        return CommonResponse.success(null);
    }

    // TODO: @AuthenticationPrincipal CustomUserDetails customUserDetails 적용
    @PostMapping("/payments/confirm")
    public CommonResponse<Void> confirm(
            @RequestParam("memberId") Long memberId,
            @RequestBody TossConfirmRequest request) {

//        Long memberId = customUserDetails.getId();

        TossConfirmResponse response = paymentService.confirmPayment(memberId, request);
        paymentService.updateCash(memberId, response.totalAmount());

        return CommonResponse.success(null);
    }
}
