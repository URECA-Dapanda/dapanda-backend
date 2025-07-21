package com.dapanda.auth.controller;

import com.dapanda.auth.dto.request.LoginRequest;
import com.dapanda.auth.dto.request.SignupRequest;
import com.dapanda.auth.dto.response.LoginResponse;
import com.dapanda.auth.dto.response.SignupResponse;
import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.jwt.JwtPrinciple;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.service.MemberService;
import com.dapanda.refreshToken.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final MemberService memberService;

	@PostMapping("/auth/signup")
	public CommonResponse<SignupResponse> signup(
			@RequestBody SignupRequest request) {

		SignupResponse result = memberService.registerUser(request);

		return CommonResponse.success(result);
	}

	@PostMapping("/auth/login")
	public CommonResponse<LoginResponse> login(
			@RequestBody LoginRequest request,
			HttpServletResponse response
	) {

		LoginResponse result = memberService.login(request, response);

		return CommonResponse.success(result);
	}

	@PostMapping("/auth/logout")
	public CommonResponse<Void> logout(HttpServletRequest request,
			HttpServletResponse response) {

		String token = jwtTokenProvider.resolveToken(request);

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

		Cookie cookie = new Cookie(JwtPrinciple.ACCESS_TOKEN.getKey(), null);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);

		request.getSession().invalidate();

		return CommonResponse.success(null);
	}
}
