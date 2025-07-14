package com.dapanda.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MobileDataSummary extends ProductSummary {

	private float remainAmount;
	private int pricePer100MB;
	private boolean splitType;

	public MobileDataSummary(Long id, int price, Long itemId, String memberName, float remainAmount,
			int pricePer100MB, boolean splitType) {

		super(id, price, itemId, memberName);
		this.remainAmount = remainAmount;
		this.pricePer100MB = pricePer100MB;
		this.splitType = splitType;
	}
}
