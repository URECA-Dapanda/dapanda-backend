package com.dapanda.payment.dto.request;

public record TossConfirmRequest(
        String paymentKey,
        String orderId,
        int amount
) {

}
