package com.dapanda.product.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateMobileDataRequest extends CreateProductRequest {

	@NotNull(message = "dataAmount는 null일 수 없습니다.")
	private final Float dataAmount; // GB 단위

	@NotNull(message = "isSplitType는 null일 수 없습니다.")
	private final Boolean isSplitType;

	public CreateMobileDataRequest(
			Integer price,
			Float dataAmount,
			Boolean isSplitType
	) {
		super(price);
		this.dataAmount = dataAmount;
		this.isSplitType = isSplitType;
	}
}
