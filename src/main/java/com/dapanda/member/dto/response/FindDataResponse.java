package com.dapanda.member.dto.response;


import java.math.BigDecimal;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FindDataResponse {

	private BigDecimal data;

	public static FindDataResponse of(BigDecimal data) {

		return FindDataResponse.builder()
				.data(data)
				.build();
	}
}
