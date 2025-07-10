package com.dapanda.review.dto.response;

import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SaveReviewResponse {

	private Long reviewId;

	public static SaveReviewResponse from(Long reviewId) {

		return SaveReviewResponse.builder()
				.reviewId(reviewId)
				.build();
	}
}
