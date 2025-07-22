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
public class FindDataResponse {

	private float data;

	public static FindDataResponse of(float data) {

		return FindDataResponse.builder()
				.data(data)
				.build();
	}
}
