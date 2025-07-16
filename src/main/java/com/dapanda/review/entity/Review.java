package com.dapanda.review.entity;

import com.dapanda.common.entity.BaseEntity;
import com.dapanda.review.dto.request.UpdateReviewRequest;
import com.dapanda.trade.entity.Trade;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private float rating;

	private String comment;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trade_id", unique = true)
	private Trade trade;

	public void updateReview(UpdateReviewRequest request) {

		this.rating = request.rating();
		this.comment = request.comment();
	}

	public static Review of(float rating, String comment, Trade trade) {

		return Review.builder()
				.rating(rating)
				.comment(comment)
				.trade(trade)
				.build();
	}
}
