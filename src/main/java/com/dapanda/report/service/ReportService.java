package com.dapanda.report.service;

import com.dapanda.chat.service.ChatService;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.member.service.MemberService;
import com.dapanda.product.service.ProductService;
import com.dapanda.report.dto.request.CreateReportRequest;
import com.dapanda.report.dto.response.CreateReportResponse;
import com.dapanda.report.entity.Report;
import com.dapanda.report.entity.ReportTargetCategory;
import com.dapanda.report.repository.ReportRepository;
import com.dapanda.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final ReportRepository reportRepository;
	private final MemberRepository memberRepository;
	private final ChatService chatService;
	private final ReviewService reviewService;
	private final ProductService productService;
	private final MemberService memberService;

	@Transactional
	public CreateReportResponse createReport(Long targetId, Long memberId, CreateReportRequest request) {

		validateDuplicateReport(targetId, request.targetCategory(), memberId);

		Long reportedMemberId = findReportTargetMemberId(targetId, request.targetCategory(), memberId);

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

	private Long findReportTargetMemberId(Long targetId, ReportTargetCategory category, Long reporterId) {

		return switch (category) {
			case MEMBER -> memberService.findMemberId(targetId);
			case PRODUCT -> productService.findMemberIdByProductId(targetId);
			case REVIEW -> reviewService.findMemberIdByReviewId(targetId);
			case CHAT -> chatService.findMemberIdByChatMessageId(targetId, reporterId);
		};
	}

	private void validateDuplicateReport(Long targetId, ReportTargetCategory category, Long memberId) {

		boolean isDuple = reportRepository.existsByReportTargetIdAndReportTargetCategoryAndReporterId(targetId, category, memberId);

		if (isDuple) {

			throw new GlobalException(ResultCode.DUPLICATE_REPORT);
		}
	}
}
