package com.dapanda.trade.repository;

import static com.dapanda.product.entity.QMobileData.mobileData;
import static com.dapanda.product.entity.QProduct.product;
import static com.dapanda.product.entity.QWifi.wifi;
import static com.dapanda.trade.entity.QTrade.trade;
import static com.dapanda.trade.entity.QTradeDetails.tradeDetails;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.product.entity.ItemType;
import com.dapanda.trade.dto.*;
import com.dapanda.trade.entity.TradeType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.*;
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
						trade.dataAmount.coalesce(new BigDecimal("0")),
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

						// description
						new CaseBuilder()
								.when(trade.tradeType.eq(TradeType.PURCHASE_MOBILE_SINGLE))
								.then(Expressions.stringTemplate(
										"CONCAT('데이터 일반 구매 ', ROUND({0}, 1), 'GB')",
										trade.dataAmount.coalesce(BigDecimal.ZERO)))
								.when(trade.tradeType.eq(TradeType.PURCHASE_MOBILE_COMPOSITE))
								.then(Expressions.stringTemplate(
										"CONCAT('데이터 자투리 구매 ', ROUND({0}, 1), 'GB')",
										trade.dataAmount.coalesce(BigDecimal.ZERO)))

								.when(trade.tradeType.eq(TradeType.PURCHASE_WIFI))
								.then(Expressions.stringTemplate("concat('와이파이 구매 ', {0}, '분')",
										trade.timeAmount.coalesce(0)))

								.when(trade.tradeType.eq(TradeType.SALE_MOBILE_DATA)
										.and(mobileData.isSplitType.isTrue()))
								.then(Expressions.stringTemplate(
										"concat('데이터 분할 판매 ', ROUND({0}, 1), 'GB')",
										trade.dataAmount.coalesce(BigDecimal.ZERO)))

								.when(trade.tradeType.eq(TradeType.SALE_MOBILE_DATA)
										.and(mobileData.isSplitType.isFalse()))
								.then(Expressions.stringTemplate(
										"concat('데이터 일반 판매 ', ROUND({0}, 1), 'GB')",
										trade.dataAmount.coalesce(BigDecimal.ZERO)))

								.when(trade.tradeType.eq(TradeType.SALE_WIFI))
								.then(Expressions.stringTemplate("concat('와이파이 ', {0}, '분')",
										trade.timeAmount.coalesce(0)))

								.otherwise("-"),

						// classification
						new CaseBuilder()
								.when(trade.tradeType.in(
										TradeType.PURCHASE_MOBILE_SINGLE,
										TradeType.PURCHASE_MOBILE_COMPOSITE,
										TradeType.PURCHASE_WIFI))
								.then("구매")
								.when(trade.tradeType.in(TradeType.SALE_MOBILE_DATA,
										TradeType.SALE_WIFI))
								.then("판매")
								.when(trade.tradeType.eq(TradeType.CHARGE))
								.then("충전")
								.when(trade.tradeType.eq(TradeType.REFUND))
								.then("출금")
								.otherwise("-"),

						trade.createdAt
				))
				.from(trade)
				.leftJoin(tradeDetails).on(tradeDetails.trade.eq(trade))
				.leftJoin(tradeDetails.product, product)
				.leftJoin(wifi)
				.on(product.itemType.eq(ItemType.WIFI).and(product.itemId.eq(wifi.id)))
				.leftJoin(mobileData)
				.on(product.itemType.eq(ItemType.MOBILE_DATA).and(product.itemId.eq(mobileData.id)))
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
						trade.tradeType.in(TradeType.PURCHASE_MOBILE_SINGLE,
								TradeType.PURCHASE_MOBILE_COMPOSITE, TradeType.PURCHASE_WIFI),
						trade.createdAt.between(startDate, endDate)
				)
				.fetchOne();

		Integer totalSelling = queryFactory
				.select(trade.tradingPrice.sum().coalesce(0))
				.from(trade)
				.where(
						trade.member.id.eq(memberId),
						trade.tradeType.in(TradeType.SALE_MOBILE_DATA, TradeType.SALE_WIFI),
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
				TradeType.PURCHASE_MOBILE_SINGLE,
				TradeType.PURCHASE_MOBILE_COMPOSITE,
				TradeType.PURCHASE_WIFI
		);
	}

	private BooleanExpression eqMemberId(Long memberId) {

		return trade.member.id.eq(memberId);
	}
}
