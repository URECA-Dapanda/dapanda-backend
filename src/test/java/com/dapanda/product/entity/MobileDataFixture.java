package com.dapanda.product.entity;

import com.dapanda.product.dto.MobileDataSummary;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

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

	public static MobileData createMobileDataWithId(Long mobileDataId, float dataAmount,
			float remainAmount, int pricePer100MB) {

		MobileData mobileData = MobileData.of(
				dataAmount,
				remainAmount,
				pricePer100MB,
				false
		);

		ReflectionTestUtils.setField(mobileData, "id", mobileDataId);

		return mobileData;
	}

	public static MobileData createMobileDataSplitType(float dataAmount, float remainAmount,
			int pricePer100MB) {

		return MobileData.of(
				dataAmount,
				remainAmount,
				pricePer100MB,
				true
		);
	}

	public static MobileData createMobileDataSplitTypeWithId(Long mobileDataId, float dataAmount,
			float remainAmount,
			int pricePer100MB) {

		MobileData mobileData = MobileData.of(
				dataAmount,
				remainAmount,
				pricePer100MB,
				true
		);

		ReflectionTestUtils.setField(mobileData, "id", mobileDataId);

		return mobileData;
	}

	public static List<MobileData> createMobileDataList() {

		return List.of(
				MobileData.of(1001, 500, 100, false),
				MobileData.of(1002, 500, 100, false),
				MobileData.of(1003, 500, 100, false),
				MobileData.of(1004, 500, 100, false),
				MobileData.of(1005, 500, 100, false),
				MobileData.of(1006, 500, 100, false),
				MobileData.of(1007, 500, 100, false),
				MobileData.of(1008, 500, 100, false),
				MobileData.of(1009, 500, 100, false),
				MobileData.of(1010, 500, 100, false),
				MobileData.of(1011, 500, 100, false)
		);
	}

	public static MobileDataSummary createMobileDataSummary(Long id, int price, Long itemId,
			String memberName, float remainAmount, int pricePer100MB, boolean isSplitType,
			LocalDateTime updatedAt) {

		return new MobileDataSummary(
				id,
				price,
				itemId,
				memberName,
				remainAmount,
				pricePer100MB,
				isSplitType,
				updatedAt
		);
	}
}
