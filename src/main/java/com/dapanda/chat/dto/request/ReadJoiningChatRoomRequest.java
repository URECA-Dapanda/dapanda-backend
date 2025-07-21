package com.dapanda.chat.dto.request;

import com.dapanda.chat.entity.ChatRoomReadOption;

import java.time.LocalDateTime;

public record ReadJoiningChatRoomRequest(

		Long cursorId,
		LocalDateTime lastMessageAt,
		Integer size,
		ChatRoomReadOption chatRoomReadOption,
		Long memberId) {
}
