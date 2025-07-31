package com.dapanda.fcmToken.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.fcmToken.dto.SaveFcmTokenRequest;
import com.dapanda.fcmToken.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FcmTokenController {

	private final FcmTokenService fcmTokenService;

	@PostMapping("/fcm/save")
	public CommonResponse<Void> saveFcmToken(@RequestBody SaveFcmTokenRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		fcmTokenService.saveOrUpdateFcmToken(userDetails.getId(), request.token());

		return CommonResponse.success(null);
	}
}
