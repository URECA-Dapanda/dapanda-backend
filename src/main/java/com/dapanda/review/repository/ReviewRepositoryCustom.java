package com.dapanda.review.repository;

import com.dapanda.review.dto.request.ReadMyReviewRequest;
import com.dapanda.review.dto.request.ReadSellerReviewRequest;
import com.dapanda.review.dto.response.ReadMyReceivedReviewResponse;
import com.dapanda.review.dto.response.ReadMyWrittenReviewResponse;
import com.dapanda.review.dto.response.ReadSellerReviewResponse;

import java.util.List;

public interface ReviewRepositoryCustom {

	List<ReadSellerReviewResponse> findSellerReviewWithCursor(ReadSellerReviewRequest request);

	List<ReadMyReceivedReviewResponse> findMyReceivedReviews(ReadMyReviewRequest request);

	List<ReadMyWrittenReviewResponse> findMyWrittenReviews(ReadMyReviewRequest request);
}
