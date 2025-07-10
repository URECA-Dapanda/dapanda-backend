package com.dapanda.payment.dto.request;

public record AmountRequest(
        String orderId,
        int amount
) {

}
