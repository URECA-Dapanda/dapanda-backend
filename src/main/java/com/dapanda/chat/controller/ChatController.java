package com.dapanda.chat.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.chat.dto.request.ReadJoiningChatRoomRequest;
import com.dapanda.chat.dto.response.CreateChatRoomResponse;
import com.dapanda.chat.dto.response.ReadJoiningChatRoomResponse;
import com.dapanda.chat.dto.response.SendChatMessageResponse;
import com.dapanda.chat.entity.ChatRoomReadOption;
import com.dapanda.chat.service.ChatService;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.CommonResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	@GetMapping("/chat-room")
	public CommonResponse<CursorPageResponse<ReadJoiningChatRoomResponse>> readChatRoom(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(required = false) LocalDateTime lastMessageAt,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) int size,
			@RequestParam(defaultValue = "ALL") ChatRoomReadOption chatRoomReadOption) {

		ReadJoiningChatRoomRequest request = new ReadJoiningChatRoomRequest(cursorId, lastMessageAt, size, chatRoomReadOption, userDetails.getId());

		return CommonResponse.success(chatService.readChatRoom(request));
	}

	@PostMapping("/products/{productId}/chat-room")
	public CommonResponse<CreateChatRoomResponse> createChatRoom(
			@PathVariable Long productId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(chatService.createChatRoom(productId, userDetails.getId()));
	}

	@GetMapping("/chat-room/{chatRoomId}/history")
	public CommonResponse<CursorPageResponse<SendChatMessageResponse>> readChatHistory(
			@PathVariable Long chatRoomId,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

		return CommonResponse.success(null);
	}
}
