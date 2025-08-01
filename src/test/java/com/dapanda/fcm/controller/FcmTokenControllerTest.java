package com.dapanda.fcm.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dapanda.TestConfig;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.fcmToken.dto.SaveFcmTokenRequest;
import com.dapanda.fcmToken.repository.NotificationRepository;
import com.dapanda.fcmToken.service.FcmTokenService;
import com.dapanda.fcm_token.entity.NotificationEntity;
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

	@Autowired
	private NotificationRepository notificationRepository;

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

	@Nested
	@DisplayName("알림 조회 API")
	class GetNotifications {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("회원의 알림 목록을 최신순으로 성공적으로 조회한다.")
			void getMyNotifications_success() throws Exception {
				// given
				Member member = MemberFixture.createMember1();
				Member savedMember = memberRepository.save(member);

				NotificationEntity notification1 = NotificationEntity.of("제목1", "본문1", savedMember);
				NotificationEntity notification2 = NotificationEntity.of("제목2", "본문2", savedMember);
				notificationRepository.save(notification1);
				notificationRepository.save(notification2);

				CustomUserDetails userDetails = CustomUserDetails.from(savedMember);

				// when & then
				mockMvc.perform(get("/api/notifications")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andDo(document("get-my-notifications",
								preprocessRequest(prettyPrint()),
								preprocessResponse(prettyPrint()),
								responseFields(
										fieldWithPath("code").type(JsonFieldType.NUMBER)
												.description("응답 코드"),
										fieldWithPath("message").type(JsonFieldType.STRING)
												.description("응답 메시지"),
										fieldWithPath("data").type(JsonFieldType.ARRAY)
												.description("알림 목록"),
										fieldWithPath("data[].id").type(JsonFieldType.NUMBER)
												.description("알림 ID"),
										fieldWithPath("data[].title").type(JsonFieldType.STRING)
												.description("알림 제목"),
										fieldWithPath("data[].body").type(JsonFieldType.STRING)
												.description("알림 내용"),
										fieldWithPath("data[].createdAt").type(JsonFieldType.STRING)
												.description("알림 생성 시간")
								)
						));
			}
		}
	}

	@Nested
	@DisplayName("알림 삭제 API")
	class DeleteNotification {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("본인의 알림을 성공적으로 삭제한다.")
			void deleteNotification_success() throws Exception {
				// given
				Member member = MemberFixture.createMember1();
				Member savedMember = memberRepository.save(member);

				NotificationEntity notification = NotificationEntity.of("제목", "본문", savedMember);
				notificationRepository.save(notification);

				CustomUserDetails userDetails = CustomUserDetails.from(savedMember);

				// when & then
				mockMvc.perform(delete("/api/notifications/{notificationId}", notification.getId())
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andDo(document("delete-notification",
								preprocessRequest(prettyPrint()),
								preprocessResponse(prettyPrint()),
								pathParameters(
										parameterWithName("notificationId").description("삭제할 알림 ID")
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
			@DisplayName("본인의 알림이 아닌 경우 403 Forbidden 응답이 반환된다.")
			void deleteNotification_forbidden() throws Exception {
				// given
				Member member1 = MemberFixture.createMember1();
				Member member2 = MemberFixture.createMember2();
				memberRepository.save(member1);
				Member savedMember2 = memberRepository.save(member2);

				NotificationEntity notification = NotificationEntity.of("제목", "본문", savedMember2);
				notificationRepository.save(notification);

				CustomUserDetails userDetails = CustomUserDetails.from(member1);

				// when & then
				mockMvc.perform(delete("/api/notifications/{notificationId}", notification.getId())
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isForbidden())
						.andDo(document("delete-notification-forbidden",
								preprocessRequest(prettyPrint()),
								preprocessResponse(prettyPrint()),
								pathParameters(
										parameterWithName("notificationId").description(
												"삭제하려는 알림 ID")
								),
								responseFields(
										fieldWithPath("code").description("에러 코드"),
										fieldWithPath("message").description("에러 메시지")
								)
						));
			}

			@Test
			@DisplayName("존재하지 않는 알림 ID인 경우 400 Bad Request 응답이 반환된다.")
			void deleteNotification_notFound() throws Exception {
				// given
				Member member = MemberFixture.createMember1();
				memberRepository.save(member);
				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(delete("/api/notifications/{notificationId}", 9999L)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest())
						.andDo(document("delete-notification-not-found",
								preprocessRequest(prettyPrint()),
								preprocessResponse(prettyPrint()),
								pathParameters(
										parameterWithName("notificationId").description(
												"존재하지 않는 알림 ID")
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
