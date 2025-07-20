package com.dapanda.trade.dto.request;

import com.dapanda.trade.dto.MobileDataScrap;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TradeMobileDataScrapRequest(

		@NotNull(message = "총 데이터양은 필수입니다.")
		Float totalAmount,

		@NotNull(message = "총 가격은 필수입니다.")
		@Min(1)
		Integer totalPrice,

		@NotNull(message = "데이터 상품 조합 목록은 필수입니다.")
		List<MobileDataScrap> combinations) {

}
