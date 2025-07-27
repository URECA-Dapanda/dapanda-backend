package com.dapanda.trade.repository;

import com.dapanda.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long>, TradeCustomRepository {

	boolean existsByProductId(Long productId);
}
