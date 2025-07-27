package com.dapanda.plan.entity;

import com.dapanda.member.entity.Member;
import java.math.BigDecimal;

public class PlanFixture {

	public static Plan createPlan(Member member, BigDecimal providingDataAmount) {

		return Plan.of("요금제", providingDataAmount, providingDataAmount, 30000, PlanCategory._5G,
				AgeGroup.ANY,
				member);
	}
}
