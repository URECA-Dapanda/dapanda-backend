package com.dapanda.fcm_token.dto;

import jakarta.validation.constraints.NotEmpty;

public record SaveFcmTokenRequest(

		@NotEmpty(message = "FCM 토큰은 필수입니다.")
		String token
) {

}
