package com.dapanda.review.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.review.dto.request.DeleteReviewRequest;
import com.dapanda.review.dto.request.SaveReviewRequest;
import com.dapanda.review.dto.request.UpdateReviewRequest;
import com.dapanda.review.dto.response.SaveReviewResponse;
import com.dapanda.review.dto.response.UpdateReviewResponse;
import com.dapanda.review.service.ReviewService;
import jakarta.validation.Valid;
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
