package com.dapanda.review.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.review.dto.request.DeleteReviewRequest;
import com.dapanda.review.dto.request.ReadSellerReviewRequest;
import com.dapanda.review.dto.request.SaveReviewRequest;
import com.dapanda.review.dto.request.UpdateReviewRequest;
import com.dapanda.review.dto.response.ReadSellerReviewResponse;
import com.dapanda.review.dto.response.SaveReviewResponse;
import com.dapanda.review.dto.response.UpdateReviewResponse;
import com.dapanda.review.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping("/reviews")
	public CommonResponse<SaveReviewResponse> saveReview(
			@RequestBody @Valid SaveReviewRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(reviewService.saveReview(request, userDetails.getId()));
	}

	/**
	 * 판매자가 받은 리뷰 조회
	 */
	@GetMapping("/reviews/seller/{memberId}")
	public CommonResponse<CursorPageResponse<ReadSellerReviewResponse>> readSellerReview(
			@PathVariable Long memberId,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) Integer size,
			@RequestParam(defaultValue = "RECENT") String reviewSortOption){

		ReadSellerReviewRequest request = new ReadSellerReviewRequest(cursorId, size, reviewSortOption, memberId);

		return CommonResponse.success(reviewService.readSellerReview(request));
	}

	@PutMapping("/reviews")
	public CommonResponse<UpdateReviewResponse> updateReview(
			@RequestBody @Valid UpdateReviewRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails){

		return CommonResponse.success(reviewService.updateReview(request, userDetails.getId()));
	}

	@DeleteMapping("/reviews")
	public CommonResponse<Void> deleteReview(
			@RequestBody @Valid DeleteReviewRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		reviewService.deleteReview(request, userDetails.getId());

		return CommonResponse.success(null);
	}
}
