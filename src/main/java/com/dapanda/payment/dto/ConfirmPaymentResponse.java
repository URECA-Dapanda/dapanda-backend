package com.dapanda.payment.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfirmPaymentResponse {

	private Long paymentId;
	private int totalAmount;

	public static ConfirmPaymentResponse from(Long paymentId, int totalAmount) {

		return ConfirmPaymentResponse.builder()
				.paymentId(paymentId)
				.totalAmount(totalAmount)
				.build();
	}
}
