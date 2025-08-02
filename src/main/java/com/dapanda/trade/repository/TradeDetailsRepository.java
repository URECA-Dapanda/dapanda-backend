package com.dapanda.trade.repository;

import com.dapanda.trade.entity.TradeDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeDetailsRepository extends JpaRepository<TradeDetails, Long> {

	TradeDetails findByTrade_Id(Long tradeId);


}
