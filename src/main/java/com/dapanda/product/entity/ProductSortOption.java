package com.dapanda.product.entity;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import lombok.Getter;

@Getter
public enum ProductSortOption {

	RECENT, // 최신순
	PRICE_ASC, // 가격 낮은순
	AMOUNT_ASC, // 데이터 용량 적은순
	AMOUNT_DESC, // 데이터 용량 많은순
	DISTANCE_ASC, // 거리 가까운순
	AVERAGE_RATE_DESC, // 리뷰 평점 높은순
	;

	public static ProductSortOption from(String name) {

		if (name == null) {

			return RECENT; // 기본값: 최신순
		}

		try {

			return ProductSortOption.valueOf(name);
		} catch (IllegalArgumentException e) {

			throw new GlobalException(ResultCode.INVALID_PRODUCT_SORT_OPTION);
		}
	}
}
