package com.dapanda.product.repository;

import static com.dapanda.review.entity.QReview.review;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.entity.ProductSortOption;
import com.dapanda.product.entity.QMobileData;
import com.dapanda.product.entity.QProduct;
import com.dapanda.product.entity.QProductImage;
import com.dapanda.product.entity.QWifi;
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

		QProduct product = QProduct.product;
		QMobileData mobileData = QMobileData.mobileData;

		List<MobileDataSummary> content = queryFactory
				.select(Projections.constructor(MobileDataSummary.class,
						product.id,
						product.price,
						product.itemId,
						product.member.name,
						mobileData.remainAmount,
						mobileData.pricePer100MB,
						mobileData.isSplitType
				))
				.from(product)
				.join(mobileData).on(mobileData.id.eq(product.itemId))
				.where(
						gtCursorId(cursorId, product),
						eqDataAmount(dataAmount, mobileData)
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

		QProduct product = QProduct.product;
		QWifi wifi = QWifi.wifi;
		QProductImage productImage = QProductImage.productImage;
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
						distance.divide(METER_TO_KILOMETER)
				))
				.from(product)
				.groupBy(product.id)
				.join(wifi).on(wifi.id.eq(product.itemId))
				.leftJoin(review).on(review.productId.eq(product.id))
				.leftJoin(productImage).on(
						productImage.wifiId.eq(wifi.id)
								.and(productImage.priority.eq(
										JPAExpressions
												.select(productImageSub.priority.min())
												.from(productImageSub)
												.where(productImageSub.wifiId.eq(wifi.id))
								))
				)
				.where(
						gtCursorId(cursorId, product),
						isOpenNow(isOpen, wifi, now)
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

	private BooleanExpression gtCursorId(Long cursorId, QProduct product) {

		return cursorId != null ? product.id.gt(cursorId) : null;
	}

	private BooleanExpression eqDataAmount(Float dataAmount, QMobileData mobileData) {

		return dataAmount != null ? mobileData.remainAmount.eq(dataAmount) : null;
	}

	private BooleanExpression isOpenNow(boolean isOpen, QWifi wifi, LocalDateTime now) {

		return isOpen ? wifi.startTime.loe(now).and(wifi.endTime.goe(now)) : null;
	}
}
