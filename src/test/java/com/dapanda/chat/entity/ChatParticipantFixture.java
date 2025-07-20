package com.dapanda.chat.entity;

import com.dapanda.member.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

public class ChatParticipantFixture {

	public static ChatParticipant createChatParticipantWithId(ChatRoom chatroom, Member member, Long chatParticipantId) {

		ChatParticipant chatParticipant = ChatParticipant.of(chatroom, member);

		ReflectionTestUtils.setField(chatParticipant, "id", chatParticipantId);

		return chatParticipant;
	}
}
