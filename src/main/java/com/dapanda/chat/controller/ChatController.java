package com.dapanda.chat.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.chat.dto.request.ReadJoiningChatRoomRequest;
import com.dapanda.chat.dto.response.CreateChatRoomResponse;
import com.dapanda.chat.dto.response.ReadJoiningChatRoomResponse;
import com.dapanda.chat.entity.ChatRoomReadOption;
import com.dapanda.chat.service.ChatService;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.CommonResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	@GetMapping("/chat-room")
	public ResponseEntity<CommonResponse<CursorPageResponse<ReadJoiningChatRoomResponse>>> readChatRoom(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(required = false) LocalDateTime lastMessageAt,
			@RequestParam(defaultValue = "2") @Min(1) @Max(100) int size,
			@RequestParam(defaultValue = "ALL") ChatRoomReadOption chatRoomReadOption) {

		ReadJoiningChatRoomRequest request = new ReadJoiningChatRoomRequest(cursorId, lastMessageAt,
				size, chatRoomReadOption, userDetails.getId());

		return ResponseEntity.ok(CommonResponse.success(chatService.readChatRoom(request)));
	}

	@PostMapping("/products/{productId}/chat-room")
	public ResponseEntity<CommonResponse<CreateChatRoomResponse>> createChatRoom(
			@PathVariable Long productId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ResponseEntity.ok(
				CommonResponse.success(chatService.createChatRoom(productId, userDetails.getId())));
	}
}
