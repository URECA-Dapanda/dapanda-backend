package com.dapanda.chat.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessagePrinciple {

	SIMP_MESSAGE_TYPE("simpMessageType"),
	PAYLOAD("payload"),
	NATIVE_HEADERS("nativeHeaders"),
	EXCEPT_MEMBER_ID("exceptMemberId"),
	SENDER_ID("senderId");

	private final String key;
}
