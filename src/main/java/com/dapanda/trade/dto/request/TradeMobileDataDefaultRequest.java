package com.dapanda.trade.dto.request;

import jakarta.validation.constraints.NotNull;

public record TradeMobileDataDefaultRequest(

		@NotNull(message = "상품 아이디는 필수입니다.")
		Long productId,

		@NotNull(message = "상품 가격은 필수입니다.")
		Integer price,

		Float dataAmount
) {

}
