package com.dapanda.trade.repository;

import static com.dapanda.product.entity.QProduct.product;
import static com.dapanda.product.entity.QWifi.wifi;
import static com.dapanda.trade.entity.QTrade.trade;
import static com.dapanda.trade.entity.QTradeDetails.tradeDetails;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.product.entity.ItemType;
import com.dapanda.trade.dto.TradeHistorySummary;
import com.dapanda.trade.entity.TradeType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TradeCustomRepositoryImpl implements TradeCustomRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public CursorPageResponse<TradeHistorySummary> findTradeHistoryByCursor(Long cursorId,
			int size, Long memberId) {

		List<TradeHistorySummary> content = queryFactory
				.select(Projections.constructor(TradeHistorySummary.class,
						trade.id,
						trade.tradeType,
						trade.dataAmount.coalesce(0f),
						wifi.title,
						trade.createdAt
				))
				.from(trade)
				.join(tradeDetails).on(tradeDetails.trade.eq(trade))
				.leftJoin(tradeDetails.product, product)
				.leftJoin(wifi).on(
						product.itemType.eq(ItemType.WIFI)
								.and(product.itemId.eq(wifi.id))
				)
				.where(eqMemberId(memberId),
						ltCursorId(cursorId),
						isValidTradeType())
				.orderBy(trade.createdAt.desc(), trade.id.asc())
				.limit(size + 1)
				.fetch();

		boolean hasNext = content.size() > size;
		if (hasNext) {
			content.remove(size);
		}
		Long nextCursorId = hasNext ? content.get(content.size() - 1).getTradeId() : null;

		return CursorPageResponse.of(content,
				CursorPageResponse.PageInfo.of(nextCursorId, hasNext, content.size()));
	}

	public Long countTradeHistoryByMemberId(Long memberId) {

		return queryFactory
				.select(trade.count())
				.from(trade)
				.where(eqMemberId(memberId), isValidTradeType())
				.fetchOne();
	}

	private BooleanExpression ltCursorId(Long cursorId) {

		return cursorId != null ? trade.id.lt(cursorId) : null;
	}

	private BooleanExpression isValidTradeType() {

		return trade.tradeType.in(
				TradeType.MOBILE_PURCHASE_SINGLE,
				TradeType.MOBILE_PURCHASE_COMPOSITE,
				TradeType.WIFI
		);
	}

	private BooleanExpression eqMemberId(Long memberId) {

		return trade.member.id.eq(memberId);
	}
}
