package com.dapanda.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateMobileDataRequest(

		@NotNull(message = "상품 아이디는 필수입니다.")
		Long productId,

		@NotNull(message = "상품 가격은 필수입니다.")
		@Min(0)
		int price,

		@NotNull(message = "변경된 데이터양은 필수입니다.")
		float changedAmount,

		@NotNull(message = "분할 여부는 필수입니다.")
		boolean isSplitType) {

}
