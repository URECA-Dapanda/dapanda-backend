package com.dapanda.review.dto.response;

import com.dapanda.product.entity.ItemType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadReviewResponse {

	private Long reviewId;
	private Long reviewerId;
	private String reviewerName;
	private Float rating;
	private Integer tradingAmount;
	private ItemType itemType;
	private String comment;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public ReadReviewResponse(
			Long reviewId,
			Long reviewerId,
			String reviewerName,
			Float rating,
			String comment,
			Integer tradingAmount,
			ItemType itemType,
			LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		this.reviewId = reviewId;
		this.reviewerId = reviewerId;
		this.reviewerName = reviewerName;
		this.rating = rating;
		this.comment = comment;
		this.tradingAmount = tradingAmount;
		this.itemType = itemType;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static ReadReviewResponse of(
			Long reviewId,
			Long reviewerId,
			String reviewerName,
			Float rating,
			String comment,
			Integer tradingAmount,
			ItemType itemType,
			LocalDateTime createdAt,
			LocalDateTime updatedAt) {

		return ReadReviewResponse.builder()
				.reviewId(reviewId)
				.reviewerId(reviewerId)
				.reviewerName(reviewerName)
				.rating(rating)
				.comment(comment)
				.tradingAmount(tradingAmount)
				.itemType(itemType)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();
	}
}
