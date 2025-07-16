package com.dapanda.review.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.review.dto.request.DeleteReviewRequest;
import com.dapanda.review.dto.request.SaveReviewRequest;
import com.dapanda.review.dto.request.UpdateReviewRequest;
import com.dapanda.review.dto.response.SaveReviewResponse;
import com.dapanda.review.dto.response.UpdateReviewResponse;
import com.dapanda.review.entity.Review;
import com.dapanda.review.entity.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final MemberRepository memberRepository;

	public SaveReviewResponse saveReview(SaveReviewRequest request, Long memberId) {

		validateSelfReview(memberId, request.revieweeId());
		validateRevieweeId(request.revieweeId());

		Member reviewer = memberRepository.getReferenceById(memberId);
		Member reviewee = memberRepository.getReferenceById(request.revieweeId());

		Review review = Review.of(request.rating(), request.comment(), request.productId(),
				reviewer, reviewee);

		Review savedReview = reviewRepository.save(review);

		return SaveReviewResponse.from(savedReview.getId());
	}

	@Transactional
	public UpdateReviewResponse updateReview(UpdateReviewRequest request, Long memberId) {

		Review savedReview = reviewRepository.findById(request.reviewId())
				.orElseThrow(() -> new GlobalException(ResultCode.REVIEW_NOT_FOUND));

		validateReviewOwner(savedReview, memberId);

		savedReview.updateReview(request);

		return UpdateReviewResponse.from(savedReview.getId());
	}

	public void deleteReview(DeleteReviewRequest request, Long memberId) {

		Review savedReview = reviewRepository.findById(request.reviewId())
				.orElseThrow(() -> new GlobalException(ResultCode.REVIEW_NOT_FOUND));

		validateReviewOwner(savedReview, memberId);

		reviewRepository.delete(savedReview);
	}

	/**
	 * 리뷰 오너 검증
	 */
	private void validateReviewOwner(Review savedReview, Long memberId) {

		if (!savedReview.getReviewer().getId().equals(memberId)) {

			throw new GlobalException(ResultCode.OTHER_REVIEW);
		}
	}

	/**
	 * 셀프 리뷰 검증
	 */
	private void validateSelfReview(Long reviewerId, Long revieweeId) {

		if (reviewerId.equals(revieweeId)) {

			throw new GlobalException(ResultCode.SELF_REVIEW);
		}
	}

	/**
	 * 리뷰 받는 회원 아이디 검증
	 */
	private void validateRevieweeId(Long revieweeId) {

		if (!memberRepository.existsById(revieweeId)) {

			throw new GlobalException(ResultCode.MEMBER_NOT_FOUND);
		}
	}
}
