package com.dapanda.member.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FindCashResponse {

	private int cash;

	public static FindCashResponse of(int cash) {

		return FindCashResponse.builder()
				.cash(cash)
				.build();
	}
}
