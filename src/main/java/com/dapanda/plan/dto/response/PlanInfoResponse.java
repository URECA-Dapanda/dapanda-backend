package com.dapanda.plan.dto.response;

import com.dapanda.plan.entity.Plan;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanInfoResponse {

	private String planName;
	private float providingDataAmount;
	private int monthlyPrice;

	public static PlanInfoResponse of(Plan plan) {

		return new PlanInfoResponse(
				plan.getName(),
				plan.getProvidingDataAmount(),
				plan.getMonthlyPrice()
		);
	}
}
