package com.dapanda.product.dto.request;

import lombok.Getter;

@Getter
public class MobileDataCursorRequest extends ProductCursorRequest {

	private final Float dataAmount;

	public MobileDataCursorRequest(Long cursorId, Integer size,
			String productSortOption, Float dataAmount) {

		super(cursorId, size, productSortOption);
		this.dataAmount = dataAmount;
	}
}
