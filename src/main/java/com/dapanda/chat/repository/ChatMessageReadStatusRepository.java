package com.dapanda.chat.repository;

import com.dapanda.chat.entity.ChatMessageReadStatus;
import com.dapanda.chat.entity.ChatRoom;
import com.dapanda.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMessageReadStatusRepository extends JpaRepository<ChatMessageReadStatus, Long> {

	Optional<ChatMessageReadStatus> findFirstByChatRoomAndMember(ChatRoom chatRoom, Member member);
}
