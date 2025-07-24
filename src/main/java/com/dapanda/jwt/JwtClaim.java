package com.dapanda.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtClaim {

		ID("id"),
		ROLE("role"),
		PROVIDER("provider");

		private final String claim;
}
