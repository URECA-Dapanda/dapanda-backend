package com.dapanda.review.dto.response;

import com.dapanda.product.entity.ItemType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReadSellerReviewResponse {

	//Review
	private Long reviewId;
	private Long reviewerId;
	private Float rating;
	private String comment;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	//Member
	private String reviewerName;
	//Trade
	private Long tradeId;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Float dataAmount;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Integer timeAmount;
	//Product
	private Long productId;
	private ItemType itemType;


	public static ReadSellerReviewResponse of(
			Long reviewId,
			Long reviewerId,
			Float rating,
			String comment,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			String reviewerName,
			Long tradeId,
			Float dataAmount,
			Integer timeAmount,
			Long productId,
			ItemType itemType
	) {

		return ReadSellerReviewResponse.builder()
				.reviewId(reviewId)
				.reviewerId(reviewerId)
				.rating(rating)
				.comment(comment)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.reviewerName(reviewerName)
				.tradeId(tradeId)
				.dataAmount(dataAmount)
				.timeAmount(timeAmount)
				.productId(productId)
				.itemType(itemType)
				.build();
	}
}
