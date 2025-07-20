package com.dapanda.chat.entity;

import com.dapanda.common.entity.CreatedAtEntity;
import com.dapanda.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 300)
	private String message;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	public static ChatMessage of(String message, ChatRoom chatRoom, Member member) {

		return ChatMessage.builder()
				.message(message)
				.chatRoom(chatRoom)
				.member(member)
				.build();
	}
}
