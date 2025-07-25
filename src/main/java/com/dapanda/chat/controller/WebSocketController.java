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
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.Collections;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

	private final SimpMessagingTemplate messageTemplate;
	private final ChatService chatService;

	/**
	 * 하나의 채팅방으로 /pub
	 * 구독중인 사용자들에게 메시지 전달
	 * OutboundInterceptor 에서 메시지의 NativeHeader에 담긴 세션과 메시지를 보낼 대상의 세션 동등 비교
	 * 동등할 경우, 자신의 메시지로 추정하고 return null
	 * 다를 경우, return message
	 */
	@MessageMapping("/{chatRoomId}")
	public void sendMessage(
			@DestinationVariable Long chatRoomId,
			@Payload @Valid CreateChatMessageRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			SimpMessageHeaderAccessor accessor) {

		CreateMessageResponse response = chatService.createChatMessage(chatRoomId, request, userDetails.getId());

		messageTemplate.convertAndSend(
				WebSocketPath.SUB.getPath() + WebSocketPath.SLASH.getPath() + chatRoomId,
				response,
				Collections.singletonMap(MessagePrinciple.SIMP_SESSION_ID.getKey(), accessor.getSessionId()));
	}
}
