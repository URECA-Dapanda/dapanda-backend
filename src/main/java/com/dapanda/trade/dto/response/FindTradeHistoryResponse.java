package com.dapanda.trade.dto.response;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.trade.dto.TradeHistorySummary;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FindTradeHistoryResponse {

	private Long tradeCount;
	private CursorPageResponse<TradeHistorySummary> trades;

	public static FindTradeHistoryResponse of(Long tradeCount,
			CursorPageResponse<TradeHistorySummary> trades) {

		return FindTradeHistoryResponse.builder()
				.tradeCount(tradeCount)
				.trades(trades)
				.build();
	}
}
