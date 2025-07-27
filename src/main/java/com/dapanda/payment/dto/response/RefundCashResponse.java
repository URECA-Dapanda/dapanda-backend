package com.dapanda.payment.dto.response;

import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundCashResponse {

	private int refundPrice;
	private int remainCash;

	public static RefundCashResponse of(int refundPrice, int remainCash) {

		return RefundCashResponse.builder()
				.refundPrice(refundPrice)
				.remainCash(remainCash)
				.build();
	}
}
