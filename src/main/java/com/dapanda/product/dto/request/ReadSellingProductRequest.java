package com.dapanda.product.dto.request;

import com.dapanda.product.entity.ProductState;

public record ReadSellingProductRequest(

		Long cursorId,
		Integer size,
		String reviewSortOption,
		Long memberId,
		ProductState productState) {
}
