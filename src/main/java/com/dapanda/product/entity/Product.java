package com.dapanda.product.entity;

import com.dapanda.common.entity.BaseEntity;
import com.dapanda.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Getter
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Enumerated(EnumType.STRING)
	private ProductState state;

	private int price;

	private Long productId;

	@Enumerated(EnumType.STRING)
	private ProductType type;

	private LocalDateTime startTime;

	private LocalDateTime endTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;
}
