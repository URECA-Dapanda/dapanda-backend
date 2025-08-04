package com.dapanda.product.entity;

import com.dapanda.product.dto.WifiSummary;
import java.time.LocalDateTime;
import org.springframework.test.util.ReflectionTestUtils;

public class WifiFixture {

	public static Wifi createWifi() {

		return Wifi.of(
				"와이파이1",
				"콘텐츠1",
				123d,
				1234d,
				"주소1",
				LocalDateTime.now(),
				LocalDateTime.now()
		);
	}

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
			String title, double latitude, double longitude, String address, float averageRate,
			double distanceKm, boolean isOpen, LocalDateTime startTime, LocalDateTime endTime) {

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
				startTime,
				endTime
		);
	}

	public static Wifi createWifiWithId(Long wifiId) {

		Wifi wifi = Wifi.of(
				"와이파이1",
				"콘텐츠1",
				123d,
				1234d,
				"주소1",
				LocalDateTime.now(),
				LocalDateTime.now()
		);

		ReflectionTestUtils.setField(wifi, "id", wifiId);

		return wifi;
	}
}
