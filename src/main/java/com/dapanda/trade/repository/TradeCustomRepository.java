package com.dapanda.trade.repository;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.trade.dto.*;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeCustomRepository {

	public CursorPageResponse<PurchaseHistorySummary> findTradeHistoryByCursor(Long cursorId,
			int size, Long memberId);

	public Long countTradeHistoryByMemberId(Long memberId);

	public CursorPageResponse<CashHistorySummary> findCashHistoryByCursor(Long cursorId, int size,
			Long memberId, int year, int month);

	public CashHistoryMonthlySummary calculateMonthlySummary(Long memberId, int year, int month);

	boolean existsByProductId(Long productId);

}
