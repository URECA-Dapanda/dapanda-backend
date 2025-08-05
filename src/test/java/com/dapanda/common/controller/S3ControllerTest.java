package com.dapanda.common.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.base.BaseIntegrationTest;
import com.dapanda.common.dto.request.PreSignRequest;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("S3 Controller 테스트")
class S3ControllerTest extends BaseIntegrationTest {

	@Autowired
	private AmazonS3 amazonS3;

	@Autowired
	private MemberRepository memberRepository;

	@Nested
	@DisplayName("S3 PreSignedUrl 발급 API")
	class GetPreSignedUrl {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("성공적으로 presignedUrl을 발급받는다")
			void getPreSignedUrlSuccess() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());
				CustomUserDetails userDetails = CustomUserDetails.from(member);

				String filename = "sample.jpg";
//				String bucket = "dpd-bucket";
				String presignedUrl = "https://example.com/presigned-url";
				String key = "images/1/uuid-sample.jpg";
//				String publicUrl = "https://dpd-bucket.s3.ap-northeast-2.amazonaws.com/" + key;

				// S3 Mocking
				Mockito.when(
								amazonS3.generatePresignedUrl(
										Mockito.any(GeneratePresignedUrlRequest.class)))
						.thenReturn(new URL(presignedUrl));
				// body 생성
//				PreSignRequest requestDto = new PreSignRequest(List.of("sample.jpg"));

				mockMvc.perform(post("/api/images/presign")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(
										new PreSignRequest(List.of(filename)))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").exists())
						.andExpect(jsonPath("$.message").exists())
						.andExpect(jsonPath("$.data").isArray())
						.andExpect(jsonPath("$.data[0].filename").value(filename))
						.andExpect(jsonPath("$.data[0].url").value(presignedUrl))
						.andExpect(jsonPath("$.data[0].publicUrl").exists())
						.andExpect(jsonPath("$.data[0].key").exists())
						.andDo(document("s3/post-presigned-url",
								requestFields(
										fieldWithPath("filenames[]").description("업로드할 파일 이름 리스트")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data[].filename").description("요청한 파일 이름"),
										fieldWithPath("data[].url").description(
												"Presigned PUT URL (S3 업로드용)"),
										fieldWithPath("data[].publicUrl").description("S3 공개 URL"),
										fieldWithPath("data[].key").description(
												"실제 S3에 업로드되는 객체의 Key")
								)
						));
			}

			@Test
			@DisplayName("여러 개 presignedUrl을 한 번에 발급받는다")
			void getPreSignedUrlsSuccess() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				var filenames = List.of("a.jpg", "b.png", "c.jpeg");
				PreSignRequest requestDto = new PreSignRequest(filenames);
				var bucket = "dpd-bucket";
				var basePresignedUrl = "https://example.com/presigned-url";
				var userId = userDetails.getId();

				// S3 Mocking: 여러 번 호출될 때마다 presignedUrl을 반환
				Mockito.when(amazonS3.generatePresignedUrl(
								Mockito.any(GeneratePresignedUrlRequest.class)))
						.thenReturn(
								new URL(basePresignedUrl + "?1"),
								new URL(basePresignedUrl + "?2"),
								new URL(basePresignedUrl + "?3")
						);

				var requestBody = Map.of("filenames", filenames);

				mockMvc.perform(post("/api/images/presign")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(requestDto)))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").exists())
						.andExpect(jsonPath("$.message").exists())
						.andExpect(jsonPath("$.data").isArray())
						.andExpect(jsonPath("$.data.length()").value(3))
						.andExpect(jsonPath("$.data[0].filename").value("a.jpg"))
						.andExpect(jsonPath("$.data[0].url").value(basePresignedUrl + "?1"))
						.andExpect(jsonPath("$.data[1].filename").value("b.png"))
						.andExpect(jsonPath("$.data[1].url").value(basePresignedUrl + "?2"))
						.andExpect(jsonPath("$.data[2].filename").value("c.jpeg"))
						.andExpect(jsonPath("$.data[2].url").value(basePresignedUrl + "?3"))
						.andDo(document("s3/post-presigned-url-bulk",
								requestFields(
										fieldWithPath("filenames[]").description(
												"여러 개 presign을 요청할 파일 이름 리스트")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data[].filename").description(
												"요청한 파일 이름"),
										fieldWithPath("data[].url").description(
												"Presigned PUT URL (S3 업로드용)"),
										fieldWithPath("data[].publicUrl").description(
												"S3 공개 URL"),
										fieldWithPath("data[].key").description(
												"실제 S3에 업로드되는 객체의 Key")
								)
						));


			}

		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("필수 입력값이 누락되면 BadRequest를 반환한다")
			void getPreSignedUrlFailMissingParam() throws Exception {

				Member member = memberRepository.save(MemberFixture.createMember1());

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// filename 없는 케이스
				mockMvc.perform(post("/api/images/presign")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.contentType(MediaType.APPLICATION_JSON)
								.content("{}"))
						.andExpect(status().isBadRequest())
						.andDo(document("s3/post-presigned-url/validation-error"));
			}
		}

	}
}
