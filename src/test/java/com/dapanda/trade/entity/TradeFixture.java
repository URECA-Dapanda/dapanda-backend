package com.dapanda.trade.entity;

import com.dapanda.member.entity.Member;
import com.dapanda.product.entity.MobileData;
import com.dapanda.product.entity.Product;
import com.dapanda.trade.dto.MobileDataScrap;
import java.math.BigDecimal;
import org.springframework.test.util.ReflectionTestUtils;

public class TradeFixture {

	public static Trade createTradeMobileDataDefault(Member member) {

		return Trade.of(
				BigDecimal.valueOf(0.5),
				1000,
				TradeType.PURCHASE_MOBILE_SINGLE,
				member
		);
	}

	public static Trade createTrade1WithId(Member member, Long tradeId) {

		Trade trade = createTradeMobileDataDefault(member);

		ReflectionTestUtils.setField(trade, "id", tradeId);

		return trade;
	}

	public static MobileDataScrap createMobileDataScrap(Product product, MobileData mobileData,
			int purchasePrice, BigDecimal purchaseDataAmount) {

		return new MobileDataScrap(product.getId(), mobileData.getId(),
				product.getMember().getName(), "", product.getPrice(), purchasePrice,
				mobileData.getRemainAmount(), purchaseDataAmount, mobileData.getPricePer100MB(),
				mobileData.isSplitType(), product.getUpdatedAt());
	}

	public static Trade createTradeWifi(Member member) {

		return Trade.of(
				30,
				1000,
				TradeType.PURCHASE_WIFI,
				member
		);
	}

	public static Trade createTradeCharge(Member member) {

		return Trade.of(
				10000,
				TradeType.CHARGE,
				member
		);
	}

	public static Trade createTradeSale(Member member) {

		return Trade.of(
				BigDecimal.valueOf(2.0),
				1000,
				TradeType.SALE_MOBILE_DATA,
				member
		);
	}
}
