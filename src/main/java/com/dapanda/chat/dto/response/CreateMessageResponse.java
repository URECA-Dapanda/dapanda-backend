package com.dapanda.chat.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageResponse {

	private Long chatMessageId;
	private String message;
	private LocalDateTime createdAt;

	public static CreateMessageResponse of(
			Long chatMessageId,
			String message,
			LocalDateTime createdAt) {

		return CreateMessageResponse.builder()
				.chatMessageId(chatMessageId)
				.message(message)
				.createdAt(createdAt)
				.build();
	}
}
