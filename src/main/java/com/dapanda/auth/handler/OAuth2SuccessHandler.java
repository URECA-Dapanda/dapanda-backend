package com.dapanda.auth.handler;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.jwt.JwtPrinciple;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberRole;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.plan.service.PlanService;
import com.dapanda.refreshToken.service.RefreshTokenService;
import jakarta.servlet.http.*;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final MemberRepository memberRepository;
	private final PlanService planService;

	@Override
	@Transactional
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException {

		//TODO 리팩터링 필요

		DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
		String email = (String) oAuth2User.getAttributes().get("email");
		String role = oAuth2User.getAuthorities().stream().iterator().next().getAuthority();

		String uri = request.getRequestURI();
		String providerStr;
		if (uri.contains("/login/oauth2/code/")) {
			providerStr = uri.substring(uri.lastIndexOf("/") + 1);
		} else {
			providerStr = "unknown";
		}

		OAuthProvider provider = OAuthProvider.valueOf(providerStr.toUpperCase());

		log.info("email: {}, provider: {}", email, provider);

		Member member = memberRepository.findByEmailAndProvider(email, provider)
				.orElseThrow(() -> new IllegalArgumentException("OAuth 로그인 유저 DB에 없음"));

		planService.createRandomPlanForMember(member);

		String accessToken = jwtTokenProvider.generateAccessToken(member);
		String refreshToken = jwtTokenProvider.generateRefreshToken(member);

		log.info("accessToken: {}", accessToken);
		log.info("refreshToken: {}", refreshToken);

		refreshTokenService.issueRefreshToken(member, refreshToken);

		String origin = request.getHeader("Origin");
		String host = request.getHeader("Host");
		boolean isLocal = (origin != null && origin.contains("localhost")) ||
				(host != null && host.contains("localhost"));

		Cookie accessCookie = new Cookie(JwtPrinciple.ACCESS_TOKEN.getKey(), accessToken);
		accessCookie.setHttpOnly(true);
		accessCookie.setSecure(true);
		accessCookie.setPath("/");
		accessCookie.setMaxAge(jwtTokenProvider.getAccessTokenExpirationSec());
		if (!isLocal) {
			accessCookie.setDomain("dapanda.org");
		}
		response.addCookie(accessCookie);

		Cookie refreshCookie = new Cookie(JwtPrinciple.REFRESH_TOKEN.getKey(), refreshToken);
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(true);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(jwtTokenProvider.getRefreshTokenExpirationSec());
		if (!isLocal) {
			refreshCookie.setDomain("dapanda.org");
		}
		response.addCookie(refreshCookie);

		String redirectUrl;
		if (isLocal) {
			redirectUrl = "http://localhost:3000/data";
		} else {
			redirectUrl = "https://dapanda.org/data";
		}

		if (role.equals(MemberRole.ROLE_NEW_MEMBER.name())) {
			redirectUrl += "?on-boarding=true";
		}

		response.sendRedirect(redirectUrl);
	}
}
