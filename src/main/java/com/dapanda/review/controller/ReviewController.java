package com.dapanda.review.controller;

import com.dapanda.common.exception.CommonResponse;
import com.dapanda.review.dto.request.SaveReviewRequest;
import com.dapanda.review.dto.response.SaveReviewResponse;
import com.dapanda.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping("/reviews")
	public CommonResponse<SaveReviewResponse> saveReview(@RequestBody @Valid SaveReviewRequest request,@RequestParam Long memberId) {

		return CommonResponse.success(reviewService.saveReview(request, memberId));
	}
}
