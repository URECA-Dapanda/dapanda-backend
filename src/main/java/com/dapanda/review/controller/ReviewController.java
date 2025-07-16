package com.dapanda.review.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.review.dto.request.DeleteReviewRequest;
import com.dapanda.review.dto.request.ReadReviewRequest;
import com.dapanda.review.dto.request.SaveReviewRequest;
import com.dapanda.review.dto.request.UpdateReviewRequest;
import com.dapanda.review.dto.response.*;
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
	 * 리뷰 단건 조회
	 */
	@GetMapping("/reviews/{reviewId}")
	public CommonResponse<ReadReviewResponse> readReview(
			@PathVariable Long reviewId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(reviewService.readReview(reviewId, userDetails.getId()));
	}

	/**
	 * 회원이 받은 리뷰 조회
	 */
	@GetMapping("/reviews/rc/{memberId}")
	public CommonResponse<CursorPageResponse<ReadReceivedReviewResponse>> readMyReceivedReview(
			@PathVariable(name = "memberId") Long memberId,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) Integer size,
			@RequestParam(defaultValue = "RECENT") String reviewSortOption) {

		ReadReviewRequest request = new ReadReviewRequest(cursorId, size, reviewSortOption, memberId);

		return CommonResponse.success(reviewService.readReceivedReview(request));
	}

	/**
	 * 회원이 작성한 리뷰 조회
	 */
	@GetMapping("/reviews/wt")
	public CommonResponse<CursorPageResponse<ReadWrittenReviewResponse>> readMyWrittenReview(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) Integer size,
			@RequestParam(defaultValue = "RECENT") String reviewSortOption) {

		ReadReviewRequest request = new ReadReviewRequest(cursorId, size, reviewSortOption, userDetails.getId());

		return CommonResponse.success(reviewService.readWrittenReview(request));
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
