package com.dapanda.report.dto.response;

import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateReportResponse {

	private Long reportId;

	public static CreateReportResponse of(Long reportId){

		return CreateReportResponse.builder()
				.reportId(reportId)
				.build();
	}
}
