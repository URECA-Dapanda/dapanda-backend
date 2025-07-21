package com.dapanda.review.dto.response;

public record ReviewStatsResponse(
		int reviewCount,
		float averageRating
) {

}
