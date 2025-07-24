package com.dapanda.product.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MobileDataInfoResponse {

	private Long productId;
	private Long itemId;
	private int price;
	private Long memberId;
	private String memberName;
	private BigDecimal remainAmount;
	private int pricePer100MB;
	private float averageRate;
	private int reviewCount;
	private boolean splitType;
	private LocalDateTime updatedAt;
}
