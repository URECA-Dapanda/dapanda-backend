package com.dapanda.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dapanda.auth.dto.response.TokenResponse;
import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberRole;
import com.dapanda.member.service.MemberService;
import com.dapanda.refreshToken.entity.RefreshToken;
import com.dapanda.refreshToken.entity.TokenState;
import com.dapanda.refreshToken.repository.RefreshTokenRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("인증/인가 서비스 테스트")
class AuthServiceTest {

    JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    MemberService memberService = mock(MemberService.class);
    RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);

    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(jwtTokenProvider, memberService, refreshTokenRepository);
    }

    @Nested
    @DisplayName("액세스 토큰 재발급")
    class ReissueAccessTokenTest {

        final String refreshToken = "refreshTokenValue";
        final String newAccessToken = "newAccessToken";
        final String email = "test@example.com";
        final String name = "testName";
        final OAuthProvider provider = OAuthProvider.GOOGLE;
        final MemberRole role = MemberRole.ROLE_MEMBER;
        final Member member = Member.ofOAuthMember(
                email,
                name,
                provider,
                role
        );
        final RefreshToken savedToken = RefreshToken.builder()
                .token(refreshToken)
                .member(member)
                .state(TokenState.VALID)
                .build();

        @Nested
        @DisplayName("성공 케이스")
        class Success {

            @Test
            @DisplayName("정상적으로 액세스 토큰을 재발급한다")
            void shouldReissueAccessTokenSuccessfully() {

                // given
                when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
                when(jwtTokenProvider.getUserEmailFromToken(refreshToken)).thenReturn(email);
                when(jwtTokenProvider.getProviderFromToken(refreshToken)).thenReturn(provider);
                when(memberService.findUserByEmailAndProvider(email, provider)).thenReturn(member);
                when(refreshTokenRepository.findByMember(member)).thenReturn(
                        Optional.of(savedToken));
                when(jwtTokenProvider.generateAccessToken(member)).thenReturn(newAccessToken);

                // when
                TokenResponse response = authService.reissueAccessToken(refreshToken);

                // then
                assertNotNull(response);
                assertEquals(newAccessToken, response.getAccessToken());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class Fail {

            @Test
            @DisplayName("refreshToken이 null이면 예외가 발생한다")
            void shouldThrowIfTokenIsNull() {

                GlobalException exception = assertThrows(GlobalException.class,
                        () -> authService.reissueAccessToken(null));
                assertEquals(ResultCode.MISSING_TOKEN, exception.getResultCode());
            }

            @Test
            @DisplayName("refreshToken이 유효하지 않으면 예외가 발생한다")
            void shouldThrowIfTokenIsInvalid() {

                when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(false);
                GlobalException exception = assertThrows(GlobalException.class,
                        () -> authService.reissueAccessToken(refreshToken));
                assertEquals(ResultCode.INVALID_TOKEN, exception.getResultCode());
            }

            @Test
            @DisplayName("DB에 저장된 refreshToken이 없으면 예외가 발생한다")
            void shouldThrowIfTokenNotFoundInDb() {

                when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
                when(jwtTokenProvider.getUserEmailFromToken(refreshToken)).thenReturn(email);
                when(jwtTokenProvider.getProviderFromToken(refreshToken)).thenReturn(provider);
                when(memberService.findUserByEmailAndProvider(email, provider)).thenReturn(member);
                when(refreshTokenRepository.findByMember(member)).thenReturn(Optional.empty());

                GlobalException exception = assertThrows(GlobalException.class,
                        () -> authService.reissueAccessToken(refreshToken));
                assertEquals(ResultCode.MISSING_TOKEN, exception.getResultCode());
            }

            @Test
            @DisplayName("입력된 refreshToken과 DB의 refreshToken이 다르면 예외가 발생한다")
            void shouldThrowIfTokenDoesNotMatch() {

                String wrongToken = "wrongToken";
                RefreshToken mismatchedToken = RefreshToken.builder()
                        .token(wrongToken)
                        .member(member)
                        .build();

                when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
                when(jwtTokenProvider.getUserEmailFromToken(refreshToken)).thenReturn(email);
                when(jwtTokenProvider.getProviderFromToken(refreshToken)).thenReturn(provider);
                when(memberService.findUserByEmailAndProvider(email, provider)).thenReturn(member);
                when(refreshTokenRepository.findByMember(member)).thenReturn(
                        Optional.of(mismatchedToken));

                GlobalException exception = assertThrows(GlobalException.class,
                        () -> authService.reissueAccessToken(refreshToken));
                assertEquals(ResultCode.TOKEN_REISSUE_FAILED, exception.getResultCode());
            }
        }
    }
}
