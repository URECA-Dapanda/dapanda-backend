package com.dapanda.common.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AllowedOriginPath {

	LOCAL("http://localhost:3000"),
	PROD("https://dapanda.org");

	private final String path;
}
