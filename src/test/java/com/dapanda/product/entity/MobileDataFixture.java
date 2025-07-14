package com.dapanda.product.entity;

import com.dapanda.product.dto.MobileDataSummary;

public class MobileDataFixture {

	public static MobileData createMobileData(float dataAmount, float remainAmount,
			int pricePer100MB) {

		return MobileData.of(
				dataAmount,
				remainAmount,
				pricePer100MB,
				false
		);
	}

	public static MobileDataSummary createMobileDataSummary(Long id, int price, Long itemId,
			String memberName, float remainAmount, int pricePer100MB, boolean isSplitType) {

		return new MobileDataSummary(
				id,
				price,
				itemId,
				memberName,
				remainAmount,
				pricePer100MB,
				isSplitType
		);
	}
}
