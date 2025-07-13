package com.dapanda.product.dto.request;

import lombok.Getter;

@Getter
public class MobileDataCursorRequest extends ProductCursorRequest {

	private final Integer dataAmount;

	public MobileDataCursorRequest(String itemType, Long cursorId, Integer size,
			String productSortOption, Integer dataAmount) {

		super(cursorId, size, productSortOption);
		this.dataAmount = dataAmount;
	}
}
