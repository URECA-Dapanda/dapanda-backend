package com.dapanda.chat.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebSocketPath {

	ALARM("/alarm"),
	PUB("/pub"),
	CONN("/conn"),
	SUB("/sub");

	private final String path;

	public static String getChatRoomSubscribePath(Long chatRoomId) {

		return SUB.getPath() + "/" + chatRoomId;
	}

	public static String getChatRoomPublishPath(Long chatRoomId) {

		return PUB.getPath() + "/" + chatRoomId;
	}

	public static String getAlarmPath(Long memberId) {

		return SUB.getPath() + ALARM.getPath() + "/" + memberId;
	}
}
