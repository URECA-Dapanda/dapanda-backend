package com.dapanda.chat.entity;

import com.dapanda.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class ChatParticipant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "chat_room_id")
	private ChatRoom chatRoom;

	@OneToOne
	@JoinColumn(name = "member_id")
	private Member member;
}
