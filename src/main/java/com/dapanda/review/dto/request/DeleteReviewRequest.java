package com.dapanda.review.dto.request;

import jakarta.validation.constraints.NotNull;

public record DeleteReviewRequest(
		@NotNull(message = "삭제할 리뷰의 아이디는 필수입니다.")
		Long reviewId) {
}
