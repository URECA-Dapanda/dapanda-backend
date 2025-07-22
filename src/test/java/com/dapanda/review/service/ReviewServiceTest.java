package com.dapanda.review.service;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.review.dto.request.CreateReviewRequest;
import com.dapanda.review.dto.request.ReadReviewRequest;
import com.dapanda.review.dto.request.UpdateReviewRequest;
import com.dapanda.review.dto.response.*;
import com.dapanda.review.entity.Review;
import com.dapanda.review.entity.ReviewFixture;
import com.dapanda.review.repository.ReviewRepository;
import com.dapanda.trade.entity.Trade;
import com.dapanda.trade.entity.TradeFixture;
import com.dapanda.trade.repository.TradeRepository;
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

import static com.dapanda.TestConstants.Member.*;
import static com.dapanda.TestConstants.Pagination.*;
import static com.dapanda.TestConstants.Review.*;
import static com.dapanda.TestConstants.Trade.TRADE_ID_1;
import static com.dapanda.TestConstants.Trade.TRADE_ID_2;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("리뷰 서비스 테스트")
class ReviewServiceTest {

	@Mock
	ReviewRepository reviewRepository;

	@Mock
	TradeRepository tradeRepository;

	@Mock
	MemberRepository memberRepository;

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
				Member savedBuyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				Trade savedTrade = TradeFixture.createTrade1WithId(savedBuyer,
						TRADE_ID_1);

				CreateReviewRequest request = new CreateReviewRequest(RATING, COMMENT);

				Review savedReview = ReviewFixture.createReview1WithId(savedTrade, REVIEW_ID);

				given(tradeRepository.findById(savedTrade.getId())).willReturn(
						Optional.of(savedTrade));
				given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

				//when
				CreateReviewResponse response = reviewService.createReview(savedTrade.getId(),
						request, BUYER_MEMBER_ID);

				//then
				assertThat(response.getReviewId()).isEqualTo(savedReview.getId());

				verify(reviewRepository).save(any(Review.class));
			}

			@Test
			@DisplayName("리뷰 등록시 평균 평점과 리뷰 개수가 올바르게 반영된다")
			void updateMemberRatingOnCreateReview() {

				//given
				float prevAverage = 4.0f;
				int prevCount = 2;
				float newRating = 5.0f;

				Member member = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				member.updateReviewInfo(prevCount, prevAverage);

				Trade trade = TradeFixture.createTrade1WithId(member, TRADE_ID_1);

				CreateReviewRequest request = new CreateReviewRequest(newRating, COMMENT);
				Review savedReview = ReviewFixture.createReview1WithId(trade, REVIEW_ID);

				given(tradeRepository.findById(trade.getId())).willReturn(Optional.of(trade));
				given(reviewRepository.save(any(Review.class))).willReturn(savedReview);
				given(reviewRepository.findByTradeMember(member)).willReturn(List.of(
						ReviewFixture.createReviewWithRating(trade, 1L, 4.0f),
						ReviewFixture.createReviewWithRating(trade, 2L, 4.0f)
				));

				//when
				reviewService.createReview(trade.getId(), request, BUYER_MEMBER_ID);

				//then
				// (4.0*2 + 5.0) / 3 = 4.33...
				assertThat(member.getReviewCount()).isEqualTo(3);
				assertThat(member.getAverageRating()).isCloseTo(4.33f, within(0.01f));
			}
		}

		@DisplayName("실패 케이스")
		@Nested
		class Fail {

			@Test
			@DisplayName("거래 내역이 존재하지 않을 경우 예외가 발생한다")
			public void selfReviewTest() {

				//given
				CreateReviewRequest request = new CreateReviewRequest(RATING, COMMENT);

				given(tradeRepository.findById(TRADE_ID_2)).willReturn(Optional.empty());

				//when & then
				assertThatThrownBy(
						() -> reviewService.createReview(TRADE_ID_2, request,
								USER_DETAILS_MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.TRADE_NOT_FOUND.getMessage());

				verify(reviewRepository, never()).save(any());
			}

			@Test
			@DisplayName("다른 회원의 거래 내역일 경우 에외가 발생한다")
			public void memberNotFoundTest() {

				//given
				CreateReviewRequest request = new CreateReviewRequest(RATING, COMMENT);

				Member buyer = MemberFixture.createMember2WithId(BUYER_MEMBER_ID);
				Trade trade = TradeFixture.createTrade1WithId(buyer, TRADE_ID_1);

				given(tradeRepository.findById(TRADE_ID_1)).willReturn(Optional.of(trade));

				//when & then
				assertThatThrownBy(
						() -> reviewService.createReview(TRADE_ID_1, request,
								USER_DETAILS_MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.OTHER_TRADE.getMessage());

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
				Member buyer = MemberFixture.createMember2WithId(BUYER_MEMBER_ID);
				Trade trade = TradeFixture.createTrade1WithId(buyer, TRADE_ID_1);
				Review review = ReviewFixture.createReview1WithId(trade, REVIEW_ID);

				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(review));

				//when
				reviewService.deleteReview(REVIEW_ID, buyer.getId());

				//then
				verify(reviewRepository).delete(review);
			}

			@Test
			@DisplayName("리뷰 삭제시 평균 평점과 리뷰 개수가 올바르게 반영된다")
			void updateMemberRatingOnDeleteReview() {

				//given
				float prevAverage = 4.5f;
				int prevCount = 2;
				float deletedRating = 5.0f;

				Member member = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				member.updateReviewInfo(prevCount, prevAverage);

				Trade trade = TradeFixture.createTrade1WithId(member, TRADE_ID_1);
				Review review = ReviewFixture.createReviewWithRating(trade, REVIEW_ID,
						deletedRating);

				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(review));

				//when
				reviewService.deleteReview(REVIEW_ID, member.getId());

				//then
				// (4.5*2 - 5.0) / 1 = 4.0
				assertThat(member.getReviewCount()).isEqualTo(1);
				assertThat(member.getAverageRating()).isCloseTo(4.0f, within(0.01f));
			}

			@Test
			@DisplayName("리뷰가 1개 남은 상태에서 삭제하면 리뷰 개수와 평균이 0이 된다")
			void memberReviewBecomesZeroOnSingleDelete() {

				//given
				float prevAverage = 5.0f;
				int prevCount = 1;
				float deletedRating = 5.0f;

				Member member = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				member.updateReviewInfo(prevCount, prevAverage);

				Trade trade = TradeFixture.createTrade1WithId(member, TRADE_ID_2);
				Review review = ReviewFixture.createReviewWithRating(trade, REVIEW_ID,
						deletedRating);

				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(review));

				//when
				reviewService.deleteReview(REVIEW_ID, member.getId());

				//then
				assertThat(member.getReviewCount()).isEqualTo(0);
				assertThat(member.getAverageRating()).isEqualTo(0.0f);
			}


		}

		@DisplayName("실패 케이스")
		@Nested
		class Fail {

			@Test
			@DisplayName("존재하지 않는 리뷰면 예외가 발생한다")
			public void reviewNotFoundTest() {

				//given
				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.empty());

				//when & then
				assertThatThrownBy(
						() -> reviewService.deleteReview(REVIEW_ID, USER_DETAILS_MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.REVIEW_NOT_FOUND.getMessage());
			}

			@Test
			@DisplayName("리뷰 작성자가 아닌 경우 예외가 발생한다")
			public void reviewOwnerTest() {

				//given
				Member buyer = MemberFixture.createMember2WithId(BUYER_MEMBER_ID);
				Trade trade = TradeFixture.createTrade1WithId(buyer, TRADE_ID_1);
				Review review = ReviewFixture.createReview1WithId(trade, REVIEW_ID);

				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(review));

				//when & then
				assertThatThrownBy(
						() -> reviewService.deleteReview(REVIEW_ID, USER_DETAILS_MEMBER_ID))
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
				UpdateReviewRequest request = new UpdateReviewRequest(RATING, COMMENT);

				Member buyer = MemberFixture.createMember2WithId(BUYER_MEMBER_ID);
				Trade trade = TradeFixture.createTrade1WithId(buyer, TRADE_ID_1);
				Review originalReview = ReviewFixture.createReview1WithId(trade, REVIEW_ID);

				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(originalReview));

				//when
				UpdateReviewResponse response = reviewService.updateReview(REVIEW_ID, request,
						buyer.getId());

				//then
				assertThat(response.getReviewId()).isEqualTo(originalReview.getId());
			}

			@Test
			@DisplayName("리뷰 수정시 평균 평점이 올바르게 반영된다")
			void updateMemberRatingOnUpdateReview() {

				//given
				float prevAverage = 4.0f;
				int reviewCount = 2;
				float oldRating = 3.0f;
				float newRating = 5.0f;

				Member member = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				member.updateReviewInfo(reviewCount, prevAverage);

				Trade trade = TradeFixture.createTrade1WithId(member, TRADE_ID_2);
				Review review = ReviewFixture.createReviewWithRating(trade, REVIEW_ID,
						oldRating);
				System.out.println(member == review.getTrade().getMember());
				UpdateReviewRequest request = new UpdateReviewRequest(newRating, COMMENT);

				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(review));
				given(reviewRepository.findByTradeMember(member)).willReturn(List.of(
						ReviewFixture.createReviewWithRating(trade, 1L, newRating),
						ReviewFixture.createReviewWithRating(trade, 2L, 5.0f)
				));

				//when
				reviewService.updateReview(REVIEW_ID, request, member.getId());

				//then
				// ((4.0*2) - 3.0 + 5.0) / 2 = 5.0
				assertThat(member.getReviewCount()).isEqualTo(2);
				assertThat(member.getAverageRating()).isCloseTo(5.0f, within(0.01f));
			}

		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("리뷰가 존재하지 않으면 예외가 발생한다")
			public void reviewNotFoundTest() {

				//given
				UpdateReviewRequest request = new UpdateReviewRequest(RATING, COMMENT);

				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.empty());

				//when & then
				assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, request,
						USER_DETAILS_MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.REVIEW_NOT_FOUND.getMessage());

				verify(reviewRepository).findById(REVIEW_ID);
			}

			@Test
			@DisplayName("리뷰 작성자가 아니면 예외가 발생한다")
			public void reviewOwnerTest() {

				//given
				UpdateReviewRequest request = new UpdateReviewRequest(RATING, COMMENT);

				Member buyer = MemberFixture.createMember2WithId(BUYER_MEMBER_ID);
				Trade trade = TradeFixture.createTrade1WithId(buyer, TRADE_ID_1);
				Review review = ReviewFixture.createReview1WithId(trade, REVIEW_ID);

				given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(review));

				//when & then
				assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, request,
						USER_DETAILS_MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.OTHER_REVIEW.getMessage());
			}
		}
	}

	@Nested
	@DisplayName("회원이 받은 리뷰 조회")
	class ReadMyReceivedReview {

		@Test
		@DisplayName("페이징조건 없이 보내면 기본 값으로 조회된다")
		public void readDefaultPagingConditionTest() {

			//given
			ReadReviewRequest request = ReviewFixture.createReviewRequest(
					DEFAULT_CURSOR_ID,
					DEFAULT_SIZE_2,
					DEFAULT_REVIEW_SORT_OPTION,
					USER_DETAILS_MEMBER_ID
			);

			List<ReadReceivedReviewResponse> response = ReviewFixture.create3ReceivedReviewResponses();

			given(reviewRepository.findReceivedReviews(request)).willReturn(response);

			//when
			CursorPageResponse<ReadReceivedReviewResponse> pageResponse = reviewService.readReceivedReview(
					request);

			//then
			assertThat(pageResponse.getPageInfo().getSize()).isEqualTo(request.size());
			assertThat(pageResponse.getPageInfo().isHasNext()).isTrue();
			assertThat(pageResponse.getPageInfo().getNextCursorId()).isEqualTo(
					response.get(request.size() - 1).getReviewId());

			assertThat(pageResponse.getData()).hasSize(request.size());
			assertThat(pageResponse.getData().get(0).getReviewId()).isEqualTo(
					response.get(0).getReviewId());
			assertThat(pageResponse.getData().get(1).getReviewId()).isEqualTo(
					response.get(1).getReviewId());
		}

		@Test
		@DisplayName("마지막 페이지면 hasNext 가 false 이다")
		public void lastPageHasNextFalseTest() {

			//given
			List<ReadReceivedReviewResponse> response = ReviewFixture.create4ReceivedReviewResponses();

			ReadReviewRequest request = ReviewFixture.createReviewRequest(
					DEFAULT_CURSOR_ID,
					response.size(),
					DEFAULT_REVIEW_SORT_OPTION,
					USER_DETAILS_MEMBER_ID
			);

			given(reviewRepository.findReceivedReviews(request)).willReturn(response);

			//when
			CursorPageResponse<ReadReceivedReviewResponse> pageResponse = reviewService.readReceivedReview(
					request);

			//then
			assertThat(pageResponse.getPageInfo().getSize()).isEqualTo(request.size());
			assertThat(pageResponse.getPageInfo().isHasNext()).isFalse();
			assertThat(pageResponse.getPageInfo().getNextCursorId()).isNull();

			assertThat(pageResponse.getData()).hasSize(request.size());
			assertThat(pageResponse.getData().get(0).getReviewId()).isEqualTo(
					response.get(0).getReviewId());
			assertThat(pageResponse.getData().get(1).getReviewId()).isEqualTo(
					response.get(1).getReviewId());
			assertThat(pageResponse.getData().get(2).getReviewId()).isEqualTo(
					response.get(2).getReviewId());
		}

		@Test
		@DisplayName("존재하지 않는 회원 조회시 빈 결과를 반환한다")
		public void nonExistentSellerReturnsEmptyTest() {

			//given
			ReadReviewRequest request = ReviewFixture.createReviewRequest(
					DEFAULT_CURSOR_ID,
					DEFAULT_SIZE_2,
					DEFAULT_REVIEW_SORT_OPTION,
					USER_DETAILS_MEMBER_ID
			);

			List<ReadReceivedReviewResponse> response = Collections.emptyList();

			given(reviewRepository.findReceivedReviews(request)).willReturn(response);

			//when
			CursorPageResponse<ReadReceivedReviewResponse> pageResponse = reviewService.readReceivedReview(
					request);

			//then
			assertThat(pageResponse.getData()).hasSize(0);
			assertThat(pageResponse.getPageInfo().isHasNext()).isFalse();
			assertThat(pageResponse.getPageInfo().getNextCursorId()).isNull();
		}
	}

	@Nested
	@DisplayName("회원이 작성한 리뷰 조회")
	class ReadMyWrittenReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("페이징조건 없이 보내면 기본 값으로 조회된다")
			public void readDefaultPagingConditionTest() {

				//given
				ReadReviewRequest request = ReviewFixture.createReviewRequest(
						DEFAULT_CURSOR_ID,
						DEFAULT_SIZE_2,
						DEFAULT_REVIEW_SORT_OPTION,
						USER_DETAILS_MEMBER_ID
				);

				List<ReadWrittenReviewResponse> response = ReviewFixture.create3WrittenReviewResponses();

				given(reviewRepository.findWrittenReviews(request)).willReturn(response);

				//when
				CursorPageResponse<ReadWrittenReviewResponse> pageResponse = reviewService.readWrittenReview(
						request);

				//then
				assertThat(pageResponse.getPageInfo().getSize()).isEqualTo(request.size());
				assertThat(pageResponse.getPageInfo().isHasNext()).isTrue();
				assertThat(pageResponse.getPageInfo().getNextCursorId()).isEqualTo(
						response.get(request.size() - 1).getReviewId());

				assertThat(pageResponse.getData()).hasSize(request.size());
				assertThat(pageResponse.getData().get(0).getReviewId()).isEqualTo(
						response.get(0).getReviewId());
				assertThat(pageResponse.getData().get(1).getReviewId()).isEqualTo(
						response.get(1).getReviewId());
			}

			@Test
			@DisplayName("마지막 페이지면 hasNext 가 false 이다")
			public void lastPageHasNextFalseTest() {

				//given
				List<ReadWrittenReviewResponse> response = ReviewFixture.create4WrittenReviewResponses();

				ReadReviewRequest request = ReviewFixture.createReviewRequest(
						DEFAULT_CURSOR_ID,
						response.size(),
						DEFAULT_REVIEW_SORT_OPTION,
						USER_DETAILS_MEMBER_ID
				);

				given(reviewRepository.findWrittenReviews(request)).willReturn(response);

				//when
				CursorPageResponse<ReadWrittenReviewResponse> pageResponse = reviewService.readWrittenReview(
						request);

				//then
				assertThat(pageResponse.getPageInfo().getSize()).isEqualTo(request.size());
				assertThat(pageResponse.getPageInfo().isHasNext()).isFalse();
				assertThat(pageResponse.getPageInfo().getNextCursorId()).isNull();

				assertThat(pageResponse.getData()).hasSize(request.size());
				assertThat(pageResponse.getData().get(0).getReviewId()).isEqualTo(
						response.get(0).getReviewId());
				assertThat(pageResponse.getData().get(1).getReviewId()).isEqualTo(
						response.get(1).getReviewId());
				assertThat(pageResponse.getData().get(2).getReviewId()).isEqualTo(
						response.get(2).getReviewId());
			}

			@Test
			@DisplayName("존재하지 않는 판매자 조회시 빈 결과를 반환한다")
			public void nonExistentSellerReturnsEmptyTest() {

				//given
				ReadReviewRequest request = ReviewFixture.createReviewRequest(
						DEFAULT_CURSOR_ID,
						DEFAULT_SIZE_2,
						DEFAULT_REVIEW_SORT_OPTION,
						USER_DETAILS_MEMBER_ID
				);

				List<ReadWrittenReviewResponse> response = Collections.emptyList();

				given(reviewRepository.findWrittenReviews(request)).willReturn(response);

				//when
				CursorPageResponse<ReadWrittenReviewResponse> pageResponse = reviewService.readWrittenReview(
						request);

				//then
				assertThat(pageResponse.getData()).hasSize(0);
				assertThat(pageResponse.getPageInfo().isHasNext()).isFalse();
				assertThat(pageResponse.getPageInfo().getNextCursorId()).isNull();
			}
		}
	}

	@Nested
	@DisplayName("리뷰 단건 조회")
	class ReadReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("요청이 유효하면 리뷰 단건을 반환한다")
			public void readReviewTest() {

				//given
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				Member seller = MemberFixture.createMember2WithId(SELLER_MEMBER_ID);

				Trade savedTrade = TradeFixture.createTrade1WithId(buyer, TRADE_ID_1);

				Review review = ReviewFixture.createReview1WithId(savedTrade, REVIEW_ID);

				given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));

				//when
				ReadReviewResponse response = reviewService.readReview(review.getId(),
						buyer.getId());

				//then
				assertThat(response.getReviewId()).isEqualTo(review.getId());
				assertThat(response.getRating()).isEqualTo(review.getRating());
				assertThat(response.getComment()).isEqualTo(review.getComment());
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("리뷰를 찾을 수 없으면 예외가 발생한다")
			public void reviewNotFoundTest() {

				//given
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				Member seller = MemberFixture.createMember2WithId(SELLER_MEMBER_ID);

				Trade savedTrade = TradeFixture.createTrade1WithId(buyer, TRADE_ID_1);

				Review review = ReviewFixture.createReview1WithId(savedTrade, REVIEW_ID);

				given(reviewRepository.findById(review.getId())).willReturn(Optional.empty());

				//when & then
				assertThatThrownBy(() -> reviewService.readReview(review.getId(), buyer.getId()))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.REVIEW_NOT_FOUND.getMessage());
			}

			@Test
			@DisplayName("리뷰 작성자가 아니면 예외가 발생한다")
			public void reviewOwnerTest() {

				//given
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				Member seller = MemberFixture.createMember2WithId(SELLER_MEMBER_ID);

				Trade savedTrade = TradeFixture.createTrade1WithId(buyer, TRADE_ID_1);

				Review review = ReviewFixture.createReview1WithId(savedTrade, REVIEW_ID);

				given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));

				//when & then
				assertThatThrownBy(
						() -> reviewService.readReview(review.getId(), USER_DETAILS_MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.OTHER_REVIEW.getMessage());
			}
		}
	}
}
