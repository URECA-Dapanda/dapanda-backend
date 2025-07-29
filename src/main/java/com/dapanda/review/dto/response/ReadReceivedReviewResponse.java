package com.dapanda.review.dto.response;

import com.dapanda.product.entity.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 받은 리뷰 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class ReadReceivedReviewResponse {

	//Review
	private Long reviewId;
	private Float rating;
	private String comment;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	//Member
	private Long reviewerId;
	private String reviewerName;
	//Trade
	private Long tradeId;
	private Integer timeAmount;
	//Product
	private Long productId;
	private ItemType itemType;
	//Wifi
	private String title;

	public static ReadReceivedReviewResponse of(
			Long reviewId,
			Float rating,
			String comment,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			Long reviewerId,
			String reviewerName,
			Long tradeId,
			Integer timeAmount,
			Long productId,
			ItemType itemType
	) {

		return ReadReceivedReviewResponse.builder()
				.reviewId(reviewId)
				.rating(rating)
				.comment(comment)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.reviewerId(reviewerId)
				.reviewerName(reviewerName)
				.tradeId(tradeId)
				.timeAmount(timeAmount)
				.productId(productId)
				.itemType(itemType)
				.build();
	}
}
