package com.dapanda.trade.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CashHistoryMonthlySummary {

	private Integer totalPurchase; // 구매 지출
	private Integer totalSelling;  // 판매 수입
	private Integer totalCharge;   // 캐시 충전
	private Integer totalRefund;   // 캐시 환불
	private Integer total;
}
