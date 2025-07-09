package com.dapanda.payment.dto.response;

public record TossConfirmResponse(
        String orderId,
        String paymentKey,
        String totalAmount,
        String method,
        String approvedAt
) {

}
