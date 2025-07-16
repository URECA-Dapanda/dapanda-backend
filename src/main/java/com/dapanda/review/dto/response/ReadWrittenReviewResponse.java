package com.dapanda.review.dto.response;

import com.dapanda.product.entity.ItemType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 내가 작성한 리뷰 조회 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class ReadWrittenReviewResponse {

	//Review
	private Long reviewId;
	private Float rating;
	private String comment;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	//Member
	private Long revieweeId;
	private String revieweeName;
	//Trade
	private Long tradeId;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Float dataAmount;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Integer timeAmount;
	//Product
	private Long productId;
	private ItemType itemType;

	public static ReadWrittenReviewResponse of(
			Long reviewId,
			Float rating,
			String comment,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			Long revieweeId,
			String revieweeName,
			Long tradeId,
			Float dataAmount,
			Integer timeAmount,
			Long productId,
			ItemType itemType
	) {

		return ReadWrittenReviewResponse.builder()
				.reviewId(reviewId)
				.rating(rating)
				.comment(comment)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.revieweeId(revieweeId)
				.revieweeName(revieweeName)
				.tradeId(tradeId)
				.dataAmount(dataAmount)
				.timeAmount(timeAmount)
				.productId(productId)
				.itemType(itemType)
				.build();
	}
}
