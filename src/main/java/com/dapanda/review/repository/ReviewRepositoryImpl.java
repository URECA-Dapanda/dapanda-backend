package com.dapanda.review.repository;

import com.dapanda.member.entity.QMember;
import com.dapanda.product.entity.QProduct;
import com.dapanda.review.dto.request.ReadSellerReviewRequest;
import com.dapanda.review.dto.response.ReadSellerReviewResponse;
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
	public List<ReadSellerReviewResponse> findSellerReviewWithCursor(ReadSellerReviewRequest request) {

		QReview review = QReview.review;
		QMember reviewer = QMember.member;
		QTrade trade = QTrade.trade;
		QProduct product = QProduct.product;

		BooleanBuilder whereClause = new BooleanBuilder();

		whereClause.and(review.reviewee.id.eq(request.sellerId()));

		if (request.cursorId() != null) {
			whereClause.and(buildCursorCondition(request.cursorId(), ReviewSortOption.valueOf(request.reviewSortOption())));
		}

		return queryFactory
				.select(Projections.constructor(ReadSellerReviewResponse.class,
						review.id,
						reviewer.id,
						reviewer.name,
						review.rating,
						review.comment,
						trade.tradingAmount,
						product.itemType,
						review.createdAt,
						review.updatedAt
				))
				.from(review)
				.join(review.reviewer, reviewer)
				.join(product).on(review.productId.eq(product.id))
				.join(trade).on(product.id.eq(trade.productId))
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
