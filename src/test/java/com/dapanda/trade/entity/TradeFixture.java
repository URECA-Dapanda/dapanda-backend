package com.dapanda.trade.entity;

import com.dapanda.member.entity.Member;
import com.dapanda.product.entity.Product;
import org.springframework.test.util.ReflectionTestUtils;

public class TradeFixture {

	public static Trade createTrade1(Product product, Member member) {

		return Trade.of(
				0.5F,
				null,
				1000,
				product,
				member
		);
	}

	public static Trade createTrade1WithId(Product product, Member member, Long tradeId) {

		Trade trade = createTrade1(product, member);

		ReflectionTestUtils.setField(trade, "id", tradeId);

		return trade;
	}
}
