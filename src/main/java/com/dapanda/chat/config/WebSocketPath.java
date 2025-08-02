package com.dapanda.chat.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebSocketPath {

	ALARM("/alarm"),
	PUB("/pub"),
	CONN("/conn"),
	SUB("/sub"),
	SLASH("/");

	private final String path;
}
