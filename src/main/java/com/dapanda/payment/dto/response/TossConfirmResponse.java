package com.dapanda.payment.dto.response;

public record TossConfirmResponse(
        String orderId,
        String paymentKey,
        int totalAmount,
        String approvedAt
) {

}
