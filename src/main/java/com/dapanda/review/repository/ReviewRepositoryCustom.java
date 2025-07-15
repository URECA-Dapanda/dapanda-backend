package com.dapanda.review.repository;

import com.dapanda.review.dto.request.ReadReviewRequest;
import com.dapanda.review.dto.response.ReadReceivedReviewResponse;
import com.dapanda.review.dto.response.ReadWrittenReviewResponse;

import java.util.List;

public interface ReviewRepositoryCustom {

	List<ReadReceivedReviewResponse> findReceivedReviews(ReadReviewRequest request);

	List<ReadWrittenReviewResponse> findWrittenReviews(ReadReviewRequest request);
}
