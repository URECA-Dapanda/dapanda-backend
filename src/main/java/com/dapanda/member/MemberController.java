package com.dapanda.member;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.member.dto.response.FindCashResponse;
import com.dapanda.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@GetMapping("/members/cash")
	public CommonResponse<FindCashResponse> getCash(
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(memberService.findCash(userDetails.getId()));
	}
}
