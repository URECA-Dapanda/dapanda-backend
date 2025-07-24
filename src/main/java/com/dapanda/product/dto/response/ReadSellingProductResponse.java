package com.dapanda.product.dto.response;

import com.dapanda.product.entity.ItemType;
import com.dapanda.product.entity.ProductState;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadSellingProductResponse {

	private Long productId;
	private ItemType type;
	private ProductState state;

	// Mobile Data
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private BigDecimal dataAmount;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private BigDecimal remainAmount;

	// WIFI
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private LocalDateTime startTime;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private LocalDateTime endTime;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static ReadSellingProductResponse createMobileDataResponse(
			Long productId,
			ItemType type,
			ProductState state,
			BigDecimal dataAmount,
			BigDecimal remainAmount,
			LocalDateTime createdAt,
			LocalDateTime updatedAt) {

		return ReadSellingProductResponse.builder()
				.productId(productId)
				.type(type)
				.state(state)
				.dataAmount(dataAmount)
				.remainAmount(remainAmount)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();
	}

	public static ReadSellingProductResponse createWifiResponse(
			Long productId,
			ItemType type,
			ProductState state,
			LocalDateTime startTime,
			LocalDateTime endTime,
			LocalDateTime createdAt,
			LocalDateTime updatedAt) {

		return ReadSellingProductResponse.builder()
				.productId(productId)
				.type(type)
				.state(state)
				.startTime(startTime)
				.endTime(endTime)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();
	}
}
