package com.dapanda.trade.entity;

import com.dapanda.common.entity.CreatedAtEntity;
import com.dapanda.member.entity.Member;
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

	private Float dataAmount;

	private Integer timeAmount;

	private int tradingPrice;

	private Long productId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	public static Trade of(Float dataAmount, Integer timeAmount, int tradingPrice, Long productId, Member member) {

		return Trade.builder()
				.dataAmount(dataAmount)
				.timeAmount(timeAmount)
				.tradingPrice(tradingPrice)
				.productId(productId)
				.member(member)
				.build();
	}
}
