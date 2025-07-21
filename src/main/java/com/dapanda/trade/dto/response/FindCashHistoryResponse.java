package com.dapanda.trade.dto.response;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.trade.dto.CashHistoryMonthlySummary;
import com.dapanda.trade.dto.CashHistorySummary;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FindCashHistoryResponse {

	private CashHistoryMonthlySummary cashHistoryMonthlySummary;
	private CursorPageResponse<CashHistorySummary> cashHistorySummary;

	public static FindCashHistoryResponse of(CashHistoryMonthlySummary monthlySummary,
			CursorPageResponse<CashHistorySummary> cashHistorySummary) {

		return FindCashHistoryResponse.builder()
				.cashHistoryMonthlySummary(monthlySummary)
				.cashHistorySummary(cashHistorySummary)
				.build();
	}
}
