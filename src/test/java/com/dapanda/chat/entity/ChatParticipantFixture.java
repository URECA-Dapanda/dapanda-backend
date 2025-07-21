package com.dapanda.chat.entity;

import com.dapanda.member.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatParticipantFixture {

	public static ChatParticipant createChatParticipantWithId(ChatRoom chatroom, Member member, Long chatParticipantId) {

		ChatParticipant chatParticipant = ChatParticipant.of(chatroom, member);

		ReflectionTestUtils.setField(chatParticipant, "id", chatParticipantId);

		return chatParticipant;
	}

	public static List<ChatParticipant> createChatParticipantList(List<ChatRoom> chatRoomList, Member buyer, Member seller){

		List<ChatParticipant> chatParticipantList = new ArrayList<>();

		for (ChatRoom chatRoom : chatRoomList) {

			chatParticipantList.add(ChatParticipant.of(chatRoom, buyer));
			chatParticipantList.add(ChatParticipant.of(chatRoom, seller));
		}

		return chatParticipantList;
	}
}
