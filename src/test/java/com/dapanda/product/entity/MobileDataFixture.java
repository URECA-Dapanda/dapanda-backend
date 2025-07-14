package com.dapanda.product.entity;

import com.dapanda.product.dto.MobileDataSummary;
import org.springframework.test.util.ReflectionTestUtils;

public class MobileDataFixture {

	public static MobileData createMobileData1(int dataAmount, int remainAmount, int pricePer100MB,
			Long mobileDataId) {

		MobileData mobileData = MobileData.of(
				dataAmount,
				remainAmount,
				DataSellingUnit.GB,
				pricePer100MB,
				false
		);

		ReflectionTestUtils.setField(mobileData, "id", mobileDataId);

		return mobileData;
	}

	public static MobileData createMobileData2(Long mobileDataId) {

		MobileData mobileData = MobileData.of(
				2,
				2,
				DataSellingUnit.GB,
				500,
				true
		);

		ReflectionTestUtils.setField(mobileData, "id", mobileDataId);

		return mobileData;
	}

	public static MobileData createMobileData3(Long mobileDataId) {

		MobileData mobileData = MobileData.of(
				1,
				1,
				DataSellingUnit.GB,
				400,
				false
		);

		ReflectionTestUtils.setField(mobileData, "id", mobileDataId);

		return mobileData;
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
