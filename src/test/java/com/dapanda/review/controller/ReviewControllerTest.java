package com.dapanda.review.controller;

import com.dapanda.RestDocsConfig;
import com.dapanda.common.config.SecurityConfig;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.entity.ItemType;
import com.dapanda.review.dto.request.SaveReviewRequest;
import com.dapanda.review.entity.Review;
import com.dapanda.review.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({RestDocsConfig.class})
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("리뷰 컨트롤러 테스트")
class ReviewControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ReviewRepository reviewRepository;

	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@BeforeEach
	void restDocsSetUp(RestDocumentationContextProvider restDocumentation) {

		this.mockMvc = RestDocsConfig.createMockMvc(context, restDocumentation);
	}

	@Nested
	@DisplayName("리뷰 등록 API")
	class SaveReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("등록된 리뷰 아이디를 반환한다")
			public void saveReviewTest() throws Exception {

				//given
				Float rating = 1.5f;
				String comment = "진짜 별로에요";
				Long productId = 123L;
				ItemType type = ItemType.MOBILE_DATA;

				Member reviewer = MemberFixture.MEMBER_REVIEWER;
				Member reviewee = MemberFixture.MEMBER_REVIEWEE;

				Member savedReviewer = memberRepository.save(reviewer);
				Member savedReviewee = memberRepository.save(reviewee);

				SaveReviewRequest request = new SaveReviewRequest(savedReviewee.getId(), productId, rating, comment);

				//when & then
				mockMvc.perform(post("/api/reviews")
								.param("memberId", savedReviewer.getId().toString())
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.reviewId").exists())
						.andDo(document("save-review",
								requestFields(
										fieldWithPath("revieweeId").description("리뷰 대상 회원의 아이디 (필수)"),
										fieldWithPath("productId").description("리뷰 대상 상품의 아이디 (필수)"),
										fieldWithPath("rating").description("리뷰 평점 (필수, 1.0 ~ 5.0)"),
										fieldWithPath("comment").description("리뷰 코멘트 (필수, 최대 50자)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("정상 처리 되었습니다."),
										fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
										fieldWithPath("data.reviewId").description("생성된 리뷰 아이디")
								))
						);

				Optional<Review> saveReview = reviewRepository.findAll().stream().findFirst();

				assertThat(saveReview).isPresent();
				assertThat(saveReview.get().getRating()).isEqualTo(rating);
				assertThat(saveReview.get().getComment()).isEqualTo(comment);
				assertThat(saveReview.get().getReviewer().getId()).isEqualTo(savedReviewer.getId());
				assertThat(saveReview.get().getReviewee().getId()).isEqualTo(savedReviewee.getId());
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("필수 필드가 누락되면 BadRequest를 반환한다")
			public void validateRequiredFields() throws Exception {

				// given
				SaveReviewRequest request = new SaveReviewRequest(null, null, null,  null);

				// when & then
				mockMvc.perform(post("/api/reviews")
								.param("memberId", "1")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("save-review-validation-error"));
			}

			@Test
			@DisplayName("리뷰 대상 회원이 존재하지 않으면 에러를 반환한다")
			public void validateMemberId() throws Exception {

				// given
				Member reviewer = MemberFixture.MEMBER_REVIEWER;

				Member savedReviewer = memberRepository.save(reviewer);

				SaveReviewRequest request = new SaveReviewRequest(505L, 123L,5.0f, "test");

				// when & then
				mockMvc.perform(post("/api/reviews")
								.param("memberId", savedReviewer.getId().toString())
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("save-review-member-id-error"));
			}
		}
	}
}
