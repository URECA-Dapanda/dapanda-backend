package com.dapanda.trade.repository;

import static com.dapanda.product.entity.QProduct.product;
import static com.dapanda.product.entity.QWifi.wifi;
import static com.dapanda.trade.entity.QTrade.trade;
import static com.dapanda.trade.entity.QTradeDetails.tradeDetails;

import com.dapanda.product.entity.ItemType;
import com.dapanda.trade.entity.TradeDetails;
import com.dapanda.trade.entity.TradeType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TradeDetailsCustomRepositoryImpl implements TradeDetailsCustomRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<TradeDetails> findOngoingWifiTradeDetailsByMemberId(Long memberId) {

		LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.now());

		return queryFactory
				.selectFrom(tradeDetails)
				.join(tradeDetails.trade, trade).fetchJoin()
				.join(tradeDetails.product, product).fetchJoin()
				.join(wifi).on(product.itemType.eq(ItemType.WIFI)
						.and(product.itemId.eq(wifi.id)))
				.where(
						trade.member.id.eq(memberId),
						trade.tradeType.eq(TradeType.PURCHASE_WIFI),
						wifi.startTime.loe(now),
						wifi.endTime.gt(now)
				)
				.fetch();
	}
}
