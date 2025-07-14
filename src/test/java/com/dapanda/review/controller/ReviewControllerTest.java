package com.dapanda.review.controller;

import com.dapanda.TestConfig;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.review.dto.request.DeleteReviewRequest;
import com.dapanda.review.dto.request.SaveReviewRequest;
import com.dapanda.review.dto.request.UpdateReviewRequest;
import com.dapanda.review.entity.Review;
import com.dapanda.review.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestConfig.class)
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

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EntityManager entityManager;

	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@BeforeEach
	void restDocsSetUp(RestDocumentationContextProvider restDocumentation) {

		this.mockMvc = TestConfig.createMockMvc(context, restDocumentation);

		cleanupDatabase();
	}

	private void cleanupDatabase(){

		entityManager.clear();

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

		jdbcTemplate.execute("TRUNCATE TABLE review");
		jdbcTemplate.execute("TRUNCATE TABLE member");

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
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

				Member reviewer = MemberFixture.MEMBER_REVIEWER;
				Member reviewee = MemberFixture.MEMBER_REVIEWEE;

				Member savedReviewer = memberRepository.save(reviewer);
				Member savedReviewee = memberRepository.save(reviewee);

				SaveReviewRequest request = new SaveReviewRequest(savedReviewee.getId(), productId, rating, comment);

				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(savedReviewer.getId());

				//when & then
				mockMvc.perform(post("/api/reviews")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails,null,Collections.emptyList()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.reviewId").exists())
						.andDo(document("review/save-review",
								requestFields(
										fieldWithPath("revieweeId").description("리뷰 대상 회원의 아이디 (필수)"),
										fieldWithPath("productId").description("리뷰 대상 상품의 아이디 (필수)"),
										fieldWithPath("rating").description("리뷰 평점 (필수, 1.0 ~ 5.0)"),
										fieldWithPath("comment").description("리뷰 코멘트 (필수, 최대 50자)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
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
			@DisplayName("필수 필드가 누락되면 BadRequest 를 반환한다")
			public void validateRequiredFields() throws Exception {

				// given
				SaveReviewRequest request = new SaveReviewRequest(null, null, null,  null);

				// when & then
				mockMvc.perform(post("/api/reviews")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("review/save-review-validation-error"));
			}
		}
	}


	@Nested
	@DisplayName("리뷰 삭제 API")
	class DeleteReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("삭제가 성공하면 200응답을 반환한다")
			public void deleteReviewTest() throws Exception {

				//given
				Float rating = 1.5f;
				String comment = "진짜 별로에요";
				Long productId = 123L;

				Member reviewer = MemberFixture.MEMBER_REVIEWER;
				Member reviewee = MemberFixture.MEMBER_REVIEWEE;

				Member savedReviewer = memberRepository.save(reviewer);
				Member savedReviewee = memberRepository.save(reviewee);

				Review review = Review.of(rating, comment, productId, savedReviewer, savedReviewee);

				Review savedReview = reviewRepository.save(review);

				DeleteReviewRequest request = new DeleteReviewRequest(savedReview.getId());

				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(savedReviewer.getId());

				//when & then
				mockMvc.perform(delete("/api/reviews")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andDo(document("review/delete-review",
								requestFields(
										fieldWithPath("reviewId").description("삭제할 리뷰 아이디 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								))
						);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("필수 필드가 누락되면 BadRequest 를 반환한다")
			public void validateRequiredFields() throws Exception {

				// given
				DeleteReviewRequest request = new DeleteReviewRequest(null);

				Member reviewer = MemberFixture.createMember();

				Member savedReviewer = memberRepository.save(reviewer);

				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(savedReviewer.getId());

				// when & then
				mockMvc.perform(delete("/api/reviews")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("review/delete-review-validation-error"));
			}
		}
	}

	@Nested
	@DisplayName("리뷰 수정 API")
	class UpdateReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("리뷰 수정이 완료되면 리뷰 아이디를 반환한다")
			public void updateReviewTest() throws Exception {

				//given
				Member reviewer = MemberFixture.MEMBER_REVIEWER;
				Member reviewee = MemberFixture.MEMBER_REVIEWEE;

				Member savedReviewer = memberRepository.save(reviewer);
				Member savedReviewee = memberRepository.save(reviewee);

				Float newRating = 1.0f;
				String newComment = "별로에요";

				Float originalRating = 5.0f;
				String originalComment = "너무 좋았어요";
				Long productId = 15L;

				Review review = Review.of(originalRating, originalComment, productId, savedReviewer, savedReviewee);
				Review savedReview = reviewRepository.save(review);

				UpdateReviewRequest request = new UpdateReviewRequest(savedReview.getId(), newRating, newComment);

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(savedReviewer.getId());

				//when & then
				mockMvc.perform(put("/api/reviews")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.reviewId").value(savedReview.getId()))
						.andDo(document("review/update-review",
								requestFields(
										fieldWithPath("reviewId").description("수정할 리뷰 아이디 (필수)"),
										fieldWithPath("rating").description("수정할 평점 (필수)"),
										fieldWithPath("comment").description("수정할 코멘트 (필수)")

								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data.reviewId").description("수정된 리뷰 아이디")
								))
						);

				Review updatedReview = reviewRepository.findById(savedReview.getId()).orElseThrow();
				assertThat(updatedReview.getId()).isEqualTo(savedReview.getId());
				assertThat(updatedReview.getRating()).isEqualTo(request.rating());
				assertThat(updatedReview.getComment()).isEqualTo(request.comment());
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("필수 필드가 누락되면 BadRequest 를 반환한다")
			public void validateRequiredFields() throws Exception {

				//given
				UpdateReviewRequest request = new UpdateReviewRequest(null, null, null);

				//when & then
				mockMvc.perform(put("/api/reviews")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("review/update-review-validation-error"));
			}
		}
	}
}
