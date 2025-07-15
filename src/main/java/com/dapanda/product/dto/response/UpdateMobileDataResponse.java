package com.dapanda.product.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateMobileDataResponse {

	private Long productId;

	public static UpdateMobileDataResponse from(Long productId) {

		return UpdateMobileDataResponse.builder()
				.productId(productId)
				.build();
	}
}
