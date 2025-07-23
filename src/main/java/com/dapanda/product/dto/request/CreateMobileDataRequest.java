package com.dapanda.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class CreateMobileDataRequest extends CreateProductRequest {

	@NotNull(message = "dataAmount는 null일 수 없습니다.")
	@DecimalMin(value = "0.1", message = "dataAmount는 최소 0.1GB 이상이어야 합니다.")
	@DecimalMax(value = "2.0", message = "dataAmount는 최대 2.0GB 이하여야 합니다.")
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
