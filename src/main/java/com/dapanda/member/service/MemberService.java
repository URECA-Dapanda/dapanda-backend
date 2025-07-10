package com.dapanda.member.service;

import com.dapanda.auth.dto.request.LoginRequest;
import com.dapanda.auth.dto.request.SignupRequest;
import com.dapanda.auth.dto.response.LoginResponse;
import com.dapanda.auth.dto.response.SignupResponse;
import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberRole;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.refreshToken.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

        if (member.isBlocked()) {
            throw new GlobalException(ResultCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new GlobalException(ResultCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member);
        String refreshToken = jwtTokenProvider.generateRefreshToken(member);

        refreshTokenService.save(member, refreshToken);

        Cookie cookie = new Cookie("accessToken", accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);
        response.addCookie(cookie);

        return LoginResponse.from(member.getName(), "로그인 성공");
    }

    public SignupResponse registerUser(SignupRequest request) {

        if (memberRepository.existsByEmail(request.email())) {
            throw new GlobalException(ResultCode.DUPLICATE_EMAIL);
        }

        if (memberRepository.existsByName(request.name())) {
            throw new GlobalException(ResultCode.DUPLICATE_USERNAME);
        }

        if (!isValidEmailFormat(request.email())) {
            throw new GlobalException(ResultCode.INVALID_EMAIL_FORMAT);
        }

        if (request.name() == null || request.name().length() > 4) {
            throw new GlobalException(ResultCode.INVALID_MEMBERNAME_FORMAT);
        }

        if (!isValidPassword(request.password())) {
            throw new GlobalException(ResultCode.WEAK_PASSWORD);
        }

        Member member = Member.ofLocalMember(
                request.email(),
                request.name(),
                passwordEncoder.encode(request.password()),
                OAuthProvider.LOCAL,
                MemberRole.ROLE_MEMBER
        );

        memberRepository.save(member);

        return SignupResponse.from(member.getId(), "회원가입이 완료되었습니다.");
    }

    private boolean isValidEmailFormat(String email) {

        return email != null && email.matches(EMAIL_REGEX);
    }

    // 비밀번호 보안 기준: 8자 이상, 숫자+소문자+대문자 각 1개 이상
    private boolean isValidPassword(String password) {

        if (password == null || password.length() < 8) {

            return false;
        }
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);

        return hasDigit && hasLower && hasUpper;
    }

    // 사용자 찾기
    public Member findUserByEmailAndProvider(String email, OAuthProvider provider) {

        return memberRepository.findByEmailAndProvider(email, provider)
                .orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));
    }
}
