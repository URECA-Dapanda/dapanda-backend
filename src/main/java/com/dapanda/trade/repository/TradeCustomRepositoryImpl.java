package com.dapanda.trade.repository;

import static com.dapanda.product.entity.QProduct.product;
import static com.dapanda.product.entity.QWifi.wifi;
import static com.dapanda.trade.entity.QTrade.trade;
import static com.dapanda.trade.entity.QTradeDetails.tradeDetails;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.product.entity.ItemType;
import com.dapanda.trade.dto.CashHistoryMonthlySummary;
import com.dapanda.trade.dto.CashHistorySummary;
import com.dapanda.trade.dto.PurchaseHistorySummary;
import com.dapanda.trade.entity.TradeType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TradeCustomRepositoryImpl implements TradeCustomRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public CursorPageResponse<PurchaseHistorySummary> findTradeHistoryByCursor(Long cursorId,
			int size, Long memberId) {

		List<PurchaseHistorySummary> content = queryFactory
				.select(Projections.constructor(PurchaseHistorySummary.class,
						trade.id,
						trade.tradeType,
						trade.dataAmount.coalesce(0f),
						wifi.title.coalesce(""),
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

	@Override
	public CursorPageResponse<CashHistorySummary> findCashHistoryByCursor(Long cursorId, int size,
			Long memberId, int year, int month) {

		LocalDateTime startDate = LocalDate.of(year, month, 1).atStartOfDay();
		LocalDateTime endDate = startDate.toLocalDate()
				.withDayOfMonth(startDate.toLocalDate().lengthOfMonth())
				.atTime(LocalTime.MAX);

		List<CashHistorySummary> content = queryFactory
				.select(Projections.constructor(CashHistorySummary.class,
						trade.id,
						trade.tradeType,
						trade.tradingPrice,
						new CaseBuilder()
								.when(trade.tradeType.in(TradeType.MOBILE_PURCHASE_SINGLE,
										TradeType.MOBILE_PURCHASE_COMPOSITE))
								// SQL 문자열 연결 연산자 이용
								.then(Expressions.stringTemplate("'' || {0}",
										trade.dataAmount.coalesce(0F)))
								.when(trade.tradeType.eq(TradeType.WIFI))
								.then(Expressions.stringTemplate("'' || {0}",
										trade.timeAmount.coalesce(0)))
								.otherwise("-"),
						trade.createdAt
				))
				.from(trade)
				.leftJoin(tradeDetails).on(tradeDetails.trade.eq(trade))
				.leftJoin(tradeDetails.product, product)
				.leftJoin(wifi)
				.on(product.itemType.eq(ItemType.WIFI).and(product.itemId.eq(wifi.id)))
				.where(
						eqMemberId(memberId),
						ltCursorId(cursorId),
						trade.createdAt.between(startDate, endDate)
				)
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

	public CashHistoryMonthlySummary calculateMonthlySummary(Long memberId, int year, int month) {

		LocalDateTime startDate = LocalDate.of(year, month, 1).atStartOfDay();
		LocalDateTime endDate = startDate.toLocalDate()
				.withDayOfMonth(startDate.toLocalDate().lengthOfMonth())
				.atTime(LocalTime.MAX);

		Integer totalPurchase = queryFactory
				.select(trade.tradingPrice.sum().coalesce(0))
				.from(trade)
				.where(
						trade.member.id.eq(memberId),
						trade.tradeType.in(TradeType.MOBILE_PURCHASE_SINGLE,
								TradeType.MOBILE_PURCHASE_COMPOSITE, TradeType.WIFI),
						trade.createdAt.between(startDate, endDate)
				)
				.fetchOne();

		Integer totalSelling = queryFactory
				.select(trade.tradingPrice.sum().coalesce(0))
				.from(trade)
				.where(
						trade.member.id.eq(memberId),
						trade.tradeType.in(TradeType.SALE),
						trade.createdAt.between(startDate, endDate)
				)
				.fetchOne();

		Integer totalCharge = queryFactory
				.select(trade.tradingPrice.sum().coalesce(0))
				.from(trade)
				.where(
						trade.member.id.eq(memberId),
						trade.tradeType.eq(TradeType.CHARGE),
						trade.createdAt.between(startDate, endDate)
				)
				.fetchOne();

		Integer totalRefund = queryFactory
				.select(trade.tradingPrice.sum().coalesce(0))
				.from(trade)
				.where(
						trade.member.id.eq(memberId),
						trade.tradeType.eq(TradeType.REFUND),
						trade.createdAt.between(startDate, endDate)
				)
				.fetchOne();

		return new CashHistoryMonthlySummary(
				totalPurchase != null ? totalPurchase : 0,
				totalSelling != null ? totalSelling : 0,
				totalCharge != null ? totalCharge : 0,
				totalRefund != null ? totalRefund : 0,
				totalCharge + totalSelling - totalPurchase - totalRefund
		);
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
