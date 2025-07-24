package com.dapanda.product.entity;

import com.dapanda.product.dto.WifiSummary;
import java.time.LocalDateTime;

public class WifiFixture {

	public static Wifi createWifi(String title, String content, double latitude, double longitude,
			String address, LocalDateTime startTime, LocalDateTime endTime) {

		return Wifi.of(
				title,
				content,
				latitude,
				longitude,
				address,
				startTime,
				endTime
		);
	}

	public static WifiSummary createWifiSummary(Long id, int price, Long itemId, String memberName,
			String title, double latitude, double longitude, String address, double averageRate,
			double distanceKm, boolean isOpen, LocalDateTime updatedAt) {

		return new WifiSummary(
				id,
				price,
				itemId,
				memberName,
				"image.jpg",
				title,
				"imageUrl",
				latitude,
				longitude,
				address,
				averageRate,
				distanceKm,
				isOpen,
				updatedAt
		);
	}
}
