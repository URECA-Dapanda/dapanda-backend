package com.dapanda.fcm.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dapanda.TestConfig;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.fcmToken.dto.SaveFcmTokenRequest;
import com.dapanda.fcmToken.service.FcmTokenService;
import com.dapanda.member.entity.*;
import com.dapanda.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("FCM 토큰 컨트롤러 테스트")
class FcmTokenControllerTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private FcmTokenService fcmTokenService;

	@Autowired
	private MemberRepository memberRepository;

	@BeforeEach
	void restDocsSetUp(RestDocumentationContextProvider restDocumentation) {

		this.mockMvc = TestConfig.createMockMvc(context, restDocumentation);

		cleanupDatabase();
	}

	@AfterEach
	void clearSecurityContext() {

		SecurityContextHolder.clearContext();
	}

	private void cleanupDatabase() {

		entityManager.clear();

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

		jdbcTemplate.execute("TRUNCATE TABLE fcm_token");

		jdbcTemplate.execute("TRUNCATE TABLE member");

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
	}

	@Nested
	@DisplayName("FCM 토큰 저장 API")
	class SaveFcmToken {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("FCM 토큰을 받아서 서버에 성공적으로 저장한다.")
			void saveFcmToken_success() throws Exception {

				// given
				SaveFcmTokenRequest request = new SaveFcmTokenRequest("valid_token");

				Member member = MemberFixture.createMember1();
				memberRepository.save(member);

				CustomUserDetails userDetails = CustomUserDetails.from(member);
				mockMvc.perform(post("/api/fcm/save")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isOk())
						.andDo(document("fcm-token-save",
								preprocessRequest(prettyPrint()),
								preprocessResponse(prettyPrint()),
								requestFields(
										fieldWithPath("token").type(JsonFieldType.STRING)
												.description("FCM 토큰")
								),
								responseFields(
										fieldWithPath("code").type(JsonFieldType.NUMBER)
												.description("응답 코드"),
										fieldWithPath("message").type(JsonFieldType.STRING)
												.description("응답 메시지")
								)
						));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			void token이_null인_경우() throws Exception {

				// given
				String requestBody = """
						{
							"token": null
						}
						""";

				MemberFixture.setAuthentication(1L, "test@example.com", "KAKAO",
						MemberRole.ROLE_MEMBER);

				// when & then
				mockMvc.perform(post("/api/fcm/save")
								.contentType("application/json")
								.content(requestBody))
						.andExpect(status().isBadRequest())
						.andDo(document("fcm-token-save-invalid-null",
								preprocessRequest(prettyPrint()),
								preprocessResponse(prettyPrint()),
								requestFields(
										fieldWithPath("token").description("FCM 토큰 (null 불가)")
								),
								responseFields(
										fieldWithPath("code").description("에러 코드"),
										fieldWithPath("message").description("에러 메시지")
								)
						));
			}

			@Test
			void token이_빈문자열인_경우() throws Exception {

				// given
				String requestBody = """
						{
							"token": ""
						}
						""";

				MemberFixture.setAuthentication(1L, "test@example.com", "KAKAO",
						MemberRole.ROLE_MEMBER);

				// when & then
				mockMvc.perform(post("/api/fcm/save")
								.contentType("application/json")
								.content(requestBody))
						.andExpect(status().isBadRequest())
						.andDo(document("fcm-token-save-invalid-blank",
								preprocessRequest(prettyPrint()),
								preprocessResponse(prettyPrint()),
								requestFields(
										fieldWithPath("token").description("FCM 토큰 (빈 문자열 불가)")
								),
								responseFields(
										fieldWithPath("code").description("에러 코드"),
										fieldWithPath("message").description("에러 메시지")
								)
						));
			}

		}
	}
}
