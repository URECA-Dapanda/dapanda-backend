package com.dapanda.member.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.dapanda.member.entity.QMember.member;
import static com.dapanda.product.entity.QProduct.product;
import static com.dapanda.review.entity.QReview.review;
import static com.dapanda.trade.entity.QTrade.trade;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{

	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<Long> findMemberIdByProductId(Long productId) {

		return Optional.ofNullable(queryFactory
				.select(member.id)
				.from(product)
				.join(product.member, member)
				.where(product.id.eq(productId))
				.fetchOne()
		);
	}

	@Override
	public Optional<Long> findMemberIdByReviewId(Long reviewId) {

		return Optional.ofNullable(queryFactory
				.select(member.id)
				.from(review)
				.join(review.trade, trade)
				.join(trade.member, member)
				.where(review.id.eq(reviewId))
				.fetchOne());
	}
}
