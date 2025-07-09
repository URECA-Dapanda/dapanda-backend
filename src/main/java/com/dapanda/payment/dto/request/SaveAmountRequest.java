package com.dapanda.payment.dto.request;

public record SaveAmountRequest(
        String orderId,
        int amount
) {

}
