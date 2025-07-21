package com.dapanda.trade.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record PurchaseWifiRequest(

		@NotNull(message = "상품 아이디는 필수입니다.")
		Long productId,

		@NotNull(message = "와이파이 아이디는 필수입니다.")
		Long wifiId,

		@NotNull(message = "시작 시간은 필수입니다.")
		LocalDateTime startTime,

		@NotNull(message = "종료 시간은 필수입니다.")
		LocalDateTime endTime) {

}
