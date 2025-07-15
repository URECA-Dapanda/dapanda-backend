package com.dapanda.trade.entity;

import com.dapanda.common.entity.CreatedAtEntity;
import com.dapanda.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Trade extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Float dataAmount;

	private Integer timeAmount;

	private int tradingPrice;

	private Long productId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;
}
