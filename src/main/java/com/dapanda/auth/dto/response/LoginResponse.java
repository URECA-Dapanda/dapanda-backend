package com.dapanda.auth.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResponse {

	private String name;
	private String message;

	public static LoginResponse from(String name, String message) {
		return LoginResponse.builder()
				.name(name)
				.message(message)
				.build();
	}
}
