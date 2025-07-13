package com.dapanda.review.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.review.dto.request.DeleteReviewRequest;
import com.dapanda.review.dto.request.SaveReviewRequest;
import com.dapanda.review.dto.response.SaveReviewResponse;
import com.dapanda.review.entity.Review;
import com.dapanda.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final MemberRepository memberRepository;

	public SaveReviewResponse saveReview(SaveReviewRequest request, Long memberId) {

		validateSelfReview(memberId, request.revieweeId());
		validateMemberId(request.revieweeId());

		Member reviewer = memberRepository.getReferenceById(memberId);
		Member reviewee = memberRepository.getReferenceById(request.revieweeId());

		Review review = Review.of(request.rating(), request.comment(), request.productId(), reviewer, reviewee);

		Review savedReview = reviewRepository.save(review);

		return SaveReviewResponse.from(savedReview.getId());
	}

	public void deleteReview(DeleteReviewRequest request, Long memberId) {

		validateMemberId(memberId);
		validateReviewOwner(request.reviewId(), memberId);
		validateReviewId(request.reviewId());

		reviewRepository.deleteById(request.reviewId());
	}

	/**
	 *	리뷰 오너 검증
	 */
	private void validateReviewOwner(Long reviewId, Long memberId) {

		Review review = reviewRepository.getReferenceById(reviewId);

		Member member = review.getReviewer();

		if (!member.getId().equals(memberId)) {

			throw new GlobalException(ResultCode.OTHER_REVIEW);
		}
	}

	/**
	 * 셀프 리뷰 검증
	 */
	private void validateSelfReview(Long reviewerId, Long revieweeId){

		if (reviewerId.equals(revieweeId)){

			throw new GlobalException(ResultCode.SELF_REVIEW);
		}
	}

	/**
	 *	memberId 검증
	 */
	private void validateMemberId(Long memberId){

		if (!memberRepository.existsById(memberId)){

			throw new GlobalException(ResultCode.MEMBER_NOT_FOUND);
		}
	}

	/**
	 * reviewId 검증
	 */
	private void validateReviewId(Long reviewId){

		if (!reviewRepository.existsById(reviewId)){

			throw new GlobalException(ResultCode.REVIEW_NOT_FOUND);
		}
	}
}
