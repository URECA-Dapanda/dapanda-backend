package com.dapanda.plan.entity;

import com.dapanda.member.entity.Member;

public class PlanFixture {

	public static Plan createPlan(Member member, Float providingDataAmount) {

		return Plan.of("요금제", providingDataAmount, 30000, PlanCategory._5G, AgeGroup.ANY,
				member);
	}
}
