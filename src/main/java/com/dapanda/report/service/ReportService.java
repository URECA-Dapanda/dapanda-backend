package com.dapanda.report.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.report.dto.request.CreateReportRequest;
import com.dapanda.report.entity.Report;
import com.dapanda.report.entity.ReportTargetCategory;
import com.dapanda.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final ReportRepository reportRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void createReport(Long targetId, Long memberId, CreateReportRequest request) {

		Member reporter = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		Report report = Report.of(request.reason(), targetId, request.targetCategory(), reporter);

		reportRepository.save(report);

		Long reportTargetMemberId = findReportTargetMemberId(targetId, request.targetCategory());

		Member reportedMember = memberRepository.findByIdWithLock(reportTargetMemberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		reportedMember.increaseReportedCount();
	}

	private Long findReportTargetMemberId(Long targetId, ReportTargetCategory category) {

		return switch (category) {

			case MEMBER -> memberRepository.findById(targetId)
					.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND)).getId();

			case PRODUCT -> memberRepository.findMemberIdByProductId(targetId)
					.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));

			case REVIEW -> memberRepository.findMemberIdByReviewId(targetId)
					.orElseThrow(() -> new GlobalException(ResultCode.REVIEW_NOT_FOUND));
		};
	}
}
