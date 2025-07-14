package com.dapanda.review.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.review.dto.request.DeleteReviewRequest;
import com.dapanda.review.dto.request.SaveReviewRequest;
import com.dapanda.review.dto.request.UpdateReviewRequest;
import com.dapanda.review.dto.response.SaveReviewResponse;
import com.dapanda.review.dto.response.UpdateReviewResponse;
import com.dapanda.review.entity.Review;
import com.dapanda.review.entity.ReviewFixture;
import com.dapanda.review.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
			@DisplayName("셀프 리뷰를 할 경우 예외가 발생한다")
			public void selfReviewTest() {

				//given
				Long memberId = 2L;
				Long revieweeId = 2L;

				Long productId = 3L;
				float rating = 3.5f;
				String comment = "그저 그래요";

				SaveReviewRequest request = new SaveReviewRequest(revieweeId, productId, rating, comment);

				//when & then
				assertThatThrownBy(() -> reviewService.saveReview(request, memberId))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.SELF_REVIEW.getMessage());

				verify(reviewRepository, never()).save(any());
			}

			@Test
			@DisplayName("리뷰 대상 회원 아이디가 존재하지 않을 경우 예외가 발생한다")
			public void memberNotFoundTest() {

				//given
				Long revieweeId = 3L;
				Long memberId = 2L;

				Long productId = 3L;
				float rating = 3.5f;
				String comment = "그저 그래요";

				SaveReviewRequest request = new SaveReviewRequest(revieweeId, productId, rating, comment);

				given(memberRepository.existsById(revieweeId)).willReturn(false);

				//when & then
				assertThatThrownBy(() -> reviewService.saveReview(request, memberId))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.MEMBER_NOT_FOUND.getMessage());

				verify(reviewRepository, never()).save(any());
			}
		}
	}

	@Nested
	@DisplayName("리뷰 삭제")
	class DeleteReview {

		@DisplayName("성공 케이스")
		@Nested
		class Success {

			@Test
			@DisplayName("리뷰 아이디와 회원 아이디가 유효하면 리뷰가 삭제된다")
			public void deleteReviewTest() {

				//given
				Long reviewId = 10L;
				Long memberId = 1L;
				Long revieweeId = 2L;

				DeleteReviewRequest request = new DeleteReviewRequest(reviewId);

				Review review = ReviewFixture.createReview1(reviewId, memberId, revieweeId);

				given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

				//when
				reviewService.deleteReview(request, memberId);

				//then
				verify(reviewRepository).delete(review);
			}
		}

		@DisplayName("실패 케이스")
		@Nested
		class Fail {

			@Test
			@DisplayName("존재하지 않는 리뷰면 예외가 발생한다")
			public void reviewNotFoundTest() {

				//given
				Long memberId = 1L;
				Long reviewId = 10L;

				DeleteReviewRequest request = new DeleteReviewRequest(reviewId);

				given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

				//when & then
				assertThatThrownBy(() -> reviewService.deleteReview(request, memberId))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.REVIEW_NOT_FOUND.getMessage());
			}

			@Test
			@DisplayName("리뷰 작성자가 아닌 경우 예외가 발생한다")
			public void reviewOwnerTest() {

				//given
				Long reviewId = 10L;
				Long otherReviewerId = 10000L;
				Long myReviewerId = 1L;
				Long revieweeId = 2L;

				DeleteReviewRequest request = new DeleteReviewRequest(reviewId);

				Review review = ReviewFixture.createReview1(reviewId, otherReviewerId, revieweeId);

				given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

				//when & then
				assertThatThrownBy(() -> reviewService.deleteReview(request, myReviewerId))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.OTHER_REVIEW.getMessage());
			}
		}
	}

	@Nested
	@DisplayName("리뷰 수정")
	class UpdateReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("리뷰 수정이 성공하면 리뷰 아이디를 반환한다")
			public void updateReviewTest() {

				//given
				Long savedReviewId = 1L;
				Float newRating = 4.5f;
				String newComment = "진짜 좋아용";

				Long memberId = 5L;
				Long reviewerId = 5L;
				Long revieweeId = 6L;

				UpdateReviewRequest request = new UpdateReviewRequest(savedReviewId, newRating, newComment);
				Review savedReview = ReviewFixture.createReview1(savedReviewId, reviewerId, revieweeId);

				Float originalRating = savedReview.getRating();
				String originalComment = savedReview.getComment();

				given(reviewRepository.findById(savedReviewId)).willReturn(Optional.of(savedReview));

				//when
				UpdateReviewResponse response = reviewService.updateReview(request, memberId);

				//then
				assertThat(response.getReviewId()).isEqualTo(savedReview.getId());

				assertThat(savedReview.getRating()).isEqualTo(newRating);
				assertThat(savedReview.getComment()).isEqualTo(newComment);

				assertThat(savedReview.getRating()).isNotEqualTo(originalRating);
				assertThat(savedReview.getComment()).isNotEqualTo(originalComment);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("리뷰가 존재하지 않으면 예외가 발생한다")
			public void reviewNotFoundTest() {

				//given
				Long nonExistentReviewId = 999L;
				Float newRating = 4.5f;
				String newComment = "진짜 좋아용";

				UpdateReviewRequest request = new UpdateReviewRequest(nonExistentReviewId, newRating, newComment);

				Long memberId = 5L;

				given(reviewRepository.findById(nonExistentReviewId)).willReturn(Optional.empty());

				//when & then
				assertThatThrownBy(() -> reviewService.updateReview(request, memberId))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.REVIEW_NOT_FOUND.getMessage());

				verify(reviewRepository).findById(nonExistentReviewId);
			}

			@Test
			@DisplayName("리뷰 오너가 아니면 예외가 발생한다")
			public void reviewOwnerTest() {

				//given
				Long reviewId = 1L;
				Float newRating = 4.5f;
				String newComment = "진짜 좋아용";

				Long reviewerId = 2L;
				Long revieweeId = 3L;

				Long memberId = 4L;

				Review savedReview = ReviewFixture.createReview1(reviewId, reviewerId, revieweeId);

				UpdateReviewRequest request = new UpdateReviewRequest(reviewId, newRating, newComment);

				given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));

				//when & then
				assertThatThrownBy(() -> reviewService.updateReview(request, memberId))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.OTHER_REVIEW.getMessage());
			}
		}
	}
}
