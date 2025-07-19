package com.dapanda.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public abstract class CreateProductRequest {

	@NotNull(message = "memberId는 null일 수 없습니다.")
	private final Long memberId;

	@NotNull(message = "price는 null일 수 없습니다.")
	@Min(value = 0, message = "price는 0 이상이어야 합니다.")
	private final Integer price;

	protected CreateProductRequest(Long memberId, Integer price) {
		this.memberId = memberId;
		this.price = price;
	}
}
