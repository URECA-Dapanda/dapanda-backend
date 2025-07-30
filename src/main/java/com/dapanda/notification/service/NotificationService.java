package com.dapanda.notification.service;

import com.dapanda.fcm_token.repository.FcmTokenRepository;
import com.dapanda.fcm_token.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final FcmTokenRepository fcmTokenRepository;
	private final FcmTokenService fcmService;

	public void notifyProductSold(Long sellerId, String productName) {
		String token = fcmTokenRepository.findByMemberId(sellerId)
				.orElseThrow(() -> new RuntimeException("FCM 토큰 없음"));

		fcmService.sendNotification(token, "상품 판매 완료", productName + "이(가) 판매되었어요!");
	}
}
