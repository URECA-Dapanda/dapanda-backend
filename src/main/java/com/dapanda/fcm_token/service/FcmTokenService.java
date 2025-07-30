package com.dapanda.fcm_token.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.fcm_token.entity.FcmToken;
import com.dapanda.fcm_token.repository.FcmTokenRepository;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
