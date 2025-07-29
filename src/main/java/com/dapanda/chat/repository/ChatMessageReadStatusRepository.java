package com.dapanda.chat.repository;

import com.dapanda.chat.entity.ChatMessageReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageReadStatusRepository extends JpaRepository<ChatMessageReadStatus, Long> {
}
