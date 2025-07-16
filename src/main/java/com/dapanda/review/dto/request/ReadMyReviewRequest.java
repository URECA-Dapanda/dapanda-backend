package com.dapanda.review.dto.request;

public record ReadMyReviewRequest(

		Long cursorId,
		Integer size,
		String reviewSortOption,
		Long memberId) {
}
