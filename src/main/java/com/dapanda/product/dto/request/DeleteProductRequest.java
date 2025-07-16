package com.dapanda.product.dto.request;

import jakarta.validation.constraints.NotNull;

public record DeleteProductRequest(

		@NotNull(message = "삭제할 상품의 아이디는 필수입니다.")
		Long productId) {

}
