package com.dapanda.auth.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dapanda.RedisTestContainerConfig;
import com.dapanda.TestConfig;
import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberRole;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.refreshToken.entity.RefreshToken;
import com.dapanda.refreshToken.entity.TokenState;
import com.dapanda.refreshToken.repository.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestConfig.class, RedisTestContainerConfig.class})
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

		this.mockMvc = TestConfig.createMockMvc(context, restDocumentation);
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

	// 로그아웃 API
	@Nested
	@DisplayName("로그아웃 API")
	class LogoutTest {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@WithMockUser(username = "testuser", roles = "MEMBER")
			@DisplayName("정상적으로 로그아웃 된다")
			void logout_success() throws Exception {

				String jwt = jwtTokenProvider.generateAccessToken(savedMember);
				RefreshToken token = RefreshToken.of(jwt, TokenState.VALID, savedMember);
				refreshTokenRepository.save(token);

				mockMvc.perform(post("/api/auth/logout")
								.cookie(new Cookie("accessToken", jwt)))
						.andExpect(status().isOk())
						.andDo(document("auth-logout-success"));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("토큰이 없으면 401 에러 반환")
			void logout_tokenNull() throws Exception {

				mockMvc.perform(post("/api/auth/logout"))
						.andExpect(status().isUnauthorized())
						.andDo(document("auth-logout-no-token"));
			}

			@Test
			@DisplayName("토큰이 유효하지 않으면 401 에러 반환")
			void logout_tokenInvalid() throws Exception {

				mockMvc.perform(post("/api/auth/logout")
								.header("Authorization", "Bearer invalidToken"))
						.andExpect(status().isUnauthorized())
						.andDo(document("auth-logout-invalid-token"));
			}
		}
	}
}
