package com.dapanda.report.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.report.dto.CreateReportRequest;
import com.dapanda.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;

	@PostMapping("/report/{targetId}")
	public CommonResponse<Void> createReport(
			@PathVariable Long targetId,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestBody CreateReportRequest request) {

		reportService.createReport(targetId, userDetails.getId(), request);

		return CommonResponse.success(null);
	}
}
