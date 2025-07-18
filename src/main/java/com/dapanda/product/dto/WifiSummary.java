package com.dapanda.product.dto;

import java.time.LocalDateTime;
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
	private double distanceKm;
	private LocalDateTime updatedAt;

	public WifiSummary(Long id, int price, Long itemId, String memberName, String title,
			String imageUrl, double latitude, double longitude, double averageRate,
			double distanceKm, LocalDateTime updatedAt) {

		super(id, price, itemId, memberName);
		this.title = title;
		this.imageUrl = imageUrl;
		this.latitude = latitude;
		this.longitude = longitude;
		this.averageRate = averageRate;
		this.distanceKm = distanceKm;
		this.updatedAt = updatedAt;
	}
}
