package com.dapanda.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ReadChatMessageHistoryResponse {

	private Long chatMessageId;
	private String message;
	private LocalDateTime createdAt;
	private boolean isMine;

	public static ReadChatMessageHistoryResponse of(
			Long chatMessageId,
			String message,
			LocalDateTime createdAt,
			boolean isMine) {

		return ReadChatMessageHistoryResponse.builder()
				.chatMessageId(chatMessageId)
				.message(message)
				.createdAt(createdAt)
				.isMine(isMine)
				.build();
	}

	@JsonProperty("isMine")
	public boolean getIsMine(){

		return isMine;
	}
}
