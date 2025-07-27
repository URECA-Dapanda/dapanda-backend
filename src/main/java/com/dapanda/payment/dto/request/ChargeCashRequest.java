package com.dapanda.payment.dto.request;

public record ChargeCashRequest(
        String paymentKey,
        String orderId,
        int amount
) {

}
