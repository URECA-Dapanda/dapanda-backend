package com.dapanda.review.service;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.review.dto.request.*;
import com.dapanda.review.dto.response.*;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

	private static final Long MEMBER_ID = 1L;
	private static final Long REVIEWER_ID = 1L;
	private static final Long REVIEWEE_ID = 2L;
	private static final Long OTHER_REVIEWER_ID = 3L;
	private static final Long NON_EXISTENT_REVIEW_ID = 999L;
	private static final Long REVIEW_ID = 1L;
	private static final Float TEST_RATING = 3.5F;
	private static final String TEST_COMMENT = "적당해요";
	private static final Long TEST_PRODUCT_ID = 123L;
	private static final Float NEW_RATING = 1.0F;
	private static final String NEW_COMMENT = "별로에요";

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
				Member savedReviewer = MemberFixture.createMember1WithId(REVIEWER_ID);
				Member savedReviewee = MemberFixture.createMember2WithId(REVIEWEE_ID);
				Review savedReview = ReviewFixture.createReviewWithId(TEST_RATING, TEST_COMMENT, TEST_PRODUCT_ID, savedReviewer, savedReviewee, REVIEW_ID);


				given(memberRepository.existsById(REVIEWEE_ID)).willReturn(true);
				given(memberRepository.getReferenceById(REVIEWER_ID)).willReturn(savedReviewer);
				given(memberRepository.getReferenceById(REVIEWEE_ID)).willReturn(savedReviewee);
				given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

				SaveReviewRequest request = new SaveReviewRequest(REVIEWEE_ID, TEST_PRODUCT_ID, TEST_RATING, TEST_COMMENT);

				//when
				SaveReviewResponse response = reviewService.saveReview(request, REVIEWER_ID);

				//then
				assertThat(response.getReviewId()).isEqualTo(REVIEW_ID);
			}
		}

		@DisplayName("실패 케이스")
		@Nested
		class Fail {

			@Test
			@DisplayName("셀프 리뷰를 할 경우 예외가 발생한다")
			public void selfReviewTest() {

				//given
				SaveReviewRequest request = new SaveReviewRequest(MEMBER_ID, TEST_PRODUCT_ID, TEST_RATING, TEST_COMMENT);

				//when & then
				assertThatThrownBy(() -> reviewService.saveReview(request, MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.SELF_REVIEW.getMessage());

				verify(reviewRepository, never()).save(any());
			}

			@Test
			@DisplayName("리뷰 대상 회원 아이디가 존재하지 않을 경우 예외가 발생한다")
			public void memberNotFoundTest() {

				//given
				SaveReviewRequest request = new SaveReviewRequest(REVIEWEE_ID, TEST_PRODUCT_ID, TEST_RATING, TEST_COMMENT);

				given(memberRepository.existsById(REVIEWEE_ID)).willReturn(false);

				//when & then
				assertThatThrownBy(() -> reviewService.saveReview(request, MEMBER_ID))
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
				DeleteReviewRequest request = new DeleteReviewRequest(REVIEW_ID);

				Member savedReviewer = MemberFixture.createMember1WithId(REVIEWER_ID);
				Member savedReviewee = MemberFixture.createMember2WithId(REVIEWEE_ID);

				Review review = ReviewFixture.createReviewWithId(TEST_RATING, TEST_COMMENT, TEST_PRODUCT_ID, savedReviewer, savedReviewee, REVIEWEE_ID);

				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(review));

				//when
				reviewService.deleteReview(request, REVIEWER_ID);

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
				DeleteReviewRequest request = new DeleteReviewRequest(REVIEW_ID);

				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.empty());

				//when & then
				assertThatThrownBy(() -> reviewService.deleteReview(request, MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.REVIEW_NOT_FOUND.getMessage());
			}

			@Test
			@DisplayName("리뷰 작성자가 아닌 경우 예외가 발생한다")
			public void reviewOwnerTest() {

				//given
				DeleteReviewRequest request = new DeleteReviewRequest(REVIEW_ID);

				Member savedReviewer = MemberFixture.createMember1WithId(OTHER_REVIEWER_ID);
				Member savedReviewee = MemberFixture.createMember2WithId(REVIEWEE_ID);

				Review review = ReviewFixture.createReviewWithId(TEST_RATING, TEST_COMMENT, TEST_PRODUCT_ID, savedReviewer, savedReviewee, REVIEW_ID);

				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(review));

				//when & then
				assertThatThrownBy(() -> reviewService.deleteReview(request, MEMBER_ID))
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
				UpdateReviewRequest request = new UpdateReviewRequest(REVIEW_ID, NEW_RATING, NEW_COMMENT);

				Member savedReviewer = MemberFixture.createMember1WithId(REVIEWER_ID);
				Member savedReviewee = MemberFixture.createMember2WithId(REVIEWEE_ID);

				Review savedReview = ReviewFixture.createReviewWithId(TEST_RATING, TEST_COMMENT, TEST_PRODUCT_ID, savedReviewer, savedReviewee, REVIEWEE_ID);

				Float originalRating = savedReview.getRating();
				String originalComment = savedReview.getComment();

				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(savedReview));

				//when
				UpdateReviewResponse response = reviewService.updateReview(request, MEMBER_ID);

				//then
				assertThat(response.getReviewId()).isEqualTo(savedReview.getId());

				assertThat(savedReview.getRating()).isEqualTo(NEW_RATING);
				assertThat(savedReview.getComment()).isEqualTo(NEW_COMMENT);

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
				UpdateReviewRequest request = new UpdateReviewRequest(NON_EXISTENT_REVIEW_ID, NEW_RATING, NEW_COMMENT);

				given(reviewRepository.findById(NON_EXISTENT_REVIEW_ID)).willReturn(Optional.empty());

				//when & then
				assertThatThrownBy(() -> reviewService.updateReview(request, MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.REVIEW_NOT_FOUND.getMessage());

				verify(reviewRepository).findById(NON_EXISTENT_REVIEW_ID);
			}

			@Test
			@DisplayName("리뷰 작성자가 아니면 예외가 발생한다")
			public void reviewOwnerTest() {

				//given
				Member savedReviewer = MemberFixture.createMember1WithId(OTHER_REVIEWER_ID);
				Member savedReviewee = MemberFixture.createMember2WithId(REVIEWEE_ID);

				Review savedReview = ReviewFixture.createReviewWithId(TEST_RATING, TEST_COMMENT, TEST_PRODUCT_ID, savedReviewer, savedReviewee, REVIEW_ID);

				UpdateReviewRequest request = new UpdateReviewRequest(REVIEW_ID, NEW_RATING, NEW_COMMENT);

				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(savedReview));

				//when & then
				assertThatThrownBy(() -> reviewService.updateReview(request, MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.OTHER_REVIEW.getMessage());
			}
		}
	}

	@Nested
	@DisplayName("판매자 리뷰 조회")
	class ReadSellerReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("페이징조건 없이 보내면 기본 값으로 조회된다")
			public void readDefaultPagingConditionTest() {

				//given
				ReadSellerReviewRequest request = ReviewFixture.createDefaultSellerReviewRequest();
				List<ReadSellerReviewResponse> response = ReviewFixture.create3SellerReviewResponses();

				given(reviewRepository.findSellerReviewWithCursor(request)).willReturn(response);

				//when
				CursorPageResponse<ReadSellerReviewResponse> pageResponse = reviewService.readSellerReview(request);

				//then
				assertThat(pageResponse.getPageInfo().getSize()).isEqualTo(request.size());
				assertThat(pageResponse.getPageInfo().isHasNext()).isTrue();
				assertThat(pageResponse.getPageInfo().getNextCursorId()).isEqualTo(response.get(request.size() - 1).getReviewId());

				assertThat(pageResponse.getData()).hasSize(request.size());
				assertThat(pageResponse.getData().get(0).getReviewId()).isEqualTo(response.get(0).getReviewId());
				assertThat(pageResponse.getData().get(1).getReviewId()).isEqualTo(response.get(1).getReviewId());
			}

			@Test
			@DisplayName("마지막 페이지면 hasNext 가 false 이다")
			public void lastPageHasNextFalseTest() {

				//given
				ReadSellerReviewRequest request = ReviewFixture.createFinalPageSellerReviewRequest();
				List<ReadSellerReviewResponse> response = ReviewFixture.create4SellerReviewResponses();

				given(reviewRepository.findSellerReviewWithCursor(request)).willReturn(response);

				//when
				CursorPageResponse<ReadSellerReviewResponse> pageResponse = reviewService.readSellerReview(request);

				//then
				assertThat(pageResponse.getPageInfo().getSize()).isEqualTo(request.size());
				assertThat(pageResponse.getPageInfo().isHasNext()).isFalse();
				assertThat(pageResponse.getPageInfo().getNextCursorId()).isNull();

				assertThat(pageResponse.getData()).hasSize(request.size());
				assertThat(pageResponse.getData().get(0).getReviewId()).isEqualTo(response.get(0).getReviewId());
				assertThat(pageResponse.getData().get(1).getReviewId()).isEqualTo(response.get(1).getReviewId());
				assertThat(pageResponse.getData().get(2).getReviewId()).isEqualTo(response.get(2).getReviewId());
			}

			@Test
			@DisplayName("존재하지 않는 판매자 조회시 빈 결과를 반환한다")
			public void nonExistentSellerReturnsEmptyTest() {

				//given
				ReadSellerReviewRequest request = ReviewFixture.createPagingSellerReviewRequest();
				List<ReadSellerReviewResponse> response = Collections.emptyList();

				given(reviewRepository.findSellerReviewWithCursor(request)).willReturn(response);

				//when
				CursorPageResponse<ReadSellerReviewResponse> pageResponse = reviewService.readSellerReview(request);

				//then
				assertThat(pageResponse.getData()).hasSize(0);
				assertThat(pageResponse.getPageInfo().isHasNext()).isFalse();
				assertThat(pageResponse.getPageInfo().getNextCursorId()).isNull();
			}
		}
	}

	@Nested
	@DisplayName("내가 받은 리뷰 조회")
	class ReadMyReceivedReview {

		@Test
		@DisplayName("페이징조건 없이 보내면 기본 값으로 조회된다")
		public void readDefaultPagingConditionTest() {

			//given
			ReadMyReviewRequest request = ReviewFixture.createDefaultMyReviewRequest();
			List<ReadMyReceivedReviewResponse> response = ReviewFixture.create3MyReceivedReviewResponses();

			given(reviewRepository.findMyReceivedReviews(request)).willReturn(response);

			//when
			CursorPageResponse<ReadMyReceivedReviewResponse> pageResponse = reviewService.readMyReceivedReview(request);

			//then
			assertThat(pageResponse.getPageInfo().getSize()).isEqualTo(request.size());
			assertThat(pageResponse.getPageInfo().isHasNext()).isTrue();
			assertThat(pageResponse.getPageInfo().getNextCursorId()).isEqualTo(response.get(request.size() - 1).getReviewId());

			assertThat(pageResponse.getData()).hasSize(request.size());
			assertThat(pageResponse.getData().get(0).getReviewId()).isEqualTo(response.get(0).getReviewId());
			assertThat(pageResponse.getData().get(1).getReviewId()).isEqualTo(response.get(1).getReviewId());
		}

		@Test
		@DisplayName("마지막 페이지면 hasNext 가 false 이다")
		public void lastPageHasNextFalseTest() {

			//given
			ReadMyReviewRequest request = ReviewFixture.createFinalPageMyReviewRequest();
			List<ReadMyReceivedReviewResponse> response = ReviewFixture.create4MyReceivedReviewResponses();

			given(reviewRepository.findMyReceivedReviews(request)).willReturn(response);

			//when
			CursorPageResponse<ReadMyReceivedReviewResponse> pageResponse = reviewService.readMyReceivedReview(request);

			//then
			assertThat(pageResponse.getPageInfo().getSize()).isEqualTo(request.size());
			assertThat(pageResponse.getPageInfo().isHasNext()).isFalse();
			assertThat(pageResponse.getPageInfo().getNextCursorId()).isNull();

			assertThat(pageResponse.getData()).hasSize(request.size());
			assertThat(pageResponse.getData().get(0).getReviewId()).isEqualTo(response.get(0).getReviewId());
			assertThat(pageResponse.getData().get(1).getReviewId()).isEqualTo(response.get(1).getReviewId());
			assertThat(pageResponse.getData().get(2).getReviewId()).isEqualTo(response.get(2).getReviewId());
		}

		@Test
		@DisplayName("존재하지 않는 판매자 조회시 빈 결과를 반환한다")
		public void nonExistentSellerReturnsEmptyTest() {

			//given
			ReadMyReviewRequest request = ReviewFixture.createPagingMyReviewRequest();
			List<ReadMyReceivedReviewResponse> response = Collections.emptyList();

			given(reviewRepository.findMyReceivedReviews(request)).willReturn(response);

			//when
			CursorPageResponse<ReadMyReceivedReviewResponse> pageResponse = reviewService.readMyReceivedReview(request);


			//then
			assertThat(pageResponse.getData()).hasSize(0);
			assertThat(pageResponse.getPageInfo().isHasNext()).isFalse();
			assertThat(pageResponse.getPageInfo().getNextCursorId()).isNull();
		}
	}

	@Nested
	@DisplayName("내가 받은 리뷰 조회")
	class ReadMyWrittenReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("페이징조건 없이 보내면 기본 값으로 조회된다")
			public void readDefaultPagingConditionTest() {

				//given
				ReadMyReviewRequest request = ReviewFixture.createDefaultMyReviewRequest();
				List<ReadMyWrittenReviewResponse> response = ReviewFixture.create3MyWrittenReviewResponses();

				given(reviewRepository.findMyWrittenReviews(request)).willReturn(response);

				//when
				CursorPageResponse<ReadMyWrittenReviewResponse> pageResponse = reviewService.readMyWrittenReview(request);

				//then
				assertThat(pageResponse.getPageInfo().getSize()).isEqualTo(request.size());
				assertThat(pageResponse.getPageInfo().isHasNext()).isTrue();
				assertThat(pageResponse.getPageInfo().getNextCursorId()).isEqualTo(response.get(request.size() - 1).getReviewId());

				assertThat(pageResponse.getData()).hasSize(request.size());
				assertThat(pageResponse.getData().get(0).getReviewId()).isEqualTo(response.get(0).getReviewId());
				assertThat(pageResponse.getData().get(1).getReviewId()).isEqualTo(response.get(1).getReviewId());
			}

			@Test
			@DisplayName("마지막 페이지면 hasNext 가 false 이다")
			public void lastPageHasNextFalseTest() {

				//given
				ReadMyReviewRequest request = ReviewFixture.createFinalPageMyReviewRequest();
				List<ReadMyWrittenReviewResponse> response = ReviewFixture.create4MyWrittenReviewResponses();

				given(reviewRepository.findMyWrittenReviews(request)).willReturn(response);

				//when
				CursorPageResponse<ReadMyWrittenReviewResponse> pageResponse = reviewService.readMyWrittenReview(request);

				//then
				assertThat(pageResponse.getPageInfo().getSize()).isEqualTo(request.size());
				assertThat(pageResponse.getPageInfo().isHasNext()).isFalse();
				assertThat(pageResponse.getPageInfo().getNextCursorId()).isNull();

				assertThat(pageResponse.getData()).hasSize(request.size());
				assertThat(pageResponse.getData().get(0).getReviewId()).isEqualTo(response.get(0).getReviewId());
				assertThat(pageResponse.getData().get(1).getReviewId()).isEqualTo(response.get(1).getReviewId());
				assertThat(pageResponse.getData().get(2).getReviewId()).isEqualTo(response.get(2).getReviewId());
			}

			@Test
			@DisplayName("존재하지 않는 판매자 조회시 빈 결과를 반환한다")
			public void nonExistentSellerReturnsEmptyTest() {

				//given
				ReadMyReviewRequest request = ReviewFixture.createPagingMyReviewRequest();
				List<ReadMyWrittenReviewResponse> response = Collections.emptyList();

				given(reviewRepository.findMyWrittenReviews(request)).willReturn(response);

				//when
				CursorPageResponse<ReadMyWrittenReviewResponse> pageResponse = reviewService.readMyWrittenReview(request);


				//then
				assertThat(pageResponse.getData()).hasSize(0);
				assertThat(pageResponse.getPageInfo().isHasNext()).isFalse();
				assertThat(pageResponse.getPageInfo().getNextCursorId()).isNull();
			}
		}
	}


}
