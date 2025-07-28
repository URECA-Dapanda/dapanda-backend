package com.dapanda.plan.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.plan.dto.response.PlanInfoResponse;
import com.dapanda.plan.entity.*;
import com.dapanda.plan.repository.PlanRepository;
import java.math.BigDecimal;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanService {

	private final PlanRepository planRepository;

	public boolean hasPlan(Member member) {

		return planRepository.existsByMember(member);
	}

	@Transactional
	public Plan createRandomPlanForMember(Member member) {

		if (hasPlan(member)) {
			return planRepository.findByMember(member).orElseThrow();
		}

		PlanCategory category = randomCategory();
		AgeGroup ageGroup = randomAgeGroup();
		String name = randomPlanName(category, ageGroup);
		BigDecimal dataAmount = new BigDecimal(randomDataAmount());
		int price = randomMonthlyPrice();

		Plan plan = Plan.of(
				name,
				dataAmount,
				price,
				category,
				ageGroup,
				member
		);

		return planRepository.save(plan);
	}

	private String randomPlanName(PlanCategory category, AgeGroup ageGroup) {

		String[] tiers = {"베이직", "Value", "프리미엄", "플러스", "라이트"};
		String tier = tiers[new Random().nextInt(tiers.length)];

		return category.getCategory() + " " + ageGroup.name() + " " + tier;
	}

	private int randomDataAmount() {

		int[] data = {3, 5, 10, 15, 20, 30, 50, 100};

		return data[new Random().nextInt(data.length)];
	}

	private int randomMonthlyPrice() {

		int[] prices = {9000, 12000, 18000, 25000, 32000, 45000, 59000};

		return prices[new Random().nextInt(prices.length)];
	}

	private PlanCategory randomCategory() {

		PlanCategory[] categories = PlanCategory.values();

		return categories[new Random().nextInt(categories.length)];
	}

	private AgeGroup randomAgeGroup() {

		AgeGroup[] groups = AgeGroup.values();

		return groups[new Random().nextInt(groups.length)];
	}

	public PlanInfoResponse findMobileDataInfoByMemberId(Long memberId) {

		Plan plan = planRepository.findByMemberId(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.NOT_FOUND_PLAN));

		return PlanInfoResponse.of(plan);
	}

	@Transactional
	public void resetMemberMobileDataPlan() {

		planRepository.resetMemberMobileDataPlan();
	}

}
