package com.dapanda.review.dto.response;

import com.dapanda.product.entity.ItemType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 내가 작성한 리뷰 조회 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadMyWrittenReviewResponse {

	private Long reviewId;
	private Long revieweeId;
	private String revieweeName;
	private Float rating;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Float dataAmount;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Integer timeAmount;
	private ItemType itemType;
	private String comment;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public ReadMyWrittenReviewResponse(
			Long reviewId,
			Long revieweeId,
			String revieweeName,
			Float rating,
			String comment,
			Float dataAmount,
			Integer timeAmount,
			ItemType itemType,
			LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		this.reviewId = reviewId;
		this.revieweeId = revieweeId;
		this.revieweeName = revieweeName;
		this.rating = rating;
		this.comment = comment;
		this.dataAmount = dataAmount;
		this.timeAmount = timeAmount;
		this.itemType = itemType;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static ReadMyWrittenReviewResponse of(
			Long reviewId,
			Long revieweeId,
			String revieweeName,
			Float rating,
			String comment,
			Float dataAmount,
			Integer timeAmount,
			ItemType itemType,
			LocalDateTime createdAt,
			LocalDateTime updatedAt) {

		return ReadMyWrittenReviewResponse.builder()
				.reviewId(reviewId)
				.revieweeId(revieweeId)
				.revieweeName(revieweeName)
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
