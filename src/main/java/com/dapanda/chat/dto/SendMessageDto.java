package com.dapanda.chat.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageDto {

	private Long chatMessageId;
	private String message;
	private LocalDateTime createdAt;

	public static SendMessageDto of(
			Long chatMessageId,
			String message,
			LocalDateTime createdAt) {

		return SendMessageDto.builder()
				.chatMessageId(chatMessageId)
				.message(message)
				.createdAt(createdAt)
				.build();
	}
}
