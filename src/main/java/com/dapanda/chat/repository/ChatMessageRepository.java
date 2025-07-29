package com.dapanda.chat.repository;

import com.dapanda.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {

	boolean existsByIdAndMember_IdAndChatRoom_Id(Long id, Long memberId, Long chatRoomId);
}
