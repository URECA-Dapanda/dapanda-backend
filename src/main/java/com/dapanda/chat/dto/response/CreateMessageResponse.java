package com.dapanda.chat.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateMessageResponse {

	private Long chatRoomId;
	private Long chatMessageId;
	private Long senderId;
	private String message;
	private LocalDateTime createdAt;

	public static CreateMessageResponse of(
			Long chatRoomId,
			Long chatMessageId,
			Long senderId,
			String message,
			LocalDateTime createdAt) {

		return CreateMessageResponse.builder()
				.chatRoomId(chatRoomId)
				.chatMessageId(chatMessageId)
				.senderId(senderId)
				.message(message)
				.createdAt(createdAt)
				.build();
	}
}
