package com.dapanda.auth.controller;

import com.dapanda.base.BaseIntegrationTest;
import com.dapanda.jwt.JwtPrinciple;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.refreshToken.entity.RefreshToken;
import com.dapanda.refreshToken.entity.TokenState;
import com.dapanda.refreshToken.repository.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("인증/인가 컨트롤러 통합 테스트")
class AuthControllerTest extends BaseIntegrationTest {

	@Autowired
	MemberRepository memberRepository;
	@Autowired
	RefreshTokenRepository refreshTokenRepository;
	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Nested
	@DisplayName("로그아웃 API")
	class LogoutTest {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("정상적으로 로그아웃 된다")
			void logoutSuccess() throws Exception {

				//given
				Member member = memberRepository.save(MemberFixture.createMember1());

				String jwt = jwtTokenProvider.generateAccessToken(member);

				refreshTokenRepository.save(RefreshToken.of(jwt, TokenState.VALID, member));

				//when & then
				mockMvc.perform(post("/api/auth/logout")
								.cookie(new Cookie(JwtPrinciple.ACCESS_TOKEN.getKey(), jwt)))
						.andExpect(status().is3xxRedirection())
						.andDo(document("auth-logout-success"));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("토큰이 없으면 401 에러 반환")
			void logoutTokenNull() throws Exception {

				mockMvc.perform(post("/api/auth/logout"))
						.andExpect(status().isUnauthorized())
						.andDo(document("auth-logout-no-token"));
			}

			@Test
			@DisplayName("토큰이 유효하지 않으면 401 에러 반환")
			void logoutTokenInvalid() throws Exception {

				mockMvc.perform(post("/api/auth/logout")
								.cookie(new Cookie(JwtPrinciple.ACCESS_TOKEN.getKey(), "invalid.token.test")))
						.andExpect(status().isUnauthorized())
						.andDo(document("auth-logout-invalid-token"));
			}
		}
	}
}
