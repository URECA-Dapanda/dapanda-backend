package com.dapanda.chat.repository;

import com.dapanda.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {

	@Query("""
			SELECT m.id
			FROM ChatMessage cm
			JOIN cm.member m
			WHERE cm.id = :chatMessageId
			""")
	Optional<Long> findMemberIdByChatMessageId(Long chatMessageId);
}
