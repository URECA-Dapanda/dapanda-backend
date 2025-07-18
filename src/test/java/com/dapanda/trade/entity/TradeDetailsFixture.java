package com.dapanda.trade.entity;

import com.dapanda.product.entity.Product;

public class TradeDetailsFixture {

	public static TradeDetails createTradeDetails(Product product, Trade trade) {

		return TradeDetails.of(
				product,
				trade
		);
	}
}
