package com.dapanda.review.dto.request;

import jakarta.validation.constraints.NotNull;

public record DeleteReviewRequest(
		@NotNull
		Long reviewId) {
}
