package com.dapanda.review.service;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.review.dto.request.CreateReviewRequest;
import com.dapanda.review.dto.request.ReadReviewRequest;
import com.dapanda.review.dto.request.UpdateReviewRequest;
import com.dapanda.review.dto.response.CreateReviewResponse;
import com.dapanda.review.dto.response.ReadReceivedReviewResponse;
import com.dapanda.review.dto.response.ReadReviewResponse;
import com.dapanda.review.dto.response.ReadWrittenReviewResponse;
import com.dapanda.review.dto.response.ReviewStatsResponse;
import com.dapanda.review.dto.response.UpdateReviewResponse;
import com.dapanda.review.entity.Review;
import com.dapanda.review.repository.ReviewRepository;
import com.dapanda.trade.entity.Trade;
import com.dapanda.trade.repository.TradeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final TradeRepository tradeRepository;
	private final MemberRepository memberRepository;

	public ReadReviewResponse readReview(Long reviewId, Long memberId) {

		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new GlobalException(ResultCode.REVIEW_NOT_FOUND));

		validateReviewOwner(review, memberId);

		return ReadReviewResponse.of(review.getId(), review.getRating(), review.getComment());
	}

	public CursorPageResponse<ReadWrittenReviewResponse> readWrittenReview(
			ReadReviewRequest request) {

		List<ReadWrittenReviewResponse> reviews = reviewRepository.findWrittenReviews(request);

		boolean hasNext = reviews.size() > request.size();

		if (hasNext) {
			reviews = reviews.subList(0, request.size());
		}

		Long nextCursorId = hasNext && !reviews.isEmpty()
				? reviews.get(reviews.size() - 1).getReviewId()
				: null;

		CursorPageResponse.PageInfo pageInfo = CursorPageResponse.PageInfo.of(
				nextCursorId,
				hasNext,
				request.size()
		);

		return CursorPageResponse.of(reviews, pageInfo);
	}

	public CursorPageResponse<ReadReceivedReviewResponse> readReceivedReview(
			ReadReviewRequest request) {

		List<ReadReceivedReviewResponse> reviews = reviewRepository.findReceivedReviews(request);

		boolean hasNext = reviews.size() > request.size();

		if (hasNext) {
			reviews = reviews.subList(0, request.size());
		}

		Long nextCursorId = hasNext && !reviews.isEmpty()
				? reviews.get(reviews.size() - 1).getReviewId()
				: null;

		CursorPageResponse.PageInfo pageInfo = CursorPageResponse.PageInfo.of(
				nextCursorId,
				hasNext,
				request.size()
		);

		return CursorPageResponse.of(reviews, pageInfo);
	}

	public CreateReviewResponse createReview(Long tradeId, CreateReviewRequest request,
			Long memberId) {

		Trade trade = tradeRepository.findById(tradeId)
				.orElseThrow(() -> new GlobalException(ResultCode.TRADE_NOT_FOUND));

		validateTradeOwner(trade, memberId);

		Member member = trade.getMember();

		ReviewStatsResponse stats = findReviewStatsByMember(member);
		int prevReviewCount = stats.reviewCount();
		float prevAverageRating = stats.averageRating();
		float newRating = request.rating();

		int newReviewCount = prevReviewCount + 1;
		float newAverageRating =
				((prevAverageRating * prevReviewCount) + newRating) / newReviewCount;

		member.updateReviewInfo(newReviewCount, newAverageRating);
		memberRepository.save(member);

		Review review = Review.of(request.rating(), request.comment(), trade);

		Review savedReview = reviewRepository.save(review);

		return CreateReviewResponse.from(savedReview.getId());
	}

	@Transactional
	public UpdateReviewResponse updateReview(Long reviewId, UpdateReviewRequest request,
			Long memberId) {

		Review savedReview = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new GlobalException(ResultCode.REVIEW_NOT_FOUND));

		validateReviewOwner(savedReview, memberId);

		Member member = savedReview.getTrade().getMember();

		List<Review> reviews = reviewRepository.findByTradeMember(member);

		int reviewCount = reviews.size();
		float averageRating = reviews.isEmpty()
				? 0.0f
				: (float) reviews.stream()
						.mapToDouble(Review::getRating)
						.average()
						.orElse(0.0);

		member.updateReviewInfo(reviewCount, averageRating);
		memberRepository.save(member);

		savedReview.updateReview(request);

		return UpdateReviewResponse.from(savedReview.getId());
	}

	public void deleteReview(Long reviewId, Long memberId) {

		Review savedReview = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new GlobalException(ResultCode.REVIEW_NOT_FOUND));

		validateReviewOwner(savedReview, memberId);

		Member member = savedReview.getTrade().getMember();

		int prevReviewCount = member.getReviewCount();
		float prevAverageRating = member.getAverageRating();
		float deletedRating = savedReview.getRating();

		int newReviewCount = prevReviewCount - 1;
		float newAverageRating = 0.0f;

		if (newReviewCount > 0) {
			newAverageRating =
					((prevAverageRating * prevReviewCount) - deletedRating) / newReviewCount;
		}

		member.updateReviewInfo(newReviewCount, newAverageRating);
		memberRepository.save(member);

		reviewRepository.delete(savedReview);
	}

	/**
	 * 리뷰 오너 검증
	 */
	private void validateReviewOwner(Review savedReview, Long memberId) {

		if (!savedReview.getTrade().getMember().getId().equals(memberId)) {

			throw new GlobalException(ResultCode.OTHER_REVIEW);
		}
	}

	/**
	 * 거래 내역 회원 아이디 검증
	 */
	private void validateTradeOwner(Trade trade, Long memberId) {

		if (!trade.getMember().getId().equals(memberId)) {

			throw new GlobalException(ResultCode.OTHER_TRADE);
		}
	}

	public ReviewStatsResponse findReviewStatsByMember(Member member) {
		List<Review> reviews = reviewRepository.findByTradeMember(member);

		int reviewCount = reviews.size();
		float averageRating = reviews.isEmpty()
				? 0.0f
				: (float) reviews.stream()
						.mapToDouble(Review::getRating)
						.average()
						.orElse(0.0);

		return new ReviewStatsResponse(reviewCount, averageRating);
	}

}
