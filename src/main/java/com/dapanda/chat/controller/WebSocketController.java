package com.dapanda.chat.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.chat.config.WebSocketPath;
import com.dapanda.chat.dto.request.CreateChatMessageRequest;
import com.dapanda.chat.dto.response.SendChatMessageResponse;
import com.dapanda.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

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
			Principal principal) {

		log.info("Message : {}", request.message());

		// principal이 Authentication인 경우만 캐스팅
		CustomUserDetails userDetails = null;
		if (principal instanceof Authentication) {
			userDetails = (CustomUserDetails) ((Authentication)principal).getPrincipal();
		}

		// 예외처리 - 인증객체가 없거나, principal이 기대 타입 아닐 때
		if (userDetails == null) {
			log.error("UserDetails 주입 실패");
			throw new IllegalStateException("인증 정보 불일치");
		}

		SendChatMessageResponse response = chatService.createChatMessage(chatRoomId, request, userDetails.getId());

		messageTemplate.convertAndSend(WebSocketPath.SUB.getPath() + WebSocketPath.SLASH.getPath() + chatRoomId, response);
	}
}
