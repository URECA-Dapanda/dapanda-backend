package com.dapanda.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateReadStatusRequest(

		@NotNull(message = "마지막 읽은 메시지 아이디는 필수입니다.")
		Long chatMessageId) {
}
