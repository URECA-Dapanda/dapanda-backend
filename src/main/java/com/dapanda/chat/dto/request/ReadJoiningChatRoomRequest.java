package com.dapanda.chat.dto.request;

import com.dapanda.chat.entity.ChatRoomReadOption;

public record ReadJoiningChatRoomRequest(

		Long cursorId,
		Integer size,
		ChatRoomReadOption chatRoomReadOption,
		Long memberId) {
}
