package com.dapanda.auth.handler;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.refreshToken.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final MemberRepository memberRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException {

		DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
		String email = (String) oAuth2User.getAttributes().get("email");

		String uri = request.getRequestURI();
		String providerStr = null;
		if (uri.contains("/login/oauth2/code/")) {
			providerStr = uri.substring(uri.lastIndexOf("/") + 1);
		} else {
			providerStr = "unknown";
		}

		OAuthProvider provider = OAuthProvider.valueOf(providerStr.toUpperCase());

		log.info("email: {}, provider: {}", email, provider);

		Member member = memberRepository.findByEmailAndProvider(email, provider)
				.orElseThrow(() -> new IllegalArgumentException("OAuth 로그인 유저 DB에 없음"));

		String accessToken = jwtTokenProvider.generateAccessToken(member);
		String refreshToken = jwtTokenProvider.generateRefreshToken(member);

		refreshTokenService.issueRefreshToken(member, refreshToken);

		Cookie cookie = new Cookie("accessToken", accessToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60);

		response.addCookie(cookie);

		response.sendRedirect("/"); // 클라이언트 페이지
	}
}
