package com.dapanda.trade.entity;

import com.dapanda.common.entity.CreatedAtEntity;
import com.dapanda.member.entity.Member;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private BigDecimal dataAmount;

	private Integer timeAmount;

	private int tradingPrice;

	@Enumerated(EnumType.STRING)
	private TradeType tradeType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	public static Trade of(BigDecimal dataAmount, Integer timeAmount, int tradingPrice,
			TradeType tradeType, Member member) {

		return Trade.builder()
				.dataAmount(dataAmount)
				.timeAmount(timeAmount)
				.tradingPrice(tradingPrice)
				.tradeType(tradeType)
				.member(member)
				.build();
	}

	public static Trade of(BigDecimal dataAmount, int tradingPrice, TradeType tradeType,
			Member member) {

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

	public static Trade of(int tradingPrice, TradeType tradeType, Member member) {

		return Trade.builder()
				.tradingPrice(tradingPrice)
				.tradeType(tradeType)
				.member(member)
				.build();
	}
}
