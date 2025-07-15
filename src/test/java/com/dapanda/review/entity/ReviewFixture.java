package com.dapanda.review.entity;

import com.dapanda.member.entity.Member;
import com.dapanda.product.entity.ItemType;
import com.dapanda.review.dto.request.ReadMyReviewRequest;
import com.dapanda.review.dto.request.ReadSellerReviewRequest;
import com.dapanda.review.dto.response.ReadMyReceivedReviewResponse;
import com.dapanda.review.dto.response.ReadMyWrittenReviewResponse;
import com.dapanda.review.dto.response.ReadSellerReviewResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewFixture {

	public static Review createReview(Float rating, String comment, Long productId, Member reviewer, Member reviewee) {

		return Review.of(rating, comment, productId, reviewer, reviewee);
	}

	public static Review createReviewWithId(Float rating, String comment, Long productId, Member reviewer, Member reviewee, Long reviewId) {

		Review review = Review.of(rating, comment, productId, reviewer, reviewee);

		ReflectionTestUtils.setField(review, "id", reviewId);

		return review;
	}

	public static ReadMyReviewRequest createPagingMyReviewRequest() {

		return new ReadMyReviewRequest(1L, 4, ReviewSortOption.RECENT.name(), 1L);
	}

	public static ReadMyReviewRequest createFinalPageMyReviewRequest() {

		return new ReadMyReviewRequest(1L, 4, ReviewSortOption.RECENT.name(), 1L);
	}

	public static ReadMyReviewRequest createDefaultMyReviewRequest() {

		return new ReadMyReviewRequest(null, 2, ReviewSortOption.RECENT.name(), 1L);
	}

	public static List<ReadMyWrittenReviewResponse> create3MyWrittenReviewResponses() {

		LocalDateTime now = LocalDateTime.now();

		return List.of(
				ReadMyWrittenReviewResponse.of(
						1L,
						101L,
						4.5f,
						"데이터 거래가 매우 만족스러웠습니다!",      //
						now.minusDays(5),
						now.minusDays(5),
						"김철수",
						1L,
						100.0f,
						null,
						1L,
						ItemType.MOBILE_DATA
				),
				ReadMyWrittenReviewResponse.of(
						2L,
						102L,
						3.8f,
						"시간 거래 괜찮았어요",
						now.minusDays(4),
						now.minusDays(4),
						"이영희",
						2L,
						null,
						2,
						2L,
						ItemType.WIFI
				),
				ReadMyWrittenReviewResponse.of(
						3L,
						103L,
						5.0f,
						"완벽한 거래였습니다. 추천해요!",
						now.minusDays(3),
						now.minusDays(3),
						"박민수",
						3L,
						250.5f,
						null,
						3L,
						ItemType.MOBILE_DATA
				)
		);
	}

	public static List<ReadMyWrittenReviewResponse> create4MyWrittenReviewResponses() {

		LocalDateTime now = LocalDateTime.now();

		return List.of(
				ReadMyWrittenReviewResponse.of(
						2L,
						102L,
						3.8f,
						"시간 거래 괜찮았어요",
						now.minusDays(4),
						now.minusDays(4),
						"이영희",
						2L,
						null,
						2,
						2L,
						ItemType.WIFI
				),
				ReadMyWrittenReviewResponse.of(
						3L,
						103L,
						5.0f,
						"완벽한 거래였습니다. 추천해요!",
						now.minusDays(3),
						now.minusDays(3),
						"박민수",
						3L,
						250.5f,
						null,
						3L,
						ItemType.MOBILE_DATA
				),
				ReadMyWrittenReviewResponse.of(
						4L,
						104L,
						2.5f,
						"좀 아쉬웠네요...",
						now.minusDays(2),
						now.minusDays(2),
						"최지혜",
						4L,
						null,
						1,
						4L,
						ItemType.WIFI
				),
				ReadMyWrittenReviewResponse.of(
						5L,
						105L,
						4.2f,
						"전반적으로 좋은 경험이었습니다",
						now.minusDays(1),
						now.minusDays(1),
						"정태웅",
						5L,
						75.8f,
						null,
						5L,
						ItemType.MOBILE_DATA
				)
		);
	}

	public static List<ReadMyReceivedReviewResponse> create3MyReceivedReviewResponses() {

		LocalDateTime now = LocalDateTime.now();

		return List.of(
				ReadMyReceivedReviewResponse.of(
						1L,
						101L,
						4.5f,
						"데이터 거래가 매우 만족스러웠습니다!",
						now.minusDays(5),
						now.minusDays(5),
						"김철수",
						1L,
						100.0f,
						null,
						1L,
						ItemType.MOBILE_DATA
				),
				ReadMyReceivedReviewResponse.of(
						2L,
						102L,
						3.8f,
						"시간 거래 괜찮았어요",
						now.minusDays(4),
						now.minusDays(4),
						"이영희",
						2L,
						null,
						2,
						2L,
						ItemType.WIFI
				),
				ReadMyReceivedReviewResponse.of(
						3L,
						103L,
						5.0f,
						"완벽한 거래였습니다. 추천해요!",
						now.minusDays(3),
						now.minusDays(3),
						"박민수",
						3L,
						250.5f,
						null,
						3L,
						ItemType.MOBILE_DATA
				)
		);
	}

	public static List<ReadMyReceivedReviewResponse> create4MyReceivedReviewResponses() {

		LocalDateTime now = LocalDateTime.now();

		return List.of(
				ReadMyReceivedReviewResponse.of(
						2L,
						102L,
						3.8f,
						"시간 거래 괜찮았어요",
						now.minusDays(4),
						now.minusDays(4),
						"이영희",
						2L,
						null,
						2,
						2L,
						ItemType.WIFI
				),
				ReadMyReceivedReviewResponse.of(
						3L,
						103L,
						5.0f,
						"완벽한 거래였습니다. 추천해요!",
						now.minusDays(3),
						now.minusDays(3),
						"박민수",
						3L,
						250.5f,
						null,
						3L,
						ItemType.MOBILE_DATA
				),
				ReadMyReceivedReviewResponse.of(
						4L,
						104L,
						2.5f,
						"좀 아쉬웠네요...",
						now.minusDays(2),
						now.minusDays(2),
						"최지혜",
						4L,
						null,
						1,
						4L,
						ItemType.WIFI
				),
				ReadMyReceivedReviewResponse.of(
						5L,
						105L,
						4.2f,
						"전반적으로 좋은 경험이었습니다",
						now.minusDays(1),
						now.minusDays(1),
						"정태웅",
						5L,
						75.8f,
						null,
						5L,
						ItemType.MOBILE_DATA
				)
		);
	}

	//-----

	public static ReadSellerReviewRequest createPagingSellerReviewRequest() {

		return new ReadSellerReviewRequest(1L, 4, ReviewSortOption.RECENT.name(), 1L);
	}

	public static ReadSellerReviewRequest createFinalPageSellerReviewRequest() {

		return new ReadSellerReviewRequest(1L, 4, ReviewSortOption.RECENT.name(), 1L);
	}

	public static ReadSellerReviewRequest createDefaultSellerReviewRequest() {

		return new ReadSellerReviewRequest(null, 2, ReviewSortOption.RECENT.name(), 1L);
	}

	public static List<ReadSellerReviewResponse> create3SellerReviewResponses() {

		LocalDateTime now = LocalDateTime.now();

		return List.of(
				ReadSellerReviewResponse.of(
						1L,
						101L,
						4.5f,
						"데이터 거래가 매우 만족스러웠습니다!",      //
						now.minusDays(5),
						now.minusDays(5),
						"김철수",
						1L,
						100.0f,
						null,
						1L,
						ItemType.MOBILE_DATA
				),
				ReadSellerReviewResponse.of(
						2L,
						102L,
						3.8f,
						"시간 거래 괜찮았어요",
						now.minusDays(4),
						now.minusDays(4),
						"이영희",
						2L,
						null,
						2,
						2L,
						ItemType.WIFI
				),
				ReadSellerReviewResponse.of(
						3L,
						103L,
						5.0f,
						"완벽한 거래였습니다. 추천해요!",
						now.minusDays(3),
						now.minusDays(3),
						"박민수",
						3L,
						250.5f,
						null,
						3L,
						ItemType.MOBILE_DATA
				)
		);
	}

	public static List<ReadSellerReviewResponse> create4SellerReviewResponses() {

		LocalDateTime now = LocalDateTime.now();

		return List.of(
				ReadSellerReviewResponse.of(
						2L,
						102L,
						3.8f,
						"시간 거래 괜찮았어요",
						now.minusDays(4),
						now.minusDays(4),
						"이영희",
						2L,
						null,
						2,
						2L,
						ItemType.WIFI
				),
				ReadSellerReviewResponse.of(
						3L,
						103L,
						5.0f,
						"완벽한 거래였습니다. 추천해요!",
						now.minusDays(3),
						now.minusDays(3),
						"박민수",
						3L,
						250.5f,
						null,
						3L,
						ItemType.MOBILE_DATA
				),
				ReadSellerReviewResponse.of(
						4L,
						104L,
						2.5f,
						"좀 아쉬웠네요...",
						now.minusDays(2),
						now.minusDays(2),
						"최지혜",
						4L,
						null,
						1,
						4L,
						ItemType.WIFI
				),
				ReadSellerReviewResponse.of(
						5L,
						105L,
						4.2f,
						"전반적으로 좋은 경험이었습니다",
						now.minusDays(1),
						now.minusDays(1),
						"정태웅",
						5L,
						75.8f,
						null,
						5L,
						ItemType.MOBILE_DATA
				)
		);
	}
}
