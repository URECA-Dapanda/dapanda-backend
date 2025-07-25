package com.dapanda.payment.dto.response;

import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargeCashResponse {

	private Long paymentId;
	private int totalAmount;

	public static ChargeCashResponse of(Long paymentId, int totalAmount) {

		return ChargeCashResponse.builder()
				.paymentId(paymentId)
				.totalAmount(totalAmount)
				.build();
	}
}
