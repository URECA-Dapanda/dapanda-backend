package com.dapanda.product.dto;

import com.dapanda.product.entity.DataSellingUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MobileDataSummary extends ProductSummary {

	private int remainAmount;
	private DataSellingUnit unit;
	private int pricePer100MB;
	private boolean splitType;

	public MobileDataSummary(Long id, int price, Long itemId, String memberName, int remainAmount,
			DataSellingUnit unit, int pricePer100MB, boolean splitType) {

		super(id, price, itemId, memberName);
		this.remainAmount = remainAmount;
		this.unit = unit;
		this.pricePer100MB = pricePer100MB;
		this.splitType = splitType;
	}
}
