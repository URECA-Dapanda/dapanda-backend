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
public class UpdateWifiResponse {

	private Long productId;

	public static UpdateWifiResponse from(Long productId) {

		return UpdateWifiResponse.builder()
				.productId(productId)
				.build();
	}

}
