package com.dapanda.fcmToken.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.fcmToken.dto.request.SaveFcmTokenRequest;
import com.dapanda.fcmToken.dto.response.NotificationResponse;
import com.dapanda.fcmToken.service.FcmTokenService;
import java.util.List;
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

	@GetMapping("/notifications")
	public CommonResponse<List<NotificationResponse>> getMyNotifications(
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		List<NotificationResponse> notifications = fcmTokenService.getNotificationsByMemberId(
				userDetails.getId());
		return CommonResponse.success(notifications);
	}

	@DeleteMapping("/notifications/{notificationId}")
	public CommonResponse<Void> deleteNotification(
			@PathVariable Long notificationId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		fcmTokenService.deleteNotification(notificationId, userDetails.getId());
		return CommonResponse.success(null);
	}
}
