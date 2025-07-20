package com.dapanda.trade.dto.request;

import jakarta.validation.constraints.NotNull;

public record TradeMobileDataDefaultRequest(

		@NotNull(message = "상품 아이디는 필수입니다.")
		Long productId,

		@NotNull(message = "데이터 아이디는 필수입니다.")
		Long mobileDataId,

		Float dataAmount) {

}
