package com.dapanda.chat.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.chat.config.MessagePrinciple;
import com.dapanda.chat.config.WebSocketPath;
import com.dapanda.chat.dto.request.CreateChatMessageRequest;
import com.dapanda.chat.dto.response.CreateMessageResponse;
import com.dapanda.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

	private final SimpMessageSendingOperations messageTemplate;
	private final ChatService chatService;

	@MessageMapping("/{chatRoomId}")
	public void sendMessage(
			@DestinationVariable Long chatRoomId,
			@Valid CreateChatMessageRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		CreateMessageResponse response = chatService.createChatMessage(chatRoomId, request, userDetails.getId());

		MessageHeaders headers = new MessageHeaders(
				Map.of(
						MessagePrinciple.EXCEPT_MEMBER_ID.getKey(), userDetails.getId(),
						MessagePrinciple.SENDER_ID.getKey(), response.getSenderId()
				)
		);

		messageTemplate.convertAndSend(
				WebSocketPath.SUB.getPath() + WebSocketPath.SLASH.getPath() + chatRoomId,
				response.getSendMessageDto(),
				headers);
	}
}
