package com.dapanda.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dapanda.RestDocsConfig;
import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberRole;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.refreshToken.entity.RefreshToken;
import com.dapanda.refreshToken.entity.TokenState;
import com.dapanda.refreshToken.repository.RefreshTokenRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({RestDocsConfig.class})
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("인증/인가 컨트롤러 통합 테스트")
class AuthControllerTest {

    static final String BASE_EMAIL = "test@example.com";
    static final String BASE_PASSWORD = "P@ssword1";
    static final String BASE_NAME = "홍길동";
    static final OAuthProvider BASE_PROVIDER = OAuthProvider.LOCAL;
    static final MemberRole BASE_ROLE = MemberRole.ROLE_MEMBER;
    
    MockMvc mockMvc;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    PasswordEncoder passwordEncoder;
    Member savedMember;
    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void restDocsSetUp(RestDocumentationContextProvider restDocumentation) {

        this.mockMvc = RestDocsConfig.createMockMvc(context, restDocumentation);
    }

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        String encodedPassword = passwordEncoder.encode(BASE_PASSWORD);
        savedMember = Member.ofLocalMember(
                BASE_EMAIL,
                BASE_NAME,
                encodedPassword,
                BASE_PROVIDER,
                BASE_ROLE
        );
        memberRepository.save(savedMember);
    }

    @Nested
    @DisplayName("회원가입 API")
    class SignupTest {

        @Test
        @WithMockUser(username = "홍길동", roles = "MEMBER")
        @DisplayName("성공적으로 회원가입된다")
        void signup_success() throws Exception {

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "newuser@aaa.com",
                                        "password": "P@ssword1",
                                        "name": "신규"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("정상 처리 되었습니다."));

            assertThat(memberRepository.findByEmail("newuser@aaa.com")).isPresent();
        }

        @Test
        @DisplayName("중복 이메일이면 에러 반환")
        void signup_duplicateEmail() throws Exception {

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "test@example.com",
                                        "password": "P@ssword1",
                                        "name": "홍길동"
                                    }
                                    """))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("로그인 API")
    class LoginTest {

        @Test
        @WithMockUser(username = "홍길동", roles = "MEMBER")
        @DisplayName("정상적으로 로그인 된다")
        void login_success() throws Exception {

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "test@example.com",
                                        "password": "P@ssword1"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value(BASE_NAME))
                    .andExpect(jsonPath("$.data.message").value("로그인 성공"));
        }

        @Test
        @DisplayName("회원정보가 없으면 404 에러")
        void login_memberNotFound() throws Exception {

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "none@aaa.com",
                                        "password": "P@ssword1"
                                    }
                                    """))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("비밀번호 불일치시 401 에러")
        void login_invalidPassword() throws Exception {

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "test@example.com",
                                        "password": "wrongpassword"
                                    }
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("액세스 토큰 재발급 API")
    class ReissueAccessTokenTest {

        String refreshToken;

        @BeforeEach
        void insertRefreshToken() {

            refreshToken = jwtTokenProvider.generateRefreshToken(savedMember);
            RefreshToken token = RefreshToken.builder()
                    .token(refreshToken)
                    .member(savedMember)
                    .state(TokenState.VALID)
                    .build();
            refreshTokenRepository.save(token);
        }

        @Test
        @WithMockUser(username = "testuser", roles = "MEMBER")
        @DisplayName("정상적으로 액세스 토큰을 재발급한다")
        void 성공적으로_재발급() throws Exception {

            mockMvc.perform(post("/api/auth/reissue")
                            .header("Refresh-Token", refreshToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").exists());
        }

        @Test
        @DisplayName("refreshToken 헤더가 없으면 401 에러 발생")
        void 헤더없음() throws Exception {

            mockMvc.perform(post("/api/auth/reissue"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("refreshToken이 유효하지 않으면 401 에러 발생")
        void 토큰유효하지않음() throws Exception {

            mockMvc.perform(post("/api/auth/reissue")
                            .header("Refresh-Token", "invalidToken"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("저장된 refreshToken이 없으면 401 에러 발생")
        void 저장토큰없음() throws Exception {

            refreshTokenRepository.deleteAll();

            mockMvc.perform(post("/api/auth/reissue")
                            .header("Refresh-Token", refreshToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("저장된 refreshToken과 입력값 다르면 401 에러 발생")
        void 저장토큰다름() throws Exception {

            RefreshToken another = RefreshToken.builder()
                    .token("anotherToken")
                    .member(savedMember)
                    .state(TokenState.VALID)
                    .build();
            refreshTokenRepository.save(another);

            mockMvc.perform(post("/api/auth/reissue")
                            .header("Refresh-Token", "anotherToken"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("refreshToken 상태가 VALID가 아니면 401 에러 발생")
        void 상태비정상() throws Exception {

            Optional<RefreshToken> opt = refreshTokenRepository.findByMember(savedMember);
            assertThat(opt).isPresent();
            RefreshToken token = opt.get();
            token.setState(TokenState.INVALID);
            refreshTokenRepository.save(token);

            mockMvc.perform(post("/api/auth/reissue")
                            .header("Refresh-Token", refreshToken))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("로그아웃 API")
    class LogoutTest {

        String accessToken = "jwt-access-token";

        @Test
        @WithMockUser(username = "testuser", roles = "MEMBER")
        @DisplayName("정상적으로 로그아웃 된다")
        void logout_success() throws Exception {

            String jwt = jwtTokenProvider.generateAccessToken(savedMember);

            RefreshToken token = RefreshToken.builder()
                    .token(jwt)
                    .member(savedMember)
                    .state(TokenState.VALID)
                    .build();
            refreshTokenRepository.save(token);

            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").doesNotExist());

            RefreshToken after = refreshTokenRepository.findByMember(savedMember).orElse(null);
            assertThat(after == null || after.getState() != TokenState.VALID).isTrue();
        }

        @Test
        @DisplayName("토큰이 없으면 401 에러 반환")
        void logout_tokenNull() throws Exception {

            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("토큰이 유효하지 않으면 401 에러 반환")
        void logout_tokenInvalid() throws Exception {

            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer invalidToken"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
