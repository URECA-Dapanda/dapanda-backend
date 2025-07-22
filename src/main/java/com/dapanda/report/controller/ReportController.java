package com.dapanda.report.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.report.dto.request.CreateReportRequest;
import com.dapanda.report.dto.response.CreateReportResponse;
import com.dapanda.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;

	@PostMapping("/report/{targetId}")
	public ResponseEntity<CommonResponse<CreateReportResponse>> createReport(
			@PathVariable Long targetId,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestBody @Valid CreateReportRequest request) {

		return ResponseEntity.ok(CommonResponse.success(
				reportService.createReport(targetId, userDetails.getId(), request)));
	}
}
