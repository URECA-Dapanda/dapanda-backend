package com.dapanda.trade.entity;

import com.dapanda.member.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

public class TradeFixture {

	public static Trade createTradeMobileDataDefault(Member member) {

		return Trade.of(
				0.5F,
				null,
				1000,
				TradeType.PURCHASE_SINGLE,
				member
		);
	}

	public static Trade createTrade1WithId(Member member, Long tradeId) {

		Trade trade = createTradeMobileDataDefault(member);

		ReflectionTestUtils.setField(trade, "id", tradeId);

		return trade;
	}
}
