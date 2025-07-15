package com.dapanda.review.entity;

import com.dapanda.product.entity.ItemType;
import com.dapanda.review.dto.request.ReadReviewRequest;
import com.dapanda.review.dto.response.ReadReceivedReviewResponse;
import com.dapanda.review.dto.response.ReadWrittenReviewResponse;
import com.dapanda.trade.entity.Trade;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewFixture {

	public static Review createReview1(Trade trade) {

		return Review.of(5.0f, "좋아요!", trade);
	}

	public static Review createReview1WithId(Trade trade, Long reviewId) {

		Review review = createReview1(trade);

		ReflectionTestUtils.setField(review, "id", reviewId);

		return review;
	}

	public static ReadReviewRequest createReviewRequest(Long cursorId, int size, String reviewSortOption, Long memberId) {

		return new ReadReviewRequest(cursorId, size, reviewSortOption, memberId);
	}

	public static List<ReadWrittenReviewResponse> create3WrittenReviewResponses() {

		LocalDateTime now = LocalDateTime.now();

		return List.of(
				ReadWrittenReviewResponse.of(
						1L,
						4.5f,
						"데이터 거래가 매우 만족스러웠습니다!",      //
						now.minusDays(5),
						now.minusDays(5),
						101L,
						"김철수",
						1L,
						100.0f,
						null,
						1L,
						ItemType.MOBILE_DATA
				),
				ReadWrittenReviewResponse.of(
						2L,
						3.8f,
						"시간 거래 괜찮았어요",
						now.minusDays(4),
						now.minusDays(4),
						102L,
						"이영희",
						2L,
						null,
						2,
						2L,
						ItemType.WIFI
				),
				ReadWrittenReviewResponse.of(
						3L,
						5.0f,
						"완벽한 거래였습니다. 추천해요!",
						now.minusDays(3),
						now.minusDays(3),
						103L,
						"박민수",
						3L,
						250.5f,
						null,
						3L,
						ItemType.MOBILE_DATA
				)
		);
	}

	public static List<ReadWrittenReviewResponse> create4WrittenReviewResponses() {

		LocalDateTime now = LocalDateTime.now();

		return List.of(
				ReadWrittenReviewResponse.of(
						2L,
						3.8f,
						"시간 거래 괜찮았어요",
						now.minusDays(4),
						now.minusDays(4),
						102L,
						"이영희",
						2L,
						null,
						2,
						2L,
						ItemType.WIFI
				),
				ReadWrittenReviewResponse.of(
						3L,
						5.0f,
						"완벽한 거래였습니다. 추천해요!",
						now.minusDays(3),
						now.minusDays(3),
						103L,
						"박민수",
						3L,
						250.5f,
						null,
						3L,
						ItemType.MOBILE_DATA
				),
				ReadWrittenReviewResponse.of(
						4L,
						2.5f,
						"좀 아쉬웠네요...",
						now.minusDays(2),
						now.minusDays(2),
						104L,
						"최지혜",
						4L,
						null,
						1,
						4L,
						ItemType.WIFI
				),
				ReadWrittenReviewResponse.of(
						5L,
						4.2f,
						"전반적으로 좋은 경험이었습니다",
						now.minusDays(1),
						now.minusDays(1),
						105L,
						"정태웅",
						5L,
						75.8f,
						null,
						5L,
						ItemType.MOBILE_DATA
				)
		);
	}

	public static List<ReadReceivedReviewResponse> create3ReceivedReviewResponses() {

		LocalDateTime now = LocalDateTime.now();

		return List.of(
				ReadReceivedReviewResponse.of(
						1L,
						4.5f,
						"데이터 거래가 매우 만족스러웠습니다!",
						now.minusDays(5),
						now.minusDays(5),
						101L,
						"김철수",
						1L,
						100.0f,
						null,
						1L,
						ItemType.MOBILE_DATA
				),
				ReadReceivedReviewResponse.of(
						2L,
						3.8f,
						"시간 거래 괜찮았어요",
						now.minusDays(4),
						now.minusDays(4),
						102L,
						"이영희",
						2L,
						null,
						2,
						2L,
						ItemType.WIFI
				),
				ReadReceivedReviewResponse.of(
						3L,
						5.0f,
						"완벽한 거래였습니다. 추천해요!",
						now.minusDays(3),
						now.minusDays(3),
						103L,
						"박민수",
						3L,
						250.5f,
						null,
						3L,
						ItemType.MOBILE_DATA
				)
		);
	}

	public static List<ReadReceivedReviewResponse> create4ReceivedReviewResponses() {

		LocalDateTime now = LocalDateTime.now();

		return List.of(
				ReadReceivedReviewResponse.of(
						2L,
						3.8f,
						"시간 거래 괜찮았어요",
						now.minusDays(4),
						now.minusDays(4),
						102L,
						"이영희",
						2L,
						null,
						2,
						2L,
						ItemType.WIFI
				),
				ReadReceivedReviewResponse.of(
						3L,
						5.0f,
						"완벽한 거래였습니다. 추천해요!",
						now.minusDays(3),
						now.minusDays(3),
						103L,
						"박민수",
						3L,
						250.5f,
						null,
						3L,
						ItemType.MOBILE_DATA
				),
				ReadReceivedReviewResponse.of(
						4L,
						2.5f,
						"좀 아쉬웠네요...",
						now.minusDays(2),
						now.minusDays(2),
						104L,
						"최지혜",
						4L,
						null,
						1,
						4L,
						ItemType.WIFI
				),
				ReadReceivedReviewResponse.of(
						5L,
						4.2f,
						"전반적으로 좋은 경험이었습니다",
						now.minusDays(1),
						now.minusDays(1),
						105L,
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
