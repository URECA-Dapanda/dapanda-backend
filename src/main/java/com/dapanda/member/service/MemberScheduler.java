package com.dapanda.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberScheduler {

	private final MemberService memberService;

	@Scheduled(cron = "0 0 0 1 * *") // 매월 1일 00:00 실행
	public void resetMemberDataMount() {

		memberService.resetMemberMobileDataAmount();
	}
}
