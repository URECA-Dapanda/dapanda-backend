package com.dapanda.report.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.report.dto.CreateReportRequest;
import com.dapanda.report.entity.Report;
import com.dapanda.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final ReportRepository reportRepository;
	private final MemberRepository memberRepository;

	public void createReport(Long targetId, Long memberId, CreateReportRequest request) {

		Member reporter = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		Report report = Report.of(request.reason(), targetId, request.targetCategory(), reporter);

		reportRepository.save(report);
	}

}
