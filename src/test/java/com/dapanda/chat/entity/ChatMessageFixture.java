package com.dapanda.chat.entity;

import com.dapanda.member.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

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

	public static ChatMessage createChatMessage(ChatRoom chatRoom, Member seller, String message) {

		return ChatMessage.of(message, chatRoom, seller);
	}

	public static List<ChatMessage> createChatMessageList(ChatRoom chatRoom, Member buyer, Member seller) {

		String testMessage = "테스트 메시지 입니다~!@#$%^&*()_+";

		List<ChatMessage> chatMessagesList = new ArrayList<>();

		for (int i = 0; i < 100; i = i + 2) {

			chatMessagesList.add(createChatMessage(chatRoom, seller, testMessage + i));
			chatMessagesList.add(createChatMessage(chatRoom, buyer, testMessage + i + 1));
		}

		return chatMessagesList;
	}
}
