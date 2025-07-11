package com.dapanda.product.entity;

import lombok.Getter;

@Getter
public enum ProductSortOption {

	RECENT("RECENT"), // 최신순
	PRICE_ASC("PRICE_ASC"), // 낮은 가격순
	AMOUNT_ASC("AMOUNT_ASC"), // 데이터 용량 적은순
	AMOUNT_DESC("AMOUNT_DESC"), // 데이터 용량 많은순
	DISTANCE_ASC("DISTANCE_ASC"), // 거리 가까운순
	AVERAGE_RATE_DESC("AVERAGE_RATE_DESC"); // 리뷰 평점 높은순

	private final String code;

	ProductSortOption(String code) {

		this.code = code;
	}
}
