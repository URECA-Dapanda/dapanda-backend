package com.dapanda.review.dto.request;

public record ReadReviewRequest(

		Long cursorId,
		Integer size,
		String reviewSortOption,
		Long memberId) {
}
