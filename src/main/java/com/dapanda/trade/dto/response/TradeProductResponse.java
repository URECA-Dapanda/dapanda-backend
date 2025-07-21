package com.dapanda.trade.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeProductResponse {

	private Long tradeId;

	public static TradeProductResponse of(Long tradeId) {

		return builder()
				.tradeId(tradeId)
				.build();
	}
}
