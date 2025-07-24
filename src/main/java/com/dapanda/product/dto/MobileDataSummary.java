package com.dapanda.product.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MobileDataSummary extends ProductSummary {

	private float remainAmount;
	private int pricePer100MB;
	private boolean splitType;
	private LocalDateTime updatedAt;

	public MobileDataSummary(Long id, int price, Long itemId, String memberName,
			String profileImageUrl, float remainAmount,
			int pricePer100MB, boolean splitType, LocalDateTime updatedAt) {

		super(id, price, itemId, memberName, profileImageUrl);
		this.remainAmount = remainAmount;
		this.pricePer100MB = pricePer100MB;
		this.splitType = splitType;
		this.updatedAt = updatedAt;
	}
}
