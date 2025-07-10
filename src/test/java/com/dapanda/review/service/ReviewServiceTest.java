package com.dapanda.review.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.review.dto.request.SaveReviewRequest;
import com.dapanda.review.dto.response.SaveReviewResponse;
import com.dapanda.review.entity.Review;
import com.dapanda.review.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("리뷰 서비스 테스트")
class ReviewServiceTest {

	@Mock
	MemberRepository memberRepository;

	@Mock
	ReviewRepository reviewRepository;

	@InjectMocks
	ReviewService reviewService;

	@Nested
	@DisplayName("리뷰 등록")
	class SaveReview {

		@DisplayName("성공 케이스")
		@Nested
		class Success {

			@Test
			@DisplayName("리뷰 등록 성공후 등록된 리뷰 아이디를 반환한다")
			public void saveReviewTest() {

				//given
				Long reviewerId = 1L;
				Long revieweeId = 2L;
				Long productId = 3L;
				Long expectedReviewId = 101L;
				float rating = 3.5f;
				String comment = "그저 그래요";

				Member reviewer = MemberFixture.MEMBER_REVIEWER;
				Member reviewee = MemberFixture.MEMBER_REVIEWEE;

				Review savedReview = Review.of(rating, comment, productId, reviewer, reviewee);

				ReflectionTestUtils.setField(savedReview, "id", expectedReviewId);

				given(memberRepository.existsById(revieweeId)).willReturn(true);
				given(memberRepository.getReferenceById(reviewerId)).willReturn(reviewer);
				given(memberRepository.getReferenceById(revieweeId)).willReturn(reviewee);
				given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

				SaveReviewRequest request = new SaveReviewRequest(revieweeId, productId, rating, comment);

				//when
				SaveReviewResponse response = reviewService.saveReview(request, reviewerId);

				//then
				assertThat(response.getReviewId()).isEqualTo(expectedReviewId);
			}
		}

		@DisplayName("실패 케이스")
		@Nested
		class Fail {

			@Test
			@DisplayName("자기 자신을 리뷰할 수 없습니다.")
			public void selfReviewTest() {

				//given
				Long reviewerId = 2L;
				Long revieweeId = 2L;
				Long productId = 3L;
				float rating = 3.5f;
				String comment = "그저 그래요";

				SaveReviewRequest request = new SaveReviewRequest(revieweeId, productId, rating, comment);

				//when & then
				assertThatThrownBy(() -> reviewService.saveReview(request, reviewerId))
						.isInstanceOf(GlobalException.class)
						.hasMessage("자신에게 리뷰를 작성할 수 없습니다.");

				verify(memberRepository, never()).getReferenceById(any());
				verify(reviewRepository, never()).save(any());
			}
		}
	}
}
