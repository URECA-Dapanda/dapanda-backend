package com.dapanda.review.dto.response;

import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateReviewResponse {

	private Long reviewId;

	public static CreateReviewResponse from(Long reviewId) {

		return CreateReviewResponse.builder()
				.reviewId(reviewId)
				.build();
	}
}
