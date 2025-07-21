package com.dapanda.trade.repository;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.trade.dto.TradeHistorySummary;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeCustomRepository {

	public CursorPageResponse<TradeHistorySummary> findTradeHistoryByCursor(Long cursorId,
			int size, Long memberId);

	public Long countTradeHistoryByMemberId(Long memberId);
}
