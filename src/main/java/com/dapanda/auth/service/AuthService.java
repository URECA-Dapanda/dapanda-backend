package com.dapanda.auth.service;

import com.dapanda.auth.dto.response.TokenResponse;
import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.service.MemberService;
import com.dapanda.refreshToken.entity.RefreshToken;
import com.dapanda.refreshToken.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponse reissueAccessToken(String refreshToken) {

        // 1. 토큰 유효성 검사
        if (refreshToken == null) {
            throw new GlobalException(ResultCode.MISSING_TOKEN);
        }
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new GlobalException(ResultCode.INVALID_TOKEN);
        }

        // 2. 사용자 정보 추출 및 검증
        String email = jwtTokenProvider.getUserEmailFromToken(refreshToken);
        OAuthProvider provider = jwtTokenProvider.getProviderFromToken(refreshToken);
        Member member = memberService.findUserByEmailAndProvider(email, provider);

        // 3. 저장된 refresh token과 일치하는지 확인
        RefreshToken savedToken = refreshTokenRepository.findByMember(member)
                .orElseThrow(() -> new GlobalException(ResultCode.MISSING_TOKEN));
        if (!savedToken.getToken().equals(refreshToken)) {
            throw new GlobalException(ResultCode.TOKEN_REISSUE_FAILED);
        }

        // 4. 새 Access Token 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(member);

        return new TokenResponse(newAccessToken);
    }

}
