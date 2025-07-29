package com.dapanda.review.repository;

import com.dapanda.member.entity.QMember;
import com.dapanda.review.dto.request.ReadReviewRequest;
import com.dapanda.review.dto.response.ReadReceivedReviewResponse;
import com.dapanda.review.dto.response.ReadWrittenReviewResponse;
import com.dapanda.review.entity.ReviewSortOption;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dapanda.product.entity.QProduct.product;
import static com.dapanda.product.entity.QWifi.wifi;
import static com.dapanda.review.entity.QReview.review;
import static com.dapanda.trade.entity.QTrade.trade;
import static com.dapanda.trade.entity.QTradeDetails.tradeDetails;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

	private final static String BUYER = "buyer";
	private final static String SELLER = "seller";
	private final static Long DEFAULT_CURSOR_ID = 0L;

	private final JPAQueryFactory queryFactory;

	@Override
	public List<ReadReceivedReviewResponse> findReceivedReviews(ReadReviewRequest request) {

		QMember buyer = new QMember(BUYER);
		QMember seller = new QMember(SELLER);

		return queryFactory
				.select(Projections.constructor(ReadReceivedReviewResponse.class,
						review.id,
						review.rating,
						review.comment,
						review.createdAt,
						review.updatedAt,
						buyer.id,
						buyer.name,
						trade.id,
						trade.timeAmount,
						product.id,
						product.itemType,
						wifi.title
				))
				.from(review)
				.join(review.trade, trade)
				.join(tradeDetails).on(tradeDetails.trade.eq(trade))
				.join(tradeDetails.product, product)
				.join(trade.member, buyer)
				.join(product.member, seller)
				.join(wifi).on(wifi.id.eq(product.itemId))
				.where(buildWhereClause(request, seller))
				.orderBy(getOrderSpecifier(ReviewSortOption.valueOf(request.reviewSortOption())))
				.limit(request.size() + 1)
				.fetch();
	}

	@Override
	public List<ReadWrittenReviewResponse> findWrittenReviews(ReadReviewRequest request) {

		QMember buyer = new QMember(BUYER);
		QMember seller = new QMember(SELLER);

		return queryFactory
				.select(Projections.constructor(ReadWrittenReviewResponse.class,
						review.id,
						review.rating,
						review.comment,
						review.createdAt,
						review.updatedAt,
						seller.id,
						seller.name,
						trade.id,
						trade.timeAmount,
						product.id,
						product.itemType
				))
				.from(review)
				.join(review.trade, trade)
				.join(tradeDetails).on(tradeDetails.trade.eq(trade))
				.join(tradeDetails.product, product)
				.join(trade.member, buyer)
				.join(product.member, seller)
				.where(buildWhereClause(request, buyer))
				.orderBy(getOrderSpecifier(ReviewSortOption.valueOf(request.reviewSortOption())))
				.limit(request.size() + 1)
				.fetch();
	}

	private BooleanBuilder buildWhereClause(ReadReviewRequest request, QMember member) {
		BooleanBuilder whereClause = new BooleanBuilder();

		whereClause.and(member.id.eq(request.memberId()));

		if (request.cursorId() != null && !request.cursorId().equals(DEFAULT_CURSOR_ID)) {
			whereClause.and(buildCursorCondition(request.cursorId(),
					ReviewSortOption.valueOf(request.reviewSortOption())));
		}
		return whereClause;
	}

	private BooleanBuilder buildCursorCondition(Long cursorId, ReviewSortOption sortOption) {

		BooleanBuilder condition = new BooleanBuilder();

		switch (sortOption) {
			case RECENT -> condition.and(review.id.lt(cursorId));
			case OLDEST -> condition.and(review.id.gt(cursorId));
		}
		return condition;
	}

	private OrderSpecifier<?>[] getOrderSpecifier(ReviewSortOption sortOption) {

		return switch (sortOption) {
			case RECENT -> new OrderSpecifier[]{review.createdAt.desc(), review.id.desc()};
			case OLDEST -> new OrderSpecifier[]{review.createdAt.asc(), review.id.asc()};
			case RATING_DESC -> new OrderSpecifier[]{review.rating.desc(), review.id.desc()};
			case RATING_ASC -> new OrderSpecifier[]{review.rating.asc(), review.id.asc()};
		};
	}
}
