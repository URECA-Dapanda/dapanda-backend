package com.dapanda.review.repository;

import com.dapanda.review.dto.request.ReadSellerReviewRequest;
import com.dapanda.review.dto.response.ReadSellerReviewResponse;

import java.util.List;

public interface ReviewRepositoryCustom {

	List<ReadSellerReviewResponse> findSellerReviewWithCursor(ReadSellerReviewRequest request);
}
