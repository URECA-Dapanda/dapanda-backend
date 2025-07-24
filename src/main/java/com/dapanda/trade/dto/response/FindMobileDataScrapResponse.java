package com.dapanda.trade.dto.response;

import com.dapanda.trade.dto.MobileDataScrap;
import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FindMobileDataScrapResponse {

	private BigDecimal totalAmount;
	private int totalPrice;
	private List<MobileDataScrap> combinations;

	public static FindMobileDataScrapResponse of(BigDecimal totalAmount, int totalPrice,
			List<MobileDataScrap> combinations) {

		return FindMobileDataScrapResponse.builder()
				.totalAmount(totalAmount)
				.totalPrice(totalPrice)
				.combinations(combinations)
				.build();
	}
}
