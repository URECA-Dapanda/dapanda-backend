package com.dapanda.chat.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessagePrinciple {

	SIMP_MESSAGE_TYPE("simpMessageType"),
	SIMP_SESSION_ID("simpSessionId");

	private final String key;
}
