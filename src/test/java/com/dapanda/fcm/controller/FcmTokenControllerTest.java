package com.dapanda.fcm.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.base.BaseIntegrationTest;
import com.dapanda.fcmToken.dto.request.SaveFcmTokenRequest;
import com.dapanda.fcmToken.entity.NotificationEntity;
import com.dapanda.fcmToken.repository.NotificationRepository;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("FCM 토큰 컨트롤러 테스트")
class FcmTokenControllerTest extends BaseIntegrationTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Nested
	@DisplayName("FCM 토큰 저장 API")
	class SaveFcmToken {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("FCM 토큰을 받아서 서버에 성공적으로 저장한다.")
			void saveFcmTokenSuccess() throws Exception {

				//given
				SaveFcmTokenRequest request = new SaveFcmTokenRequest("valid_token");

				Member member = memberRepository.save(MemberFixture.createMember1());

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				//when & then
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
			@DisplayName("토큰이 null 일 경우 예외가 발생한다")
			void nullTokenTest() throws Exception {

				// given
				SaveFcmTokenRequest request = new SaveFcmTokenRequest(null);

				Member member = memberRepository.save(MemberFixture.createMember1());

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(post("/api/fcm/save")
								.contentType("application/json")
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
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
			@DisplayName("토큰이 비었을 경우 에외가 발생한다")
			void emptyTokenTest() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				SaveFcmTokenRequest request = new SaveFcmTokenRequest(null);

				// when & then
				mockMvc.perform(post("/api/fcm/save")
								.contentType("application/json")
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
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
			void getMyNotificationsSuccess() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());

				notificationRepository.save(NotificationEntity.of("제목1", "본문1", member));
				notificationRepository.save(NotificationEntity.of("제목2", "본문2", member));

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(get("/api/notifications")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andDo(document("get-my-notifications",
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
				Member member = memberRepository.save(MemberFixture.createMember1());

				NotificationEntity notificationEntity = notificationRepository.save(NotificationEntity.of("제목", "본문", member));

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(delete("/api/notifications/{notificationId}", notificationEntity.getId())
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

				NotificationEntity notificationEntity = notificationRepository.save(
						NotificationEntity.of("제목", "본문", savedMember2));

				CustomUserDetails userDetails = CustomUserDetails.from(member1);

				// when & then
				mockMvc.perform(delete("/api/notifications/{notificationId}", notificationEntity.getId())
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
				Member member = memberRepository.save(MemberFixture.createMember1());

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
