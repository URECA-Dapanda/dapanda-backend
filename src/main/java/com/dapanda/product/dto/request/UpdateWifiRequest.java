package com.dapanda.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record UpdateWifiRequest(

		@NotNull(message = "상품 아이디는 필수입니다.")
		Long productId,

		@NotNull(message = "상품 가격은 필수입니다.")
		@Min(0)
		int price,
		
		@NotNull(message = "상품 제목은 필수입니다.")
		String title,

		@NotNull(message = "상품 본문은 필수입니다.")
		String content,

		@NotNull(message = "위도는 필수입니다.")
		double latitude,

		@NotNull(message = "경도는 필수입니다.")
		double longitude,

		@NotNull(message = "시작 시간은 필수입니다.")
		LocalDateTime startTime,

		@NotNull(message = "종료 시간은 필수입니다.")
		LocalDateTime endTime
) {

}
