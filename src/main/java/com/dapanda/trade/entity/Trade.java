package com.dapanda.trade.entity;

import com.dapanda.common.entity.CreatedAtEntity;
import com.dapanda.member.entity.Member;
import com.dapanda.product.entity.ItemType;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Trade extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private int tradingAmount;

	private int tradingPrice;

	private Long productId;

	private ItemType type;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;
}
