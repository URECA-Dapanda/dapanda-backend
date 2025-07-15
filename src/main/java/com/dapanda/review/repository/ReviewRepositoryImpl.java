package com.dapanda.review.repository;

import com.dapanda.member.entity.QMember;
import com.dapanda.product.entity.QProduct;
import com.dapanda.review.dto.request.ReadReviewRequest;
import com.dapanda.review.dto.response.ReadReceivedReviewResponse;
import com.dapanda.review.dto.response.ReadWrittenReviewResponse;
import com.dapanda.review.entity.QReview;
import com.dapanda.review.entity.ReviewSortOption;
import com.dapanda.trade.entity.QTrade;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<ReadReceivedReviewResponse> findReceivedReviews(ReadReviewRequest request) {

		QReview review = QReview.review;
		QMember member = QMember.member;
		QTrade trade = QTrade.trade;
		QProduct product = QProduct.product;

		BooleanBuilder whereClause = new BooleanBuilder();

		whereClause.and(review.trade.product.member.id.eq(request.memberId()));

		if (request.cursorId() != null) {
			whereClause.and(buildCursorCondition(request.cursorId(), ReviewSortOption.valueOf(request.reviewSortOption())));
		}

		return queryFactory
				.select(Projections.constructor(ReadReceivedReviewResponse.class,
						review.id,
						review.rating,
						review.comment,
						review.createdAt,
						review.updatedAt,
						member.id,
						member.name,
						trade.id,
						trade.dataAmount,
						trade.timeAmount,
						product.id,
						product.itemType
				))
				.from(review)
				.join(review.trade, trade)
				.join(trade.product, product)
				.where(whereClause)
				.orderBy(getOrderSpecifier(ReviewSortOption.valueOf(request.reviewSortOption())))
				.limit(request.size() + 1)
				.fetch();
	}

	@Override
	public List<ReadWrittenReviewResponse> findWrittenReviews(ReadReviewRequest request) {

		QReview review = QReview.review;
		QMember member = QMember.member;
		QTrade trade = QTrade.trade;
		QProduct product = QProduct.product;

		BooleanBuilder whereClause = new BooleanBuilder();

		whereClause.and(review.trade.member.id.eq(request.memberId()));

		if (request.cursorId() != null) {
			whereClause.and(buildCursorCondition(request.cursorId(), ReviewSortOption.valueOf(request.reviewSortOption())));
		}

		return queryFactory
				.select(Projections.constructor(ReadWrittenReviewResponse.class,
						review.id,
						review.rating,
						review.comment,
						review.createdAt,
						review.updatedAt,
						member.id,
						member.name,
						trade.id,
						trade.dataAmount,
						trade.timeAmount,
						product.id,
						product.itemType
				))
				.from(review)
				.join(review.trade, trade)
				.join(trade.product, product)
				.where(whereClause)
				.orderBy(getOrderSpecifier(ReviewSortOption.valueOf(request.reviewSortOption())))
				.limit(request.size() + 1)
				.fetch();
	}

	private BooleanBuilder buildCursorCondition(Long cursorId, ReviewSortOption sortOption) {

		QReview review = QReview.review;
		BooleanBuilder condition = new BooleanBuilder();

		switch (sortOption) {
			case RECENT:

				condition.and(review.id.lt(cursorId));
				break;

			case OLDEST:

				condition.and(review.id.gt(cursorId));
				break;

			case RATING_DESC:

				Float cursorRating = queryFactory
						.select(review.rating)
						.from(review)
						.where(review.id.eq(cursorId))
						.fetchOne();

				if (cursorRating != null) {
					condition.and(
							review.rating.lt(cursorRating)
									.or(review.rating.eq(cursorRating).and(review.id.lt(cursorId)))
					);
				}
				break;

			case RATING_ASC:

				Float cursorRatingAsc = queryFactory
						.select(review.rating)
						.from(review)
						.where(review.id.eq(cursorId))
						.fetchOne();

				if (cursorRatingAsc != null) {
					condition.and(
							review.rating.gt(cursorRatingAsc)
									.or(review.rating.eq(cursorRatingAsc).and(review.id.gt(cursorId)))
					);
				}
				break;
		}

		return condition;
	}

	private OrderSpecifier<?>[] getOrderSpecifier(ReviewSortOption sortOption) {

		QReview review = QReview.review;

		return switch (sortOption) {
			case RECENT -> new OrderSpecifier[]{review.createdAt.desc(), review.id.desc()};
			case OLDEST -> new OrderSpecifier[]{review.createdAt.asc(), review.id.asc()};
			case RATING_DESC -> new OrderSpecifier[]{review.rating.desc(), review.id.desc()};
			case RATING_ASC -> new OrderSpecifier[]{review.rating.asc(), review.id.asc()};
		};
	}
}
