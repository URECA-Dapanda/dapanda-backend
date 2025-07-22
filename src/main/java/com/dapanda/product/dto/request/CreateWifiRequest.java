package com.dapanda.product.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class CreateWifiRequest extends CreateProductRequest {

	private final String title;
	private final String content;
	private final Double latitude;
	private final Double longitude;
	private final LocalDateTime startTime;
	private final LocalDateTime endTime;
	private final List<CreateProductImageRequest> images;

	public CreateWifiRequest(
			Integer price,
			String title,
			String content,
			Double latitude,
			Double longitude,
			LocalDateTime startTime,
			LocalDateTime endTime,
			List<CreateProductImageRequest> images
	) {
		super(price);
		this.title = title;
		this.content = content;
		this.latitude = latitude;
		this.longitude = longitude;
		this.startTime = startTime;
		this.endTime = endTime;
		this.images = images;
	}
}
