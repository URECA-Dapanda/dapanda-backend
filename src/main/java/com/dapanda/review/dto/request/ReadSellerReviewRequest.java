package com.dapanda.review.dto.request;

public record ReadSellerReviewRequest(

		Long cursorId,
		Integer size,
		String reviewSortOption,
		Long sellerId) {
}
