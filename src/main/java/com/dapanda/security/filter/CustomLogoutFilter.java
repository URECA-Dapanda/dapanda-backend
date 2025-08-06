package com.dapanda.security.filter;

import com.dapanda.jwt.JwtPrinciple;
import com.dapanda.refreshToken.service.RefreshTokenService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

	@Value("${cookie.domain}")
	private String domain;

	private final RefreshTokenService refreshTokenService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		doLogoutFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
	}

	private void doLogoutFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

		if (isInvalidRequest(request)) {

			filterChain.doFilter(request, response);
			return;
		}

		String refreshToken = getRefreshToken(request);

		if (isInvalidRefreshToken(refreshToken)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

			return;
		}

		doLogout(refreshToken, response);
	}

	/**
	 * 쿠키에서 Refresh_Token 으로된 Key값을 꺼냄
	 */
	private String getRefreshToken(HttpServletRequest request) {

		String refreshToken = null;

		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(JwtPrinciple.REFRESH_TOKEN.getKey())) {
				refreshToken = cookie.getValue();
			}
		}

		return refreshToken;
	}

	/**
	 * 검증 완료된 RefreshToken을 삭제
	 * SecurityContextHolder 비우기
	 * 카카오와 함께 로그아웃 리다이렉트
	 */
	private void doLogout(String refreshToken, HttpServletResponse response) {

		refreshTokenService.deactivateRefreshToken(refreshToken);

		Cookie accessCookie = new Cookie(JwtPrinciple.ACCESS_TOKEN.getKey(), null);
		accessCookie.setHttpOnly(true);
		accessCookie.setSecure(true);
		accessCookie.setMaxAge(0);
		accessCookie.setPath("/");
		accessCookie.setDomain(domain);

		Cookie refreshCookie = new Cookie(JwtPrinciple.REFRESH_TOKEN.getKey(), null);
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(true);
		refreshCookie.setMaxAge(0);
		refreshCookie.setPath("/");
		refreshCookie.setDomain(domain);

		SecurityContextHolder.clearContext();

		response.setStatus(HttpServletResponse.SC_OK);
		response.addCookie(refreshCookie);
		response.addCookie(accessCookie);
	}

	/**
	 * 요청 uri 검사
	 * /api/auth/logout && POST
	 */
	private boolean isInvalidRequest(HttpServletRequest request) {

		String requestUri = request.getRequestURI();
		String requestMethod = request.getMethod();

		return !requestUri.equals("/api/auth/logout") || !requestMethod.equals("POST");
	}

	private boolean isInvalidRefreshToken(String refreshToken) {

		return refreshToken == null || !refreshTokenService.isExistingRefreshToken(refreshToken);

	}
}
