package com.dapanda.refreshToken.service;

import com.dapanda.member.entity.Member;
import com.dapanda.refreshToken.entity.RefreshToken;
import com.dapanda.refreshToken.entity.TokenState;
import com.dapanda.refreshToken.repository.RefreshTokenRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;

	public void save(Member member, String refreshToken) {

		refreshTokenRepository.findByMember(member).ifPresentOrElse(
				token -> refreshTokenRepository.save(token.toBuilder().token(refreshToken).build()),
				() -> refreshTokenRepository.save(
						RefreshToken.builder()
								.member(member)
								.token(refreshToken)
								.build()
				)
		);
	}

	public void invalidateRefreshToken(Member member) {

		refreshTokenRepository.findByMemberAndState(member, TokenState.VALID)
				.ifPresent(token -> {
					token.setState(TokenState.INVALID);
					refreshTokenRepository.save(token);
				});
	}

	public void issueRefreshToken(Member member, String newTokenValue) {

		refreshTokenRepository.findByMemberAndState(member, TokenState.VALID)
				.orElseGet(() -> {
					RefreshToken newToken = RefreshToken.builder()
							.member(member)
							.token(newTokenValue)
							.state(TokenState.VALID)
							.build();

					return refreshTokenRepository.save(newToken);
				});
	}

	public Optional<RefreshToken> findByUser(Member member) {

		return refreshTokenRepository.findByMember(member);
	}
}
