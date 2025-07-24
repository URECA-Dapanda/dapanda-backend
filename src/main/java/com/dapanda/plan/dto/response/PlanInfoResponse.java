package com.dapanda.plan.dto.response;

import com.dapanda.plan.entity.Plan;
import java.math.BigDecimal;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanInfoResponse {

	private String name;
	private BigDecimal providingDataAmount;
	private int monthlyPrice;

	public static PlanInfoResponse of(Plan plan) {

		return PlanInfoResponse.builder().
				name(plan.getName()).
				providingDataAmount(plan.getProvidingDataAmount()).
				monthlyPrice(plan.getMonthlyPrice()).
				build();
	}
}
