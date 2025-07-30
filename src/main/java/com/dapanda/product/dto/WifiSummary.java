package com.dapanda.product.dto;

import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WifiSummary extends ProductSummary {

	private String title;
	private double latitude;
	private double longitude;
	private String address;
	private String imageUrl;
	private float averageRate;
	private double distanceKm;
	private boolean open;
	private LocalTime startTime;
	private LocalTime endTime;

	public WifiSummary(Long id, int price, Long itemId, String memberName, String profileImageUrl,
			String title, String imageUrl, double latitude, double longitude, String address,
			float averageRate, double distanceKm, boolean open, String startTime,
			String endTime) {

		super(id, price, itemId, memberName, profileImageUrl);
		this.title = title;
		this.imageUrl = imageUrl;
		this.latitude = latitude;
		this.longitude = longitude;
		this.address = address;
		this.averageRate = averageRate;
		this.distanceKm = distanceKm;
		this.open = open;
		this.startTime = LocalTime.parse(startTime);
		this.endTime = LocalTime.parse(endTime);
	}
}
