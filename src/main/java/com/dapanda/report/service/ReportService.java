package com.dapanda.report.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.report.dto.request.CreateReportRequest;
import com.dapanda.report.dto.response.CreateReportResponse;
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
	public CreateReportResponse createReport(Long targetId, Long memberId, CreateReportRequest request) {

		validateDuplicateReport(targetId, request.targetCategory(), memberId);

		Long reportedMemberId = findReportTargetMemberId(targetId, request.targetCategory());

		validateSelfReport(reportedMemberId, memberId);

		Member reporter = memberRepository.getReferenceById(memberId);

		Report report = Report.of(request.reason(), targetId, request.targetCategory(), reporter);

		Report savedReport = reportRepository.save(report);

		Member reportedMember = memberRepository.findByIdForUpdate(reportedMemberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		reportedMember.increaseReportedCount();

		return CreateReportResponse.of(savedReport.getId());
	}

	private void validateSelfReport(Long reportedMemberId, Long reporterMemberId) {

		if (reporterMemberId.equals(reportedMemberId)) {

			throw new GlobalException(ResultCode.SELF_REPORT);
		}

	}

	private Long findReportTargetMemberId(Long targetId, ReportTargetCategory category) {

		return switch (category) {

			case MEMBER -> memberRepository.findById(targetId)
					.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND)).getId();

			case PRODUCT -> memberRepository.findMemberIdByProductId(targetId)
					.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));

			case REVIEW -> memberRepository.findMemberIdByReviewId(targetId)
					.orElseThrow(() -> new GlobalException(ResultCode.REVIEW_NOT_FOUND));

			case CHAT -> memberRepository.findMemberIdByChatMessageId(targetId)
					.orElseThrow(() -> new GlobalException(ResultCode.CHAT_MESSAGE_NOT_FOUND));
		};
	}

	private void validateDuplicateReport(Long targetId, ReportTargetCategory category, Long memberId) {

		boolean isDuple = reportRepository.existsByReportTargetIdAndReportTargetCategoryAndReporterId(targetId, category, memberId);

		if (isDuple) {

			throw new GlobalException(ResultCode.DUPLICATE_REPORT);
		}
	}
}
