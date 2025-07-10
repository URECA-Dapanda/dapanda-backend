package com.dapanda.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
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

	// 회원가입 API
	@Nested
	@DisplayName("회원가입 API")
	class SignupTest {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@WithMockUser(username = "홍길동", roles = "MEMBER")
			@DisplayName("정상적으로 회원가입된다")
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
						.andDo(document("auth-signup-success",
								requestFields(
										fieldWithPath("email").description("회원 이메일"),
										fieldWithPath("password").description("비밀번호"),
										fieldWithPath("name").description("회원 이름")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("결과 메시지"),
										fieldWithPath("data.id").description("생성된 회원 ID")
												.optional(),
										fieldWithPath("data.message").description("가입 메시지")
												.optional()
								)
						));
				assertThat(memberRepository.findByEmail("newuser@aaa.com")).isPresent();
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("이미 가입된 이메일이면 409 + DUPLICATE_EMAIL(2000) 반환")
			void signup_duplicateEmail() throws Exception {
				memberRepository.save(
						Member.ofLocalMember("exist@aaa.com", "ex", "hashed", OAuthProvider.LOCAL,
								MemberRole.ROLE_MEMBER));

				mockMvc.perform(post("/api/auth/signup")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{
										    "email": "exist@aaa.com",
										    "password": "P@ssword1",
										    "name": "중복"
										}
										"""))
						.andExpect(status().isConflict())
						.andExpect(jsonPath("$.code").value(2000))
						.andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."))
						.andDo(document("auth-signup-duplicate-email"));
			}

			@Test
			@DisplayName("이미 사용 중인 사용자명이면 409 + DUPLICATE_USERNAME(2001) 반환")
			void signup_duplicateUsername() throws Exception {
				memberRepository.save(
						Member.ofLocalMember("other@aaa.com", "중복이름", "hashed", OAuthProvider.LOCAL,
								MemberRole.ROLE_MEMBER));

				mockMvc.perform(post("/api/auth/signup")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{
										    "email": "unique@aaa.com",
										    "password": "P@ssword1",
										    "name": "중복이름"
										}
										"""))
						.andExpect(status().isConflict())
						.andExpect(jsonPath("$.code").value(2001))
						.andExpect(jsonPath("$.message").value("이미 사용 중인 사용자명입니다."))
						.andDo(document("auth-signup-duplicate-username"));
			}

			@Test
			@DisplayName("비밀번호 보안 기준 미달이면 400 + WEAK_PASSWORD(2002) 반환")
			void signup_weakPassword() throws Exception {
				mockMvc.perform(post("/api/auth/signup")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{
										    "email": "weakpw@aaa.com",
										    "password": "weak",
										    "name": "사용자"
										}
										"""))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(2002))
						.andExpect(jsonPath("$.message").value("비밀번호가 보안 기준에 맞지 않습니다."))
						.andDo(document("auth-signup-weak-password"));
			}

			@Test
			@DisplayName("이메일 형식이 올바르지 않으면 400 + INVALID_EMAIL_FORMAT(2003) 반환")
			void signup_invalidEmailFormat() throws Exception {
				mockMvc.perform(post("/api/auth/signup")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{
										    "email": "invalidemail",
										    "password": "P@ssword1",
										    "name": "사용자"
										}
										"""))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(2003))
						.andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."))
						.andDo(document("auth-signup-invalid-email"));
			}

			@Test
			@DisplayName("아이디 형식(길이)이 올바르지 않으면 400 + INVALID_MEMBERNAME_FORMAT(2004) 반환")
			void signup_invalidMemberNameFormat() throws Exception {
				mockMvc.perform(post("/api/auth/signup")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{
										    "email": "name@aaa.com",
										    "password": "P@ssword1",
										    "name": "이름길이초과"
										}
										"""))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(2004))
						.andExpect(jsonPath("$.message").value("아이디 형식이 올바르지 않습니다."))
						.andDo(document("auth-signup-invalid-membername"));
			}
		}

	}

	// 로그인 API
	@Nested
	@DisplayName("로그인 API")
	class LoginTest {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

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
						.andDo(document("auth-login-success",
								requestFields(
										fieldWithPath("email").description("회원 이메일"),
										fieldWithPath("password").description("비밀번호")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("결과 메시지"),
										fieldWithPath("data.name").description("이름"),
										fieldWithPath("data.message").description("로그인 결과 메시지")
								)
						));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

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
						.andExpect(status().isNotFound())
						.andDo(document("auth-login-not-found"));
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
						.andExpect(status().isUnauthorized())
						.andDo(document("auth-login-invalid-password"));
			}
		}
	}

	// 액세스 토큰 재발급 API
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

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@WithMockUser(username = "testuser", roles = "MEMBER")
			@DisplayName("정상적으로 액세스 토큰을 재발급한다")
			void shouldReissueAccessTokenSuccessfully() throws Exception {

				mockMvc.perform(post("/api/auth/reissue")
								.header("Refresh-Token", refreshToken))
						.andExpect(status().isOk())
						.andDo(document("auth-reissue-success",
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("결과 메시지"),
										fieldWithPath("data.accessToken").description("재발급된 액세스 토큰")
								)
						));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("refreshToken 헤더가 없으면 401 에러 발생")
			void 헤더없음() throws Exception {

				mockMvc.perform(post("/api/auth/reissue"))
						.andExpect(status().isUnauthorized())
						.andDo(document("auth-reissue-no-header"));
			}

			@Test
			@DisplayName("refreshToken이 유효하지 않으면 401 에러 발생")
			void 토큰유효하지않음() throws Exception {

				mockMvc.perform(post("/api/auth/reissue")
								.header("Refresh-Token", "invalidToken"))
						.andExpect(status().isUnauthorized())
						.andDo(document("auth-reissue-invalid-token"));
			}

			@Test
			@DisplayName("저장된 refreshToken이 없으면 401 에러 발생")
			void 저장토큰없음() throws Exception {

				refreshTokenRepository.deleteAll();
				mockMvc.perform(post("/api/auth/reissue")
								.header("Refresh-Token", refreshToken))
						.andExpect(status().isUnauthorized())
						.andDo(document("auth-reissue-no-saved-token"));
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
						.andExpect(status().isUnauthorized())
						.andDo(document("auth-reissue-token-mismatch"));
			}

			@Test
			@DisplayName("refreshToken 상태가 VALID가 아니면 401 에러 발생")
			void 상태비정상() throws Exception {

				RefreshToken token = refreshTokenRepository.findByMember(savedMember).orElseThrow();
				token.setState(TokenState.INVALID);
				refreshTokenRepository.save(token);

				mockMvc.perform(post("/api/auth/reissue")
								.header("Refresh-Token", refreshToken))
						.andExpect(status().isUnauthorized())
						.andDo(document("auth-reissue-invalid-state"));
			}
		}
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
				RefreshToken token = RefreshToken.builder()
						.token(jwt)
						.member(savedMember)
						.state(TokenState.VALID)
						.build();
				refreshTokenRepository.save(token);

				mockMvc.perform(post("/api/auth/logout")
								.header("Authorization", "Bearer " + jwt))
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
