package com.dapanda.product.entity;

import com.dapanda.product.dto.WifiSummary;
import java.time.LocalDateTime;
import org.springframework.test.util.ReflectionTestUtils;

public class WifiFixture {

	public static Wifi createWifi1(String title, String content, double latitude, double longitude,
			LocalDateTime startTime, LocalDateTime endTime) {

		return Wifi.of(
				title,
				content,
				latitude,
				longitude,
				"imageUrl",
				startTime,
				endTime
		);
	}

	public static Wifi createWifi2(Long wifiId) {

		Wifi wifi = Wifi.of(
				"와이파이 팝니다",
				"선릉역 2번출구 앞 카페 와이파이",
				30.1,
				126.3,
				"imageUrl",
				LocalDateTime.of(2025, 7, 13, 10, 0),
				LocalDateTime.of(2025, 7, 13, 22, 0)
		);

		ReflectionTestUtils.setField(wifi, "id", wifiId);

		return wifi;
	}

	public static WifiSummary createWifiSummary(Long id, int price, Long itemId, String memberName,
			String title,
			double latitude, double longitude, double averageRate,
			double distanceKm) {

		return new WifiSummary(
				id,
				price,
				itemId,
				memberName,
				title,
				latitude,
				longitude,
				"imageUrl",
				averageRate,
				distanceKm
		);
	}
}
