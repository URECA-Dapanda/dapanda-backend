package com.dapanda.trade.entity;

import com.dapanda.member.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

public class TradeFixture {

	public static Trade createTrade1(Member member) {

		return Trade.of(
				0.5F,
				null,
				1000,
				member
		);
	}

	public static Trade createTrade1WithId(Member member, Long tradeId) {

		Trade trade = createTrade1(member);

		ReflectionTestUtils.setField(trade, "id", tradeId);

		return trade;
	}
}
