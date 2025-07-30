package com.dapanda.fcm_token.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.fcm_token.entity.FcmToken;
import com.dapanda.fcm_token.repository.FcmTokenRepository;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.entity.ItemType;
import com.google.firebase.messaging.*;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenService {

	private final FcmTokenRepository fcmTokenRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void saveOrUpdateFcmToken(Long memberId, String token) {

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		// 기존에 FCM 토큰이 등록되어 있다면 업데이트
		fcmTokenRepository.findByMemberId(memberId)
				.ifPresentOrElse(
						existing -> existing.updateToken(token),
						() -> {
							FcmToken newToken = FcmToken.of(token, member);
							fcmTokenRepository.save(newToken);
						}
				);
	}

	public void notifyProductSold(Long sellerId, LocalDateTime createdAt, ItemType itemType) {

		String token = extractTokenOrThrow(sellerId);
		String date = createdAt.toLocalDate().toString();
		String itemTypeKo = convertItemTypeToKorean(itemType);

		String title = "상품 판매 완료";
		String body = String.format("\"%s %s\"에 올리신 \"%s\" 상품이 팔렸어요", date, itemTypeKo);

		sendNotification(token, title, body);
	}

	public void sendNotification(String token, String title, String body) {

		Message message = Message.builder()
				.setToken(token)
				.setNotification(Notification.builder()
						.setTitle(title)
						.setBody(body)
						.build())
				.putData("click_action", "FLUTTER_NOTIFICATION_CLICK") // 필요 시 설정
				.build();

		try {
			String response = FirebaseMessaging.getInstance().send(message);
			log.info("FCM 메시지 전송 완료: {}", response);
		} catch (FirebaseMessagingException e) {
			log.error("FCM 메시지 전송 실패", e);
		}
	}

	private String extractTokenOrThrow(Long memberId) {

		FcmToken fcmToken = fcmTokenRepository.findByMemberId(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.FCM_NOT_FOUND));

		return fcmToken.getToken();
	}

	private String convertItemTypeToKorean(ItemType type) {

		return switch (type) {

			case MOBILE_DATA -> "모바일 데이터";
			case WIFI -> "와이파이";
			default -> "상품";
		};
	}

}
