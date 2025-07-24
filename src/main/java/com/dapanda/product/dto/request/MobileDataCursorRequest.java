package com.dapanda.product.dto.request;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class MobileDataCursorRequest extends ProductCursorRequest {

	private final BigDecimal dataAmount;

	public MobileDataCursorRequest(Long cursorId, Integer size,
			String productSortOption, BigDecimal dataAmount) {

		super(cursorId, size, productSortOption);
		this.dataAmount = dataAmount;
	}
}
