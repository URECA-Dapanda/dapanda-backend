package com.dapanda.chat.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class SendChatMessageResponse {

	private Long chatMessageId;
	private Long senderId;
	private String message;
	private LocalDateTime createdAt;

	public static SendChatMessageResponse of(
			Long chatMessageId,
			Long senderId,
			String message,
			LocalDateTime createdAt) {

		return SendChatMessageResponse.builder()
				.chatMessageId(chatMessageId)
				.senderId(senderId)
				.message(message)
				.createdAt(createdAt)
				.build();
	}
}
