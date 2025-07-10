package com.dapanda.review.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
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
		validateRevieweeId(request.revieweeId());

		Member reviewer = memberRepository.getReferenceById(memberId);
		Member reviewee = memberRepository.getReferenceById(request.revieweeId());

		Review review = Review.of(request.rating(), request.comment(), request.productId(), reviewer, reviewee);

		Review savedReview = reviewRepository.save(review);

		return SaveReviewResponse.from(savedReview.getId());
	}

	private void validateSelfReview(Long reviewerId, Long revieweeId){

		if (reviewerId.equals(revieweeId)){

			throw new GlobalException(ResultCode.SELF_REVIEW);
		}
	}

	private void validateRevieweeId(Long revieweeId){

		if (!memberRepository.existsById(revieweeId)){

			throw new GlobalException(ResultCode.MEMBER_NOT_FOUND);
		}
	}
}
