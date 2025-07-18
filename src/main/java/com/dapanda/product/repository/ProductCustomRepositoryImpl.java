package com.dapanda.product.repository;

import static com.dapanda.product.entity.QMobileData.mobileData;
import static com.dapanda.product.entity.QProduct.product;
import static com.dapanda.product.entity.QProductImage.productImage;
import static com.dapanda.product.entity.QWifi.wifi;
import static com.dapanda.review.entity.QReview.review;
import static com.dapanda.trade.entity.QTradeDetails.tradeDetails;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.response.MobileDataInfoResponse;
import com.dapanda.product.dto.response.WifiInfoResponse;
import com.dapanda.product.entity.ProductSortOption;
import com.dapanda.product.entity.ProductState;
import com.dapanda.product.entity.QProductImage;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
			ProductSortOption productSortOption, Float dataAmount) {

		List<MobileDataSummary> content = queryFactory
				.select(Projections.constructor(MobileDataSummary.class,
						product.id,
						product.price,
						product.itemId,
						product.member.name,
						mobileData.remainAmount,
						mobileData.pricePer100MB,
						mobileData.isSplitType,
						product.updatedAt
				))
				.from(product)
				.join(mobileData).on(mobileData.id.eq(product.itemId))
				.where(isActiveProduct(),
						gtCursorId(cursorId),
						eqDataAmount(dataAmount)
				)
				.orderBy(
						productSortOption == ProductSortOption.PRICE_ASC ? product.price.asc() :
								productSortOption == ProductSortOption.AMOUNT_ASC
										? mobileData.remainAmount.asc() :
										productSortOption == ProductSortOption.AMOUNT_DESC
												? mobileData.remainAmount.desc() :
												product.updatedAt.desc(), // default: RECENT
						product.id.asc()
				)
				.limit(size + 1)
				.fetch();

		// TODO: 메서드로 뺴기
		boolean hasNext = content.size() > size;
		if (hasNext) {
			content.remove(size);
		}
		Long nextCursorId = hasNext ? content.get(content.size() - 1).getId() : null;

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
				DISTANCE_TEMPLATE,
				longitude, latitude, wifi.longitude, wifi.latitude);

		List<WifiSummary> content = queryFactory
				.select(Projections.constructor(WifiSummary.class,
						product.id,
						product.price,
						product.itemId,
						product.member.name,
						wifi.title,
						Expressions.stringTemplate("MIN({0})", productImage.imageUrl),
						wifi.latitude,
						wifi.longitude,
						review.rating.avg().coalesce(DEFAULT_RATING),
						distance.divide(METER_TO_KILOMETER),
						product.updatedAt
				))
				.from(product)
				.groupBy(product.id)
				.join(wifi).on(wifi.id.eq(product.itemId))
				.leftJoin(review).on(review.trade.id.eq(
						JPAExpressions
								.select(tradeDetails.trade.id)
								.from(tradeDetails)
								.where(tradeDetails.product.id.eq(product.id))
				))
				.where(
						gtCursorId(cursorId),
						isOpenNow(isOpen, now)
				)
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
						gtCursorId(cursorId),
						isOpenNow(isOpen, now)
				)
				.orderBy(
						productSortOption == ProductSortOption.PRICE_ASC ? product.price.asc() :
								productSortOption == ProductSortOption.AVERAGE_RATE_DESC
										? review.rating.avg().desc() :
										distance.asc(),
						product.id.asc()
				)
				.limit(size + 1)
				.fetch();

		boolean hasNext = content.size() > size;
		if (hasNext) {
			content.remove(size);
		}
		Long nextCursorId = hasNext ? content.get(content.size() - 1).getId() : null;

		return CursorPageResponse.of(content,
				CursorPageResponse.PageInfo.of(nextCursorId, hasNext, content.size()));
	}

	@Override
	public MobileDataInfoResponse findMobileDataInfo(Long productId) {

		return queryFactory
				.select(Projections.constructor(MobileDataInfoResponse.class,
						product.id,
						mobileData.id,
						product.price,
						product.member.id,
						product.member.name,
						mobileData.remainAmount,
						mobileData.pricePer100MB,
						review.rating.avg().coalesce(DEFAULT_RATING),
						review.rating.count().intValue(),
						product.updatedAt
				))
				.from(product)
				.join(mobileData).on(product.itemId.eq(mobileData.id))
				.leftJoin(review).on(review.trade.id.eq(
						JPAExpressions
								.select(tradeDetails.trade.id)
								.from(tradeDetails)
								.where(tradeDetails.product.id.eq(product.id))
				))
				.where(isActiveProduct(),
						product.itemId.eq(mobileData.id),
						product.id.eq(productId)
				)
				.groupBy(product.id, mobileData.id, product.price, product.member,
						mobileData.remainAmount, mobileData.pricePer100MB, product.updatedAt)
				.fetchOne();
	}

	@Override
	public WifiInfoResponse findWifiInfo(Long productId) {

		return queryFactory
				.select(Projections.constructor(WifiInfoResponse.class,
						product.id,
						wifi.id,
						product.price,
						product.member.id,
						product.member.name,
						wifi.title,
						wifi.content,
						wifi.latitude,
						wifi.longitude,
						review.rating.avg().coalesce(DEFAULT_RATING),
						review.rating.count().intValue(),
						Expressions.nullExpression(List.class),
						wifi.startTime,
						wifi.endTime,
						product.updatedAt
				))
				.from(product)
				.join(wifi).on(product.itemId.eq(wifi.id))
				.leftJoin(review).on(review.trade.id.eq(
						JPAExpressions
								.select(tradeDetails.trade.id)
								.from(tradeDetails)
								.where(tradeDetails.product.id.eq(product.id))
				))
				.where(isActiveProduct(),
						product.itemId.eq(wifi.id),
						product.id.eq(productId)
				)
				.groupBy(product.id, wifi.id, product.price, product.member, wifi.title,
						wifi.content, wifi.latitude, wifi.longitude, wifi.startTime, wifi.endTime,
						product.updatedAt)
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

	private BooleanExpression isActiveProduct() {

		return product.state.eq(ProductState.ACTIVE);
	}

	private BooleanExpression gtCursorId(Long cursorId) {

		return cursorId != null ? product.id.gt(cursorId) : null;
	}

	private BooleanExpression eqDataAmount(Float dataAmount) {

		return dataAmount != null ? mobileData.remainAmount.eq(dataAmount) : null;
	}

	private BooleanExpression isOpenNow(boolean isOpen, LocalDateTime now) {

		return isOpen ? wifi.startTime.loe(now).and(wifi.endTime.goe(now)) : null;
	}
}
