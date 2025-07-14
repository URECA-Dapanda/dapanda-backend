package com.dapanda.product.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class WifiCursorRequest extends ProductCursorRequest {

	@NotNull(message = "open은 null일 수 없습니다.")
	private final boolean open;

	@NotNull(message = "latitude는 null일 수 없습니다.")
	private final Double latitude;

	@NotNull(message = "longitude는 null일 수 없습니다.")
	private final Double longitude;

	public WifiCursorRequest(Long cursorId, Integer size,
			String productSortOption, boolean open, Double latitude, Double longitude) {

		super(cursorId, size, productSortOption);
		this.open = open;
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
