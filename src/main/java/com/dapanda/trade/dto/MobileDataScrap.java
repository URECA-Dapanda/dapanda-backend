package com.dapanda.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MobileDataScrap {

	private Long productId;
	private Long mobileDataId;
	private String memberName;
	private int price;
	private int purchasePrice;
	private BigDecimal remainAmount;
	private BigDecimal purchaseAmount;
	private int pricePer100MB;
	private boolean splitType;
	private LocalDateTime updatedAt;
}
