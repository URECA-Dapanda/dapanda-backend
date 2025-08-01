package com.dapanda.fcmToken.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.fcmToken.entity.FcmToken;
import com.dapanda.fcmToken.repository.FcmTokenRepository;
import com.dapanda.fcmToken.repository.NotificationRepository;
import com.dapanda.fcm_token.dto.response.NotificationResponse;
import com.dapanda.fcm_token.entity.NotificationEntity;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.entity.ItemType;
import com.google.firebase.messaging.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenService {

	private final FcmTokenRepository fcmTokenRepository;
	private final NotificationRepository notificationRepository;
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

		Member seller = memberRepository.findById(sellerId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		String token = extractTokenOrThrow(sellerId);
		String date = createdAt.toLocalDate().toString();
		String itemTypeKo = convertItemTypeToKorean(itemType);

		String title = "상품 판매 완료";
		String body = String.format("\"%s\"에 올리신 \"%s\" 상품이 팔렸어요", date, itemTypeKo);

		sendNotification(token, title, body);
		saveNotification(title, body, seller);
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

	private void saveNotification(String title, String body, Member member) {

		NotificationEntity notificationEntity = NotificationEntity.of(title, body, member);
		notificationRepository.save(notificationEntity);
	}


	public List<NotificationResponse> getNotificationsByMemberId(Long memberId) {

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		return notificationRepository.findByMemberOrderByCreatedAtDesc(member).stream()
				.map(NotificationResponse::from)
				.toList();
	}

	public void deleteNotification(Long notificationId, Long memberId) {

		NotificationEntity notificationEntity = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new GlobalException(ResultCode.NOTIFICATION_NOT_FOUND));

		if (!notificationEntity.getMember().getId().equals(memberId)) {
			throw new GlobalException(ResultCode.FORBIDDEN);
		}

		notificationRepository.delete(notificationEntity);
	}


}
