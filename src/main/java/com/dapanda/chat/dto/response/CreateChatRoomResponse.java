package com.dapanda.chat.dto.response;

import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateChatRoomResponse {

	private Long chatRoomId;

	public static CreateChatRoomResponse of(Long chatRoomId) {

		return CreateChatRoomResponse.builder()
				.chatRoomId(chatRoomId)
				.build();
	}
}
