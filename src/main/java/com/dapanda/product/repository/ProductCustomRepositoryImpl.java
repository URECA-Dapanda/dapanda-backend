package com.dapanda.product.repository;

import static com.dapanda.member.entity.QMember.member;
import static com.dapanda.product.entity.QMobileData.mobileData;
import static com.dapanda.product.entity.QProduct.product;
import static com.dapanda.product.entity.QProductImage.productImage;
import static com.dapanda.product.entity.QWifi.wifi;
import static com.dapanda.trade.entity.QTrade.trade;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.QMember;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.request.ReadSellingProductRequest;
import com.dapanda.product.dto.response.*;
import com.dapanda.product.entity.*;
import com.dapanda.trade.dto.MobileDataScrap;
import com.dapanda.trade.entity.TradeType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductCustomRepositoryImpl implements ProductCustomRepository {

	// 기본 평점이 없는 경우 사용할 기본값
	private static final double DEFAULT_RATING = 0.0;

	// 미터 단위를 킬로미터로 변환하기 위한 나눗셈 상수
	private static final double METER_TO_KILOMETER = 1000.0;

	// QueryDSL에서 거리 계산을 위한 MySQL의 ST_DISTANCE_SPHERE 함수 템플릿
	private static final String DISTANCE_TEMPLATE = "ST_DISTANCE_SPHERE(POINT({0}, {1}), POINT({2}, {3}))";

	private final JPAQueryFactory queryFactory;

	@Override
	public CursorPageResponse<MobileDataSummary> findMobileDataByCursor(Long cursorId, int size,
			ProductSortOption productSortOption, BigDecimal dataAmount) {

		List<MobileDataSummary> content = queryFactory
				.select(Projections.constructor(MobileDataSummary.class,
						product.id,
						product.price,
						product.itemId,
						product.member.name,
						product.member.profileImageUrl.coalesce(""),
						mobileData.remainAmount,
						mobileData.pricePer100MB,
						mobileData.isSplitType,
						product.updatedAt
				))
				.from(product)
				.join(mobileData).on(mobileData.id.eq(product.itemId))
				.where(isActiveProduct(),
						mobileDataCondition(cursorId, productSortOption),
						eqDataAmount(dataAmount)
				)
				.orderBy(
						productSortOption == ProductSortOption.PRICE_ASC
								? mobileData.pricePer100MB.asc() :
								productSortOption == ProductSortOption.AMOUNT_ASC
										? mobileData.remainAmount.asc() :
										productSortOption == ProductSortOption.AMOUNT_DESC
												? mobileData.remainAmount.desc() :
												product.updatedAt.desc(), // default: RECENT
						product.id.asc()
				)
				.limit(size + 1)
				.fetch();

		boolean hasNext = content.size() > size;
		if (hasNext) {
			content.remove(size);
		}
		Long nextCursorId = hasNext ? content.get(content.size() - 1).getProductId() : null;

		return CursorPageResponse.of(content,
				CursorPageResponse.PageInfo.of(nextCursorId, hasNext, content.size()));
	}

	@Override
	public CursorPageResponse<WifiSummary> findWifiByCursor(Long cursorId, int size,
			ProductSortOption productSortOption, boolean isOpen, Double latitude,
			Double longitude) {

		QProductImage productImageSub = new QProductImage("productImageSub");

		LocalDateTime now = LocalDateTime.now();

		// 거리 계산
		NumberExpression<Double> distance = Expressions.numberTemplate(Double.class,
				DISTANCE_TEMPLATE, longitude, latitude, wifi.longitude, wifi.latitude);

		List<WifiSummary> content = queryFactory
				.select(Projections.constructor(WifiSummary.class,
						product.id,
						product.price,
						product.itemId,
						product.member.name,
						product.member.profileImageUrl.coalesce(""),
						wifi.title,
						Expressions.stringTemplate("MIN({0})", productImage.imageUrl),
						wifi.latitude,
						wifi.longitude,
						wifi.address,
						member.averageRating,
						distance.divide(METER_TO_KILOMETER),
						isCurrentTimeWithinTimeRange(),
						Expressions.stringTemplate("TIME({0})", wifi.startTime),
						Expressions.stringTemplate("TIME({0})", wifi.endTime)
				))
				.from(product)
				.groupBy(product.id)
				.join(wifi).on(wifi.id.eq(product.itemId))
				.leftJoin(member).on(product.member.id.eq(member.id))
				.leftJoin(productImage).on(
						productImage.wifiId.eq(wifi.id)
								.and(productImage.priority.eq(
										JPAExpressions
												.select(productImageSub.priority.min())
												.from(productImageSub)
												.where(productImageSub.wifiId.eq(wifi.id))
								))
				)
				.where(isActiveProduct(),
						wifiCursorCondition(cursorId, productSortOption, latitude, longitude),
						isOpenNow(isOpen)
				)
				.orderBy(
						productSortOption == ProductSortOption.PRICE_ASC ? product.price.asc() :
								productSortOption == ProductSortOption.AVERAGE_RATE_DESC
										? member.averageRating.desc() : distance.asc(),
						product.id.asc()
				)
				.limit(size + 1)
				.fetch();

		boolean hasNext = content.size() > size;
		if (hasNext) {
			content.remove(size);
		}
		Long nextCursorId = hasNext ? content.get(content.size() - 1).getProductId() : null;

		return CursorPageResponse.of(content,
				CursorPageResponse.PageInfo.of(nextCursorId, hasNext, content.size()));
	}

	@Override
	public MobileDataInfoResponse findMobileDataInfo(Long productId, Long memberId) {

		return queryFactory
				.select(Projections.constructor(MobileDataInfoResponse.class,
						product.id,
						mobileData.id,
						product.price,
						product.member.id,
						product.member.name,
						product.member.profileImageUrl.coalesce(""),
						mobileData.remainAmount,
						mobileData.pricePer100MB,
						member.averageRating,
						member.reviewCount,
						Expressions.booleanTemplate("{0} = {1}", product.member.id, memberId),
						mobileData.isSplitType,
						product.updatedAt
				))
				.from(product)
				.join(mobileData).on(product.itemId.eq(mobileData.id))
				.leftJoin(member).on(product.member.id.eq(member.id))
				.where(isActiveOrSoldOutProduct(),
						product.itemId.eq(mobileData.id),
						product.id.eq(productId)
				)
				.groupBy(product.id, mobileData.id, product.price, product.member,
						mobileData.remainAmount, mobileData.pricePer100MB, product.updatedAt)
				.fetchOne();
	}

	@Override
	public WifiInfoResponse findWifiInfo(Long productId, Long memberId) {

		return queryFactory
				.select(Projections.constructor(WifiInfoResponse.class,
						product.id,
						wifi.id,
						product.price,
						product.member.id,
						product.member.name,
						product.member.profileImageUrl.coalesce(""),
						wifi.title,
						wifi.content,
						wifi.latitude,
						wifi.longitude,
						wifi.address,
						member.averageRating,
						member.reviewCount,
						Expressions.booleanTemplate("{0} = {1}", product.member.id, memberId),
						Expressions.nullExpression(List.class),
						wifi.startTime,
						wifi.endTime,
						isCurrentTimeWithinTimeRange(),
						product.updatedAt
				))
				.from(product)
				.join(wifi).on(product.itemId.eq(wifi.id))
				.leftJoin(member).on(product.member.id.eq(member.id))
				.where(isActiveOrSoldOutProduct(),
						product.itemId.eq(wifi.id),
						product.id.eq(productId)
				)
				.groupBy(product.id, wifi.id, product.price, product.member, wifi.title,
						wifi.content, wifi.latitude, wifi.longitude, wifi.address, wifi.startTime,
						wifi.endTime, product.updatedAt)
				.fetchOne();
	}

	public List<String> findWifiImages(Long wifiId) {

		return queryFactory
				.select(productImage.imageUrl)
				.from(productImage)
				.where(productImage.wifiId.eq(wifiId))
				.orderBy(productImage.priority.asc())
				.fetch();
	}

	@Override
	public List<ReadSellingProductResponse> findSellingProduct(ReadSellingProductRequest request) {

		QProductImage productImageSub = new QProductImage("productImageSub");

		BooleanBuilder cursorCondition = new BooleanBuilder();

		if (request.cursorId() != null && request.cursorId() != 0L) {

			cursorCondition.and((product.id.lt(request.cursorId())));
		}

		List<Tuple> tuples = queryFactory
				.select(
						product.id,
						product.itemType,
						product.state,
						mobileData.dataAmount,
						mobileData.remainAmount,
						wifi.startTime,
						wifi.endTime,
						wifi.title,
						productImage.imageUrl.coalesce(""),
						product.createdAt,
						product.updatedAt
				)
				.from(product)
				.leftJoin(mobileData).on(
						product.itemId.eq(mobileData.id)
								.and(product.itemType.eq(ItemType.MOBILE_DATA))
				)
				.leftJoin(wifi).on(
						product.itemId.eq(wifi.id)
								.and(product.itemType.eq(ItemType.WIFI))
				)
				.leftJoin(productImage).on(
						productImage.wifiId.eq(wifi.id)
								.and(productImage.priority.eq(
										JPAExpressions.select(productImageSub.priority.min())
												.from(productImageSub)
												.where(productImageSub.wifiId.eq(wifi.id))
								))
				)
				.where(
						product.member.id.eq(request.memberId()),
						request.productState() != null ? product.state.eq(request.productState())
								: null,
						cursorCondition
				)
				.orderBy(product.createdAt.desc())
				.limit(request.size() + 1)
				.fetch();

		return tuples.stream()
				.map(tuple -> {

					ItemType type = tuple.get(product.itemType);

					if (type == ItemType.MOBILE_DATA) {

						return ReadSellingProductResponse.createMobileDataResponse(
								tuple.get(product.id),
								type,
								tuple.get(product.state),
								tuple.get(mobileData.dataAmount),
								tuple.get(mobileData.remainAmount),
								tuple.get(product.createdAt),
								tuple.get(product.updatedAt)
						);
					}

					return ReadSellingProductResponse.createWifiResponse(
							tuple.get(product.id),
							type,
							tuple.get(product.state),
							tuple.get(wifi.startTime),
							tuple.get(wifi.endTime),
							tuple.get(wifi.title),
							tuple.get(productImage.imageUrl),
							tuple.get(product.createdAt),
							tuple.get(product.updatedAt)
					);
				})
				.toList();
	}

	@Override
	public List<MobileDataScrap> findMobileDataScrap(BigDecimal dataAmount, Long memberId) {

		return queryFactory
				.select(Projections.constructor(MobileDataScrap.class,
						product.id,
						mobileData.id,
						product.member.name,
						product.member.profileImageUrl.coalesce(""),
						product.price,
						Expressions.constant(0),
						mobileData.remainAmount,
						Expressions.constant(BigDecimal.valueOf(0)),
						mobileData.pricePer100MB,
						mobileData.isSplitType,
						product.updatedAt
				))
				.from(product)
				.join(mobileData).on(product.itemId.eq(mobileData.id))
				.where(
						product.member.id.ne(memberId),
						product.state.eq(ProductState.ACTIVE),
						mobileData.remainAmount.gt(0)
				)
				.orderBy(
						mobileData.pricePer100MB.asc(),
						mobileData.remainAmount.desc(),
						mobileData.isSplitType.asc()
				)
				.limit(100) // 필요에 따라 조절
				.fetch();
	}

	@Override
	public BigDecimal sumSoldMobileDataAmountByMemberId(Long memberId) {

		BigDecimal sum = queryFactory
				.select(mobileData.dataAmount.sum())
				.from(product)
				.join(mobileData).on(product.itemId.eq(mobileData.id))
				.where(
						product.member.id.eq(memberId),
						product.state.in(ProductState.ACTIVE, ProductState.SOLD_OUT)
				)
				.fetchOne();

		return sum != null ? sum : new BigDecimal("0");
	}


	private BooleanExpression mobileDataCondition(Long cursorId,
			ProductSortOption productSortOption) {

		if (cursorId == null) {

			return null;
		}

		Product product = queryFactory
				.select(QProduct.product)
				.from(QProduct.product)
				.where(QProduct.product.id.eq(cursorId))
				.fetchOne();

		MobileData mobileData = queryFactory
				.select(QMobileData.mobileData)
				.from(QProduct.product)
				.join(QMobileData.mobileData)
				.on(QProduct.product.itemId.eq(QMobileData.mobileData.id))
				.where(QProduct.product.id.eq(cursorId))
				.fetchOne();

		if (productSortOption == ProductSortOption.PRICE_ASC) {

			return QMobileData.mobileData.pricePer100MB.gt(mobileData.getPricePer100MB())
					.or(QMobileData.mobileData.pricePer100MB.eq(mobileData.getPricePer100MB())
							.and(QProduct.product.id.gt(cursorId)));
		} else if (productSortOption == ProductSortOption.AMOUNT_ASC) {

			return QMobileData.mobileData.remainAmount.gt(mobileData.getRemainAmount())
					.or(QMobileData.mobileData.remainAmount.eq(mobileData.getRemainAmount()))
					.and(QProduct.product.id.gt(cursorId));
		} else if (productSortOption == ProductSortOption.AMOUNT_DESC) {

			return QMobileData.mobileData.remainAmount.lt(mobileData.getRemainAmount())
					.or(QMobileData.mobileData.remainAmount.eq(mobileData.getRemainAmount()))
					.and(QProduct.product.id.gt(cursorId));
		} else { // ProductSortOption.RECENT

			return QProduct.product.updatedAt.gt(product.getUpdatedAt())
					.or(QProduct.product.updatedAt.eq(product.getUpdatedAt())
							.and(QProduct.product.id.gt(cursorId)));
		}
	}

	private BooleanExpression wifiCursorCondition(Long cursorId,
			ProductSortOption productSortOption, Double latitude, Double longitude) {

		if (cursorId == null) {

			return null;
		}

		Product product = queryFactory
				.select(QProduct.product)
				.from(QProduct.product)
				.where(QProduct.product.id.eq(cursorId))
				.fetchOne();

		Wifi wifi = queryFactory
				.select(QWifi.wifi)
				.from(QProduct.product)
				.join(QWifi.wifi).on(QProduct.product.itemId.eq(QWifi.wifi.id))
				.where(QProduct.product.id.eq(cursorId))
				.fetchOne();

		Member member = queryFactory
				.select(QMember.member)
				.from(QProduct.product)
				.join(QMember.member).on(QProduct.product.member.eq(QMember.member))
				.where(QProduct.product.id.eq(cursorId))
				.fetchOne();

		if (productSortOption == ProductSortOption.PRICE_ASC) {

			return QProduct.product.price.gt(product.getPrice())
					.or(QProduct.product.price.eq(product.getPrice())
							.and(QProduct.product.id.gt(cursorId)));
		} else if (productSortOption == ProductSortOption.AVERAGE_RATE_DESC) {

			return QMember.member.averageRating.lt(member.getAverageRating())
					.or(QMember.member.averageRating.eq(member.getAverageRating()))
					.and(QProduct.product.id.gt(cursorId));
		} else { // ProductSortOption.DISTANCE_ASC

			NumberExpression<Double> cursorDistance = Expressions.numberTemplate(Double.class,
					DISTANCE_TEMPLATE, longitude, latitude, wifi.getLongitude(),
					wifi.getLatitude());
			NumberExpression<Double> productDistance = Expressions.numberTemplate(Double.class,
					DISTANCE_TEMPLATE, longitude, latitude, QWifi.wifi.longitude,
					QWifi.wifi.latitude);

			return productDistance.gt(cursorDistance)
					.or(productDistance.eq(cursorDistance).and(QProduct.product.id.gt(cursorId)));
		}
	}

	private BooleanExpression isActiveProduct() {

		return product.state.eq(ProductState.ACTIVE);
	}

	private BooleanExpression isActiveOrSoldOutProduct() {

		return product.state.in(ProductState.ACTIVE, ProductState.SOLD_OUT);
	}

	private BooleanExpression eqDataAmount(BigDecimal dataAmount) {

		return dataAmount != null
				? Expressions.booleanTemplate("ABS({0} - {1}) < 0.000001", mobileData.remainAmount,
				dataAmount) : null;
	}

	private BooleanExpression isOpenNow(boolean isOpen) {

		return isOpen ? isCurrentTimeWithinTimeRange() : null;
	}

	private BooleanExpression isCurrentTimeWithinTimeRange() {

		return Expressions.booleanTemplate(
				"(CASE WHEN TIME({1}) < TIME({0}) " +
						"THEN TIME(CURRENT_TIMESTAMP) >= TIME({0}) OR TIME(CURRENT_TIMESTAMP) <= TIME({1}) "
						+ "ELSE TIME(CURRENT_TIMESTAMP) BETWEEN TIME({0}) AND TIME({1}) END)",
				wifi.startTime, wifi.endTime
		);
	}

	public FindMarketPriceResponse findMarketPrice(ItemType itemType) {

		LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

		// 데이터 상품
		if (itemType == ItemType.MOBILE_DATA) {
			Integer recentPricePer100MB = queryFactory
					.select(mobileData.pricePer100MB)
					.from(product)
					.join(mobileData).on(product.itemId.eq(mobileData.id))
					.where(
							product.itemType.eq(itemType),
							product.updatedAt.after(oneMonthAgo),
							product.state.eq(ProductState.SOLD_OUT)
					)
					.orderBy(product.updatedAt.desc())
					.limit(1)
					.fetchOne();

			Double averagePricePer100MB = queryFactory
					.select(mobileData.pricePer100MB.avg())
					.from(product)
					.join(mobileData).on(product.itemId.eq(mobileData.id))
					.where(
							product.itemType.eq(itemType),
							product.updatedAt.after(oneMonthAgo),
							product.state.eq(ProductState.SOLD_OUT)
					)
					.fetchOne();

			return FindMarketPriceResponse.of(
					recentPricePer100MB != null ? recentPricePer100MB : 0,
					averagePricePer100MB != null ? averagePricePer100MB.intValue() : 0
			);
		}

		// 와이파이 상품
		Integer recentPrice = queryFactory
				.select(trade.tradingPrice.multiply(10).divide(trade.timeAmount)) // 10분당 가격
				.from(trade)
				.where(
						trade.tradeType.eq(TradeType.SALE_WIFI),
						trade.createdAt.after(oneMonthAgo)
				)
				.orderBy(trade.createdAt.desc())
				.limit(1)
				.fetchOne();

		Double averagePrice = queryFactory
				.select(
						trade.tradingPrice.multiply(10).divide(trade.timeAmount).avg()
				)
				.from(trade)
				.where(
						trade.tradeType.eq(TradeType.SALE_WIFI),
						trade.createdAt.after(oneMonthAgo)
				)
				.fetchOne();

		return FindMarketPriceResponse.of(
				recentPrice != null ? recentPrice : 0,
				averagePrice != null ? averagePrice.intValue() : 0
		);
	}

	public Long countSellingProduct(ReadSellingProductRequest request) {

		return queryFactory
				.select(product.count())
				.from(product)
				.leftJoin(mobileData).on(
						product.itemId.eq(mobileData.id)
								.and(product.itemType.eq(ItemType.MOBILE_DATA))
				)
				.leftJoin(wifi).on(
						product.itemId.eq(wifi.id)
								.and(product.itemType.eq(ItemType.WIFI))
				)
				.where(
						product.member.id.eq(request.memberId()),
						request.productState() != null ? product.state.eq(request.productState())
								: null
				)
				.fetchOne();
	}
}
