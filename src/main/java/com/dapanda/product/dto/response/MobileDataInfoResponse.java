package com.dapanda.product.dto.response;

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
	private float remainAmount;
	private int pricePer100MB;
	private double averageRate;
	private int reviewCount;
	private boolean splitType;
	private LocalDateTime updatedAt;
}
