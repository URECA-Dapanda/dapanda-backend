package com.dapanda.product.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MobileData {

	public static final float MAX_TRANSFERABLE_DATA_AMOUNT = 2.0F; // 데이터 전송 제한 정책

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private float dataAmount;
	private float remainAmount;
	private int pricePer100MB;
	private boolean isSplitType;

	public static MobileData of(float dataAmount, float remainAmount, int pricePer100MB,
			boolean isSplitType) {

		return MobileData.builder()
				.dataAmount(dataAmount)
				.remainAmount(remainAmount)
				.pricePer100MB(pricePer100MB)
				.isSplitType(isSplitType)
				.build();
	}

	public static MobileData singleOf(float dataAmount, int totalPrice, boolean isSplitType) {

		int pricePer100MB = (int) Math.ceil(totalPrice / dataAmount * 100);

		return MobileData.builder()
				.dataAmount(dataAmount)
				.remainAmount(dataAmount)
				.pricePer100MB(pricePer100MB)
				.isSplitType(isSplitType)
				.build();
	}

	public void updateMobileData(float changedAmount, int pricePer100MB, boolean isSplitType) {

		this.dataAmount += changedAmount;
		this.remainAmount += changedAmount;
		this.pricePer100MB = pricePer100MB;
		this.isSplitType = isSplitType;
	}

	public void deductRemainAmount(float dataAmount) {

		this.remainAmount -= dataAmount;
	}
}
