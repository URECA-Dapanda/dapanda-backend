package com.dapanda.fcmToken.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SaveFcmTokenRequest(

		@NotBlank(message = "FCM 토큰은 필수입니다.")
		String token
) {

}
