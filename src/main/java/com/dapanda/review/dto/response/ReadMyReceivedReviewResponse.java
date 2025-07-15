package com.dapanda.review.dto.response;

import com.dapanda.product.entity.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 내가 받은 리뷰 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadMyReceivedReviewResponse {

	private Long reviewId;
	private Long reviewerId;
	private String reviewerName;
	private Float rating;
	private Float dataAmount;
	private Integer timeAmount;
	private ItemType itemType;
	private String comment;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public ReadMyReceivedReviewResponse(
			Long reviewId,
			Long reviewerId,
			String reviewerName,
			Float rating,
			String comment,
			Float dataAmount,
			Integer timeAmount,
			ItemType itemType,
			LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		this.reviewId = reviewId;
		this.reviewerId = reviewerId;
		this.reviewerName = reviewerName;
		this.rating = rating;
		this.comment = comment;
		this.dataAmount = dataAmount;
		this.timeAmount = timeAmount;
		this.itemType = itemType;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static ReadMyReceivedReviewResponse of(
			Long reviewId,
			Long reviewerId,
			String reviewerName,
			Float rating,
			String comment,
			Float dataAmount,
			Integer timeAmount,
			ItemType itemType,
			LocalDateTime createdAt,
			LocalDateTime updatedAt) {

		return ReadMyReceivedReviewResponse.builder()
				.reviewId(reviewId)
				.reviewerId(reviewerId)
				.reviewerName(reviewerName)
				.rating(rating)
				.comment(comment)
				.dataAmount(dataAmount)
				.timeAmount(timeAmount)
				.itemType(itemType)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();
	}
}
