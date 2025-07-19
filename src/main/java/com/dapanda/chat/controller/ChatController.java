package com.dapanda.chat.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.chat.dto.response.CreateChatRoomResponse;
import com.dapanda.chat.service.ChatService;
import com.dapanda.common.exception.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	@PostMapping("/products/{productId}/chat-room")
	public CommonResponse<CreateChatRoomResponse> createChatRoom(
			@PathVariable Long productId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(chatService.createChatRoom(productId, userDetails.getId()));
	}
}
