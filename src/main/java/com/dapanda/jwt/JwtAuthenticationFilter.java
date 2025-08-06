package com.dapanda.jwt;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.service.MemberService;
import com.dapanda.refreshToken.entity.TokenState;
import com.dapanda.refreshToken.service.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Value("${cookie.domain}")
	private String domain;
	private static final int MAX_REPORT_COUNT = 5;
	private final JwtTokenProvider jwtTokenProvider;
	private final MemberService memberService;
	private final RefreshTokenService refreshTokenService;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {

		String uri = request.getRequestURI();

		return uri.startsWith("/actuator/health") || uri.startsWith("/oauth2") || uri.startsWith("/docs");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		String accessToken = jwtTokenProvider.resolveTokenFromCookie(request,
				JwtPrinciple.ACCESS_TOKEN.getKey());
		String refreshToken = jwtTokenProvider.resolveTokenFromCookie(request,
				JwtPrinciple.REFRESH_TOKEN.getKey());
		boolean authenticated = false;

		// 1. Access Token 검증
		if (accessToken != null) {
			try {
				if (jwtTokenProvider.validateToken(accessToken)) {
					setAuthentication(accessToken, request);
					authenticated = true;
				}
			} catch (ExpiredJwtException ex) {
				log.info("AccessToken 만료: RefreshToken 검사 진행");
				// 아래에서 RefreshToken 처리
			}
		}

		// 2. Access Token이 만료(또는 없음) & Refresh Token으로 재발급 시도
		if (!authenticated && refreshToken != null) {
			try {
				if (jwtTokenProvider.validateToken(refreshToken)) {
					String email = jwtTokenProvider.getUserEmailFromToken(refreshToken);
					OAuthProvider provider = jwtTokenProvider.getProviderFromToken(refreshToken);
					Member member = memberService.findUserByEmailAndProvider(email, provider);

					validateBlockedMember(member);

					var savedToken = refreshTokenService.findByUserAndState(member).orElse(null);
					if (savedToken != null &&
							savedToken.getToken().equals(refreshToken) &&
							savedToken.getState() == TokenState.VALID) {

						// Access Token 재발급
						String newAccessToken = jwtTokenProvider.generateAccessToken(member);

						// 쿠키에 새 토큰 세팅
						setJwtCookie(response, JwtPrinciple.ACCESS_TOKEN.getKey(), newAccessToken,
								jwtTokenProvider.getAccessTokenExpirationSec());

						// (Refresh Token은 만료 전이면 그대로 둠, 만료 시 재발급 로직 추가 가능)

						setAuthentication(newAccessToken, request);
						log.info("AccessToken 자동 재발급 및 인증 완료");
					}
				}
			} catch (ExpiredJwtException ex) {
				log.info("RefreshToken도 만료: 재로그인 필요");
			} catch (Exception ex) {
				log.error("RefreshToken 검증 중 오류: {}", ex.getMessage());
			}
		}

		// 인증 실패(두 토큰 모두 만료 또는 유효하지 않음) 시 아무 동작 없음(401 응답은 Spring Security의 ExceptionHandler에서 처리)
		filterChain.doFilter(request, response);
	}

	private void setAuthentication(String accessToken, HttpServletRequest request) {

		String email = jwtTokenProvider.getUserEmailFromToken(accessToken);
		OAuthProvider provider = jwtTokenProvider.getProviderFromToken(accessToken);

		CustomUserDetails userDetails = jwtTokenProvider.getAuthentication(email, provider);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());

		auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	private void setJwtCookie(HttpServletResponse response, String name, String token, int maxAgeSec) {

		Cookie cookie = new Cookie(name, token);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(maxAgeSec);
		cookie.setDomain(domain);

		response.addCookie(cookie);
	}

	private void validateBlockedMember(Member member) {

		if (member.isBlocked() || member.getReportedCount() >= MAX_REPORT_COUNT) {

			throw new AuthenticationServiceException("차단된 사용자 입니다");
		}
	}

}
