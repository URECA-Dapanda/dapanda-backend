package com.dapanda.product.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Wifi {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	private String content;

	private double latitude; // 위도

	private double longitude; // 경도

	private String address;

	private LocalDateTime startTime;

	private LocalDateTime endTime;

	public static Wifi of(String title, String content, double latitude, double longitude,
			String address, LocalDateTime startTime, LocalDateTime endTime) {

		return Wifi.builder()
				.title(title)
				.content(content)
				.latitude(latitude)
				.longitude(longitude)
				.address(address)
				.startTime(startTime)
				.endTime(endTime)
				.build();
	}

	public void updateWifi(String title, String content, double latitude, double longitude,
			String address, LocalDateTime startTime, LocalDateTime endTime) {

		this.title = title;
		this.content = content;
		this.latitude = latitude;
		this.longitude = longitude;
		this.address = address;
		this.startTime = startTime;
		this.endTime = endTime;
	}
}
