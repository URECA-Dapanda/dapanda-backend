package com.dapanda.review.entity;

import com.dapanda.common.entity.BaseEntity;
import com.dapanda.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Entity
@Getter
public class Review extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private double rating;

	private String comment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewer_id")
	private Member reviewer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewee_id")
	private Member reviewee;
}
