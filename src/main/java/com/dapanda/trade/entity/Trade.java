package com.dapanda.trade.entity;

import com.dapanda.common.entity.CreatedAtEntity;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Float dataAmount;

	private Integer timeAmount;

	private int tradingPrice;

	@Enumerated(EnumType.STRING)
	private TradeType tradeType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	public static Trade of(Float dataAmount, Integer timeAmount, int tradingPrice,
			TradeType tradeType, Member member) {

		return Trade.builder()
				.dataAmount(dataAmount)
				.timeAmount(timeAmount)
				.tradingPrice(tradingPrice)
				.tradeType(tradeType)
				.member(member)
				.build();
	}

	public static Trade of(Float dataAmount, int tradingPrice, TradeType tradeType, Member member) {

		return Trade.builder()
				.dataAmount(dataAmount)
				.tradingPrice(tradingPrice)
				.tradeType(tradeType)
				.member(member)
				.build();
	}

	public static Trade of(Integer timeAmount, int tradingPrice, TradeType tradeType,
			Member member) {

		return Trade.builder()
				.timeAmount(timeAmount)
				.tradingPrice(tradingPrice)
				.tradeType(tradeType)
				.member(member)
				.build();
	}
}
