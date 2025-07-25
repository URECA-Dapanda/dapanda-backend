package com.dapanda.trade.dto;

import com.dapanda.trade.entity.TradeType;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CashHistorySummary {

	private Long tradeId;
	private TradeType tradeType;
	private int price;
	private String description;
	private String classification;
	private LocalDateTime createdAt;
}
