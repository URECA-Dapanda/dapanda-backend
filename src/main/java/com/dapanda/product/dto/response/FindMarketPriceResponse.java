package com.dapanda.product.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FindMarketPriceResponse {

	private int recentPrice;
	private int averagePrice;

	public static FindMarketPriceResponse of(int recentPrice, int averagePrice) {

		return FindMarketPriceResponse.builder()
				.recentPrice(recentPrice)
				.averagePrice(averagePrice)
				.build();
	}

}
