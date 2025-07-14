package com.dapanda.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductCursorRequest {

	private final Long cursorId;

	@NotNull(message = "size는 null일 수 없습니다.")
	@Min(value = 1, message = "size는 최소 1 이상이어야 합니다.")
	private final Integer size;

	private final String productSortOption;
}
