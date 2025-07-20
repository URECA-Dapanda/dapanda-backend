package com.dapanda.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(

		@NotNull(message = "회원 아이디는 필수입니다.")
		Long memberId,

		@NotNull(message = "채팅 메시지는 필수입니다.")
		@Size(min = 1, max = 250, message = "채팅 메시지는 1~250자 까지 가능합니다")
		String message) {
}
