package com.dapanda.plan.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.plan.dto.response.PlanInfoResponse;
import com.dapanda.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlanController {

	private final PlanService planService;

	@GetMapping("/plans/my-data")
	public ResponseEntity<CommonResponse<PlanInfoResponse>> getMyMobileDataInfo(
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ResponseEntity.ok(CommonResponse.success(
				planService.findMobileDataInfoByMemberId(userDetails.getId())));
	}

}
