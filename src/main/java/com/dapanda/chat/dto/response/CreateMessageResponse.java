package com.dapanda.chat.dto.response;

import com.dapanda.chat.dto.SendMessageDto;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateMessageResponse {

	private SendMessageDto sendMessageDto;
	private Long senderId;

	public static CreateMessageResponse of(SendMessageDto sendMessageDto, Long senderId) {

		return CreateMessageResponse.builder()
				.sendMessageDto(sendMessageDto)
				.senderId(senderId)
				.build();
	}
}
