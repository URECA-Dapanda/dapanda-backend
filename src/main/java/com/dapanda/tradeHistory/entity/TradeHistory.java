package com.dapanda.tradeHistory.entity;

import com.dapanda.common.entity.CreatedAtEntity;
import com.dapanda.member.entity.Member;
import com.dapanda.product.entity.ProductType;
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
public class TradeHistory extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private int tradingAmount;

	private int tradingPrice;

	private Long productId;

	private ProductType type;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;
}
