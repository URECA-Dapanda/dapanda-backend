package com.dapanda.trade.dto.response;

import com.dapanda.trade.dto.MobileDataScrap;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FindMobileDataScrapResponse {

	private float totalAmount;
	private int totalPrice;
	private List<MobileDataScrap> combinations;

	public static FindMobileDataScrapResponse of(float totalAmount, int totalPrice,
			List<MobileDataScrap> combinations) {

		return FindMobileDataScrapResponse.builder()
				.totalAmount(totalAmount)
				.totalPrice(totalPrice)
				.combinations(combinations)
				.build();
	}
}
