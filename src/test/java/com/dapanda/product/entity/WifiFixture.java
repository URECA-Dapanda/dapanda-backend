package com.dapanda.product.entity;

import com.dapanda.product.dto.WifiSummary;
import java.time.LocalDateTime;

public class WifiFixture {

	public static Wifi createWifi(String title, String content, double latitude, double longitude,
			LocalDateTime startTime, LocalDateTime endTime) {

		return Wifi.of(
				title,
				content,
				latitude,
				longitude,
				startTime,
				endTime
		);
	}

	public static WifiSummary createWifiSummary(Long id, int price, Long itemId, String memberName,
			String title, double latitude, double longitude, double averageRate,
			double distanceKm) {

		return new WifiSummary(
				id,
				price,
				itemId,
				memberName,
				title,
				"imageUrl",
				latitude,
				longitude,
				averageRate,
				distanceKm
		);
	}
}
