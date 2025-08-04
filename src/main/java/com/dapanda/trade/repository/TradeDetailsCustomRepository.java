package com.dapanda.trade.repository;

import com.dapanda.trade.entity.TradeDetails;
import java.util.List;

public interface TradeDetailsCustomRepository {

	List<TradeDetails> findOngoingWifiTradeDetailsByMemberId(Long memberId);
}
