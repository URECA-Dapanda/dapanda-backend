package com.dapanda.trade.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MobileDataScrap {

	private Long productId;
	private Long mobileDataId;
	private String memberName;
	private int price;
	private int purchasePrice;
	private float remainAmount;
	private float purchaseAmount;
	private int pricePer100MB;
	private boolean splitType;
	private LocalDateTime updatedAt;
}
