package com.dapanda.product.entity;

import com.dapanda.product.dto.MobileDataSummary;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class MobileDataFixture {

	public static MobileData createMobileData(BigDecimal dataAmount, BigDecimal remainAmount,
			int pricePer100MB) {

		return MobileData.of(
				dataAmount,
				remainAmount,
				pricePer100MB,
				false
		);
	}

	public static MobileData createMobileDataWithId(Long mobileDataId, BigDecimal dataAmount,
			BigDecimal remainAmount, int pricePer100MB) {

		MobileData mobileData = MobileData.of(
				dataAmount,
				remainAmount,
				pricePer100MB,
				false
		);

		ReflectionTestUtils.setField(mobileData, "id", mobileDataId);

		return mobileData;
	}

	public static MobileData createMobileDataSplitType(BigDecimal dataAmount,
			BigDecimal remainAmount,
			int pricePer100MB) {

		return MobileData.of(
				dataAmount,
				remainAmount,
				pricePer100MB,
				true
		);
	}

	public static MobileData createMobileDataSplitTypeWithId(Long mobileDataId,
			BigDecimal dataAmount,
			BigDecimal remainAmount,
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
				MobileData.of(BigDecimal.valueOf(1001), BigDecimal.valueOf(500), 100, false),
				MobileData.of(BigDecimal.valueOf(1002), BigDecimal.valueOf(500), 100, false),
				MobileData.of(BigDecimal.valueOf(1003), BigDecimal.valueOf(500), 100, false),
				MobileData.of(BigDecimal.valueOf(1004), BigDecimal.valueOf(500), 100, false),
				MobileData.of(BigDecimal.valueOf(1005), BigDecimal.valueOf(500), 100, false),
				MobileData.of(BigDecimal.valueOf(1006), BigDecimal.valueOf(500), 100, false),
				MobileData.of(BigDecimal.valueOf(1007), BigDecimal.valueOf(500), 100, false),
				MobileData.of(BigDecimal.valueOf(1008), BigDecimal.valueOf(500), 100, false),
				MobileData.of(BigDecimal.valueOf(1009), BigDecimal.valueOf(500), 100, false),
				MobileData.of(BigDecimal.valueOf(1010), BigDecimal.valueOf(500), 100, false),
				MobileData.of(BigDecimal.valueOf(1011), BigDecimal.valueOf(500), 100, false)
		);
	}

	public static MobileDataSummary createMobileDataSummary(Long id, int price, Long itemId,
			String memberName, BigDecimal remainAmount, int pricePer100MB, boolean isSplitType,
			LocalDateTime updatedAt) {

		return new MobileDataSummary(
				id,
				price,
				itemId,
				memberName,
				"profile.jpg",
				remainAmount,
				pricePer100MB,
				isSplitType,
				updatedAt
		);
	}
}
