package com.dapanda.product.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Wifi {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String content;

	//위도
	private double latitude;

	//경도
	private double longitude;

	private String imageUrl;

	@Enumerated(EnumType.STRING)
	private WifiSellingUnit unit;
}
