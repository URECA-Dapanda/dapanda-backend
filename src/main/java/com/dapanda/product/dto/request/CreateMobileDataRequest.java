package com.dapanda.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateMobileDataRequest extends CreateProductRequest {

	@NotNull(message = "dataAmount는 null일 수 없습니다.")
	@Min(value = 1, message = "dataAmount는 1MB 이상이어야 합니다.")
	private final Float dataAmount; // MB 단위

	@NotNull(message = "isSplitType는 null일 수 없습니다.")
	private final Boolean isSplitType;

	public CreateMobileDataRequest(
			Long memberId,
			Integer price,
			Float dataAmount,
			Boolean isSplitType
	) {
		super(memberId, price);
		this.dataAmount = dataAmount;
		this.isSplitType = isSplitType;
	}
}
