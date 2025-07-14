package com.dapanda.review.entity;

import com.dapanda.member.entity.MemberFixture;
import org.springframework.test.util.ReflectionTestUtils;

public class ReviewFixture {

	public static Review createReview1(Long reviewId, Long reviewerId, Long revieweeId) {

		Review review = Review.of(
				3.5f,
				"comment",
				123L,
				MemberFixture.createMember1(reviewerId),
				MemberFixture.createMember2(revieweeId)
		);

		ReflectionTestUtils.setField(review, "id", reviewId);

		return review;
	}
}
