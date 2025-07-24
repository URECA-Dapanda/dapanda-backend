package com.dapanda.trade.dto;

import com.dapanda.trade.entity.TradeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseHistorySummary {

	private Long tradeId;
	private TradeType tradeType;
	private BigDecimal dataAmount; // 데이터 상품
	private String title; // 와이파이 상품
	private LocalDateTime createdAt;
}
