package com.dapanda.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtPrinciple {

	ACCESS_TOKEN("accessToken"),
	REFRESH_TOKEN("refreshToken");

	private final String key;
}
