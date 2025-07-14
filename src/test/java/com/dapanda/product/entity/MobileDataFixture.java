package com.dapanda.product.entity;

import com.dapanda.product.dto.MobileDataSummary;

public class MobileDataFixture {

	public static MobileData createMobileData(int dataAmount, int remainAmount,
			int pricePer100MB) {

		return MobileData.of(
				dataAmount,
				remainAmount,
				DataSellingUnit.GB,
				pricePer100MB,
				false
		);
	}

	public static MobileDataSummary createMobileDataSummary(Long id, int price, Long itemId,
			String memberName, int remainAmount, DataSellingUnit unit, int pricePer100MB,
			boolean isSplitType) {

		return new MobileDataSummary(
				id,
				price,
				itemId,
				memberName,
				remainAmount,
				unit,
				pricePer100MB,
				isSplitType
		);
	}
}
