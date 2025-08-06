package com.dapanda.auth.controller;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.jwt.JwtPrinciple;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.service.MemberService;
import com.dapanda.refreshToken.service.RefreshTokenService;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final MemberService memberService;

	@PostMapping("/auth/logout")
	public void logout(HttpServletRequest request,
					   HttpServletResponse response) throws IOException {

		String token = jwtTokenProvider.resolveTokenFromCookie(request, "accessToken");

		if (token == null) {
			throw new GlobalException(ResultCode.NOT_LOGGED_IN);
		}
		if (!jwtTokenProvider.validateToken(token)) {
			throw new GlobalException(ResultCode.INVALID_TOKEN);
		}

		String email = jwtTokenProvider.getUserEmailFromToken(token);
		OAuthProvider provider = jwtTokenProvider.getProviderFromToken(token);
		Member member = memberService.findUserByEmailAndProvider(email, provider);

		refreshTokenService.invalidateRefreshToken(member);

		boolean isLocal = Optional.ofNullable(request.getHeader("Origin"))
				.map(o -> o.contains("localhost"))
				.orElse(false);

		Cookie accessCookie = new Cookie(JwtPrinciple.ACCESS_TOKEN.getKey(), null);
		accessCookie.setHttpOnly(true);
		accessCookie.setSecure(true);
		accessCookie.setPath("/");
		accessCookie.setMaxAge(0);
		if (!isLocal) {
			accessCookie.setDomain("dapanda.org");
		}
		response.addCookie(accessCookie);

		Cookie refreshCookie = new Cookie(JwtPrinciple.REFRESH_TOKEN.getKey(), null);
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(true);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(0);
		if (!isLocal) {
			accessCookie.setDomain("dapanda.org");
		}
		response.addCookie(refreshCookie);

		request.getSession().invalidate();

		response.sendRedirect("http://localhost:3000");
	}
}
