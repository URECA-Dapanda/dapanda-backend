package com.dapanda.plan.service.entity;

import com.dapanda.member.entity.Member;
import com.dapanda.plan.entity.AgeGroup;
import com.dapanda.plan.entity.Plan;
import com.dapanda.plan.entity.PlanCategory;

public class PlanFixture {

	public static Plan createPlan(Member member, Float providingDataAmount) {

		return Plan.of("요금제", providingDataAmount, 30000, PlanCategory._5G, AgeGroup.ANY,
				member);
	}
}
