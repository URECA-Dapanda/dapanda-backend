package com.dapanda.chat.controller;

import com.dapanda.chat.config.WebSocketPath;
import com.dapanda.chat.dto.request.CreateChatMessageRequest;
import com.dapanda.chat.service.ChatService;
import jakarta.validation.Valid;
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
	public void sendMessage(
			@DestinationVariable Long chatRoomId,
			@Valid CreateChatMessageRequest request) {

		log.info("Message : {}", request.message());

		chatService.createChatMessage(chatRoomId, request);

		messageTemplate.convertAndSend(WebSocketPath.SUB.getPath() + WebSocketPath.SLASH.getPath() + chatRoomId, request.message());
	}
}
