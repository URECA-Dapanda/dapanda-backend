package com.dapanda.product.entity;

public class ProductImageFixture {

	public static ProductImage createProductImage(String imageUrl, int priority, Long wifiId) {

		return ProductImage.of(
				imageUrl,
				priority,
				wifiId
		);
	}
}
