package com.dapanda.payment.dto.request;

public record RefundCashRequest(
		String requestId,
		int refundAmount
) {

}
