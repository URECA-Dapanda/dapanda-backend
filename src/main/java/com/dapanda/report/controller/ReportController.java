package com.dapanda.report.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.report.dto.request.CreateReportRequest;
import com.dapanda.report.dto.response.CreateReportResponse;
import com.dapanda.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;

	@PostMapping("/report/{targetId}")
	public CommonResponse<CreateReportResponse> createReport(
			@PathVariable Long targetId,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestBody @Valid CreateReportRequest request) {

		return CommonResponse.success(reportService.createReport(targetId, userDetails.getId(), request));
	}
}
