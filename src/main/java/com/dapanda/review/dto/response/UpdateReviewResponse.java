package com.dapanda.review.dto.response;

import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateReviewResponse {

	private Long reviewId;

	public static UpdateReviewResponse from(Long reviewId) {

		return UpdateReviewResponse.builder()
				.reviewId(reviewId)
				.build();
	}
}
