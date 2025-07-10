package com.dapanda.auth.controller;

import com.dapanda.auth.dto.request.LoginRequest;
import com.dapanda.auth.dto.request.SignupRequest;
import com.dapanda.auth.dto.response.LoginResponse;
import com.dapanda.auth.dto.response.SignupResponse;
import com.dapanda.auth.dto.response.TokenResponse;
import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.auth.service.AuthService;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.service.MemberService;
import com.dapanda.refreshToken.entity.TokenState;
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
	private final AuthService authService;

	@PostMapping("/auth/reissue")
	public CommonResponse<TokenResponse> reissueAccessToken(
			HttpServletRequest request) {

		String refreshToken = request.getHeader("Refresh-Token");
		log.info("요청 {}", request.getRequestURI());
		log.info("리프레시 토큰 : {}", refreshToken);

		// 유효성 검사
		if (refreshToken == null) {
			throw new GlobalException(ResultCode.MISSING_TOKEN);
		}
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new GlobalException(ResultCode.INVALID_TOKEN);
		}

		String email = jwtTokenProvider.getUserEmailFromToken(refreshToken);
		OAuthProvider provider = jwtTokenProvider.getProviderFromToken(refreshToken);
		Member member = memberService.findUserByEmailAndProvider(email, provider);

		var savedToken = refreshTokenService.findByUser(member)
				.orElseThrow(() -> new GlobalException(ResultCode.MISSING_TOKEN));

		if (!savedToken.getToken().equals(refreshToken)) {
			throw new GlobalException(ResultCode.TOKEN_REISSUE_FAILED);
		}
		if (savedToken.getState() != TokenState.VALID) {
			throw new GlobalException(ResultCode.INVALID_TOKEN);
		}

		TokenResponse tokenResponse = authService.reissueAccessToken(refreshToken);

		return CommonResponse.success(tokenResponse);
	}

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

		Cookie cookie = new Cookie("accessToken", null);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);

		request.getSession().invalidate();

		return CommonResponse.success(null);
	}
}
