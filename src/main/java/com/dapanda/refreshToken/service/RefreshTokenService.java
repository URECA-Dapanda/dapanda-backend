package com.dapanda.refreshToken.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.refreshToken.entity.RefreshToken;
import com.dapanda.refreshToken.entity.TokenState;
import com.dapanda.refreshToken.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;

	public void save(Member member, String refreshToken) {

		refreshTokenRepository.findByMember(member).ifPresentOrElse(
				existing -> {
					RefreshToken updated = RefreshToken.of(refreshToken, TokenState.VALID, member);
					refreshTokenRepository.save(updated);
				},
				() -> {
					RefreshToken created = RefreshToken.of(refreshToken, TokenState.VALID, member);
					refreshTokenRepository.save(created);
				}
		);
	}

	@Transactional
	public void deactivateRefreshToken(String token) {

		RefreshToken refreshToken = refreshTokenRepository.findByTokenAndState(token, TokenState.VALID)
				.orElseThrow(() -> new GlobalException(ResultCode.REFRESH_TOKEN_NOT_FOUND));

		refreshToken.deactivateToken();
	}

	public boolean isExistingRefreshToken(String token) {

		return refreshTokenRepository.existsByTokenAndState(token, TokenState.VALID);
	}

	public void issueRefreshToken(Member member, String newTokenValue) {

		refreshTokenRepository.findByMemberAndState(member, TokenState.VALID)
				.ifPresent(refreshToken -> {
					refreshToken.deactivateToken();
					refreshTokenRepository.save(refreshToken);
				});

		refreshTokenRepository.save(
				RefreshToken.of(newTokenValue, TokenState.VALID, member)
		);
	}


	public Optional<RefreshToken> findByUserAndState(Member member) {

		return refreshTokenRepository.findByMemberAndState(member, TokenState.VALID);
	}
}
