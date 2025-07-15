package com.dapanda.review.dto.response;

import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadReviewResponse {

	private Long reviewId;
	private float rating;
	private String comment;

	public static ReadReviewResponse of(Long reviewId, float rating, String comment){

		return ReadReviewResponse.builder()
				.reviewId(reviewId)
				.rating(rating)
				.comment(comment)
				.build();
	}
}
