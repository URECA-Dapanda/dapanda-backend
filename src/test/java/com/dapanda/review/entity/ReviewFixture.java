package com.dapanda.review.entity;

import com.dapanda.member.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

public class ReviewFixture {

	public static Review createReview(Float rating, String comment, Long productId, Member reviewer, Member reviewee) {

		return Review.of(rating, comment, productId, reviewer, reviewee);
	}

	public static Review createReviewWithId(Float rating, String comment, Long productId, Member reviewer, Member reviewee, Long reviewId) {

		Review review = Review.of(rating, comment, productId, reviewer, reviewee);

		ReflectionTestUtils.setField(review, "id", reviewId);

		return review;
	}
}
