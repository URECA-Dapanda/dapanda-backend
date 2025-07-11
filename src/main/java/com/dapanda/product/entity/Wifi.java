package com.dapanda.product.entity;

import jakarta.persistence.Entity;
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

	private double latitude; // 위도

	private double longitude; // 경도

	private String imageUrl;

	private LocalDateTime startTime;

	private LocalDateTime endTime;
}
