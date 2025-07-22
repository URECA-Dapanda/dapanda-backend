package com.dapanda.review.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.review.dto.request.CreateReviewRequest;
import com.dapanda.review.dto.request.ReadReviewRequest;
import com.dapanda.review.dto.request.UpdateReviewRequest;
import com.dapanda.review.dto.response.CreateReviewResponse;
import com.dapanda.review.dto.response.ReadReceivedReviewResponse;
import com.dapanda.review.dto.response.ReadReviewResponse;
import com.dapanda.review.dto.response.ReadWrittenReviewResponse;
import com.dapanda.review.dto.response.UpdateReviewResponse;
import com.dapanda.review.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	/**
	 * 리뷰 등록
	 */
	@PostMapping("/trades/{tradeId}/reviews")
	public ResponseEntity<CommonResponse<CreateReviewResponse>> saveReview(
			@PathVariable Long tradeId,
			@RequestBody @Valid CreateReviewRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ResponseEntity.ok(CommonResponse.success(
				reviewService.createReview(tradeId, request, userDetails.getId())));
	}

	/**
	 * 리뷰 단건 조회
	 */
	@GetMapping("/reviews/{reviewId}")
	public ResponseEntity<CommonResponse<ReadReviewResponse>> readReview(
			@PathVariable Long reviewId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ResponseEntity.ok(
				CommonResponse.success(reviewService.readReview(reviewId, userDetails.getId())));
	}

	/**
	 * 회원이 받은 리뷰 조회
	 */
	@GetMapping("/members/{memberId}/reviews/received")
	public ResponseEntity<CommonResponse<CursorPageResponse<ReadReceivedReviewResponse>>> readMyReceivedReview(
			@PathVariable(name = "memberId") Long memberId,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) Integer size,
			@RequestParam(defaultValue = "RECENT") String reviewSortOption) {

		ReadReviewRequest request = new ReadReviewRequest(cursorId, size, reviewSortOption,
				memberId);

		return ResponseEntity.ok(CommonResponse.success(reviewService.readReceivedReview(request)));
	}

	/**
	 * 내가 작성한 리뷰 조회
	 */
	@GetMapping("/reviews/wrote")
	public ResponseEntity<CommonResponse<CursorPageResponse<ReadWrittenReviewResponse>>> readMyWrittenReview(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) Integer size,
			@RequestParam(defaultValue = "RECENT") String reviewSortOption) {

		ReadReviewRequest request = new ReadReviewRequest(cursorId, size, reviewSortOption,
				userDetails.getId());

		return ResponseEntity.ok(CommonResponse.success(reviewService.readWrittenReview(request)));
	}

	/**
	 * 내가 받은 리뷰 조회
	 */
	@GetMapping("/reviews/received")
	public ResponseEntity<CommonResponse<CursorPageResponse<ReadReceivedReviewResponse>>> readMyReceivedReview(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) Integer size,
			@RequestParam(defaultValue = "RECENT") String reviewSortOption) {

		ReadReviewRequest request = new ReadReviewRequest(cursorId, size, reviewSortOption,
				userDetails.getId());

		return ResponseEntity.ok(CommonResponse.success(reviewService.readReceivedReview(request)));
	}

	/**
	 * 리뷰 일부 수정
	 */
	@PatchMapping("/reviews/{reviewId}")
	public ResponseEntity<CommonResponse<UpdateReviewResponse>> updateReview(
			@PathVariable Long reviewId,
			@RequestBody @Valid UpdateReviewRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ResponseEntity.ok(CommonResponse.success(
				reviewService.updateReview(reviewId, request, userDetails.getId())));
	}

	/**
	 * 리뷰 삭제
	 */
	@DeleteMapping("/reviews/{reviewId}")
	public ResponseEntity<CommonResponse<Void>> deleteReview(
			@PathVariable Long reviewId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		reviewService.deleteReview(reviewId, userDetails.getId());

		return ResponseEntity.ok(CommonResponse.success(null));
	}
}
