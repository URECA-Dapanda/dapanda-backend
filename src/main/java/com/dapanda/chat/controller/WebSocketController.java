package com.dapanda.chat.controller;

import com.dapanda.chat.config.WebSocketPath;
import com.dapanda.chat.dto.request.ChatMessageRequest;
import com.dapanda.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

	private final SimpMessageSendingOperations messageTemplate;
	private final ChatService chatService;

	@MessageMapping("/{chatRoomId}")
	public void sendMessage(@DestinationVariable Long chatRoomId, ChatMessageRequest request) {

		log.info("Message : {}", request.message());

		chatService.createChatMessage(chatRoomId, request);

		messageTemplate.convertAndSend(WebSocketPath.TOPIC.getPath() + WebSocketPath.SLASH.getPath() + chatRoomId, request.message());
	}
}
