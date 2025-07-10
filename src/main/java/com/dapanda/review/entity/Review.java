package com.dapanda.review.entity;

import com.dapanda.common.entity.BaseEntity;
import com.dapanda.member.entity.Member;
import com.dapanda.product.entity.ItemType;
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

	private Long productId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewer_id")
	private Member reviewer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewee_id")
	private Member reviewee;

	public static Review of(float rating, String comment, Long productId, Member reviewer, Member reviewee) {

		return Review.builder()
				.rating(rating)
				.comment(comment)
				.productId(productId)
				.reviewer(reviewer)
				.reviewee(reviewee)
				.build();
	}
}
