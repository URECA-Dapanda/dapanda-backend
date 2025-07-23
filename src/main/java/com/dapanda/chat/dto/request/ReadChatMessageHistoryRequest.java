package com.dapanda.chat.dto.request;

public record ReadChatMessageHistoryRequest(
		Long cursorId,
		int size,
		Long memberId,
		Long chatRoomId) {
}
