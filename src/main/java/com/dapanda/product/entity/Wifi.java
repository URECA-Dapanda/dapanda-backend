package com.dapanda.product.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Getter
public class Wifi {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	private String content;

	//위도
	private double latitude;

	//경도
	private double longitude;

	private String imageUrl;

	@Enumerated(EnumType.STRING)
	private WifiSellingUnit unit;

	private LocalDateTime startTime;

	private LocalDateTime endTime;
}
