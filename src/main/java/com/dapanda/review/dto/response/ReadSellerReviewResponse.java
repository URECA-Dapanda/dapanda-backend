package com.dapanda.review.dto.response;

import com.dapanda.product.entity.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReadSellerReviewResponse {

	private Long reviewId;
	private Long reviewerId;
	private String reviewerName;
	private Float rating;
	private String comment;
	private Float dataAmount;
	private Integer timeAmount;
	private ItemType itemType;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static ReadSellerReviewResponse of(
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

		return ReadSellerReviewResponse.builder()
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
