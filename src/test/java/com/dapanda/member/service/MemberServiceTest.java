package com.dapanda.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import com.dapanda.refreshToken.repository.RefreshTokenRepository;
import com.dapanda.refreshToken.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@DisplayName("Member 서비스 테스트")
class MemberServiceTest {

    static final String EMAIL = "user@aaa.com";
    static final String PASSWORD = "P@ssword1";
    static final String NAME = "홍길동";
    static final MemberRole ROLE = MemberRole.ROLE_MEMBER;

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RefreshTokenService refreshTokenService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {

        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("회원가입(registerUser)")
    class RegisterUserTest {

        @Test
        @DisplayName("정상적으로 회원가입된다")
        void registerUser_success() {

            SignupRequest request = new SignupRequest(EMAIL, PASSWORD, NAME);

            SignupResponse response = memberService.registerUser(request);

            assertThat(response).isNotNull();
            assertThat(response.getMessage()).contains("완료");
            assertThat(memberRepository.findByEmail(EMAIL)).isPresent();
        }

        @Test
        @DisplayName("중복 이메일이면 예외 발생")
        void duplicateEmail() {

            memberRepository.save(Member.ofLocalMember(
                    EMAIL,
                    NAME,
                    passwordEncoder.encode(PASSWORD),
                    OAuthProvider.LOCAL,
                    ROLE
            ));

            SignupRequest request = new SignupRequest(EMAIL, PASSWORD, "다른이름");
            GlobalException ex = assertThrows(GlobalException.class,
                    () -> memberService.registerUser(request));
            assertThat(ex.getResultCode()).isEqualTo(ResultCode.DUPLICATE_EMAIL);
        }

        @Test
        @DisplayName("중복 닉네임이면 예외 발생")
        void duplicateName() {

            memberRepository.save(Member.ofLocalMember(
                    EMAIL,
                    NAME,
                    passwordEncoder.encode(PASSWORD),
                    OAuthProvider.LOCAL,
                    ROLE));

            SignupRequest request = new SignupRequest("diff@aaa.com", PASSWORD, NAME);
            GlobalException ex = assertThrows(GlobalException.class,
                    () -> memberService.registerUser(request));
            assertThat(ex.getResultCode()).isEqualTo(ResultCode.DUPLICATE_USERNAME);
        }

        @Test
        @DisplayName("이메일 형식이 아니면 예외 발생")
        void invalidEmail() {

            SignupRequest request = new SignupRequest("invalid-email", PASSWORD, NAME);
            GlobalException ex = assertThrows(GlobalException.class,
                    () -> memberService.registerUser(request));
            assertThat(ex.getResultCode()).isEqualTo(ResultCode.INVALID_EMAIL_FORMAT);
        }

        @Test
        @DisplayName("닉네임 길이가 5자 이상이면 예외 발생")
        void nameTooLong() {

            SignupRequest request = new SignupRequest("ok@ok.com", PASSWORD, "긴이름123");
            GlobalException ex = assertThrows(GlobalException.class,
                    () -> memberService.registerUser(request));
            assertThat(ex.getResultCode()).isEqualTo(ResultCode.INVALID_MEMBERNAME_FORMAT);
        }

        @Test
        @DisplayName("비밀번호 보안 기준 미달이면 예외 발생")
        void weakPassword() {

            SignupRequest request = new SignupRequest("ok@ok.com", "1234", NAME);
            GlobalException ex = assertThrows(GlobalException.class,
                    () -> memberService.registerUser(request));
            assertThat(ex.getResultCode()).isEqualTo(ResultCode.WEAK_PASSWORD);
        }
    }

    @Nested
    @DisplayName("로그인(login)")
    class LoginTest {

        @BeforeEach
        void setupMember() {

            Member member = Member.ofLocalMember(
                    EMAIL,
                    NAME,
                    passwordEncoder.encode(PASSWORD),
                    OAuthProvider.LOCAL,
                    ROLE);
            memberRepository.save(member);
        }

        @Test
        @DisplayName("정상적으로 로그인된다")
        void login_success() {

            LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
            HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

            LoginResponse loginResponse = memberService.login(request, response);

            assertThat(loginResponse).isNotNull();
            assertThat(loginResponse.getName()).isEqualTo(NAME);
            assertThat(loginResponse.getMessage()).contains("성공");
        }

        @Test
        @DisplayName("회원이 없으면 예외 발생")
        void memberNotFound() {

            LoginRequest request = new LoginRequest("none@aaa.com", PASSWORD);
            HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

            GlobalException ex = assertThrows(GlobalException.class,
                    () -> memberService.login(request, response));
            assertThat(ex.getResultCode()).isEqualTo(ResultCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("비밀번호 불일치 시 예외 발생")
        void invalidPassword() {

            LoginRequest request = new LoginRequest(EMAIL, "Wrong123");
            HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

            GlobalException ex = assertThrows(GlobalException.class,
                    () -> memberService.login(request, response));
            assertThat(ex.getResultCode()).isEqualTo(ResultCode.INVALID_PASSWORD);
        }
    }

    @Nested
    @DisplayName("findUserByEmailAndProvider")
    class FindUserByEmailAndProviderTest {

        @Test
        @DisplayName("정상적으로 찾을 수 있다")
        void find_success() {

            Member member = Member.ofLocalMember(
                    EMAIL,
                    NAME,
                    passwordEncoder.encode(PASSWORD),
                    OAuthProvider.LOCAL,
                    ROLE);
            memberRepository.save(member);

            Member found = memberService.findUserByEmailAndProvider(EMAIL, OAuthProvider.LOCAL);
            assertThat(found).isNotNull();
            assertThat(found.getEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("없는 경우 예외")
        void not_found() {

            GlobalException ex = assertThrows(GlobalException.class,
                    () -> memberService.findUserByEmailAndProvider("none@none.com",
                            OAuthProvider.LOCAL));
            assertThat(ex.getResultCode()).isEqualTo(ResultCode.MEMBER_NOT_FOUND);
        }
    }
}
