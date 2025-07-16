package com.dapanda.report.dto;

import com.dapanda.report.entity.ReportTargetCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateReportRequest(

		@NotBlank(message = "신고 사유는 필수입니다.")
		@Max(value = 100, message = "신고 사유는 최대 100자 입니다.")
		String reason,
		@NotNull(message = "신고 대상 유형은 필수입니다.")
		ReportTargetCategory targetCategory) {
}
