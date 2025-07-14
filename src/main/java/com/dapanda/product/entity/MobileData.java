package com.dapanda.product.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private int dataAmount;

	private int remainAmount;

	@Enumerated(EnumType.STRING)
	private DataSellingUnit unit;

	private int pricePer100MB;

	private boolean isSplitType;

	public static MobileData of(int dataAmount, int remainAmount, DataSellingUnit unit,
			int pricePer100MB, boolean isSplitType) {

		return MobileData.builder()
				.dataAmount(dataAmount)
				.remainAmount(remainAmount)
				.unit(unit)
				.pricePer100MB(pricePer100MB)
				.isSplitType(isSplitType)
				.build();
	}
}
