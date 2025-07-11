package com.dapanda.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WifiSummary extends ProductSummary {

	private String title;
	private double latitude;
	private double longitude;
	private String imageUrl;
	private double averageRate;

	public WifiSummary(Long id, int price, Long itemId, String memberName, String title,
			double latitude, double longitude, String imageUrl, double averageRate) {

		super(id, price, itemId, memberName);
		this.title = title;
		this.latitude = latitude;
		this.longitude = longitude;
		this.imageUrl = imageUrl;
		this.averageRate = averageRate;
	}
}
