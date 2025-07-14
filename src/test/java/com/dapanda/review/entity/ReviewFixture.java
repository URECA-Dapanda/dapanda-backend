package com.dapanda.review.entity;

import com.dapanda.member.entity.Member;

public class ReviewFixture {

	public static Review createReview1(Member reviewer, Member reviewee) {

		return Review.of(
				3.5f,
				"comment",
				123L,
				reviewer,
				reviewee
		);
	}
}
