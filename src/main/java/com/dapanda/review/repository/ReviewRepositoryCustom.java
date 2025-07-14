package com.dapanda.review.repository;

import com.dapanda.review.dto.request.ReadReviewRequest;
import com.dapanda.review.dto.response.ReadReviewResponse;

import java.util.List;

public interface ReviewRepositoryCustom {

	List<ReadReviewResponse> findSellerReviewWithCursor(ReadReviewRequest request);
}
