package com.dapanda.chat.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebSocketPath {

	PUBLISH("/publish"),
	CONNECT("/connect"),
	TOPIC("/topic"),
	SLASH("/");

	private final String path;
}
