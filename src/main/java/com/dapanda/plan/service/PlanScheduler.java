package com.dapanda.plan.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanScheduler {

	private final PlanService planService;

	@Scheduled(cron = "0 0 0 1 * *") // 매월 1일 00:00 실행
	public void resetMemberMobileDataPlan() {

		planService.resetMemberMobileDataPlan();
	}
}
