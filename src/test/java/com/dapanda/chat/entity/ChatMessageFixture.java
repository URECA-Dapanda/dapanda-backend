package com.dapanda.chat.entity;

import com.dapanda.member.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

public class ChatMessageFixture {

	public static ChatMessage createChatMessageWithId(ChatRoom chatRoom, Member sender, Long chatMessageId) {

		ChatMessage chatMessage = ChatMessage.of(
				"안녕하세요",
				chatRoom,
				sender
		);

		ReflectionTestUtils.setField(chatMessage, "id", chatMessageId);

		return chatMessage;
	}
}
