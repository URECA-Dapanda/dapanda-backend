package com.dapanda.chat.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.chat.config.WebSocketPath;
import com.dapanda.chat.dto.request.CreateChatMessageRequest;
import com.dapanda.chat.dto.response.CreateMessageResponse;
import com.dapanda.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

	private final SimpMessagingTemplate messageTemplate;
	private final ChatService chatService;

	@MessageMapping("/{chatRoomId}")
	public void sendMessage(
			@DestinationVariable Long chatRoomId,
			@Payload @Valid CreateChatMessageRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		CreateMessageResponse response = chatService.createChatMessage(chatRoomId, request, userDetails.getId());

		messageTemplate.convertAndSend(
				WebSocketPath.SUB.getPath() + WebSocketPath.SLASH.getPath() + chatRoomId,
				response
		);
	}
}
