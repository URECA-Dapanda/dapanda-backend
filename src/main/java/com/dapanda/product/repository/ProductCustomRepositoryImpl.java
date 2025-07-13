package com.dapanda.product.repository;

import static com.dapanda.review.entity.QReview.review;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.entity.ProductSortOption;
import com.dapanda.product.entity.QMobileData;
import com.dapanda.product.entity.QProduct;
import com.dapanda.product.entity.QWifi;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductCustomRepositoryImpl implements ProductCustomRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public CursorPageResponse<MobileDataSummary> findMobileDataByCursor(Long cursorId, int size,
			ProductSortOption productSortOption, Integer dataAmount) {

		QProduct product = QProduct.product;
		QMobileData mobileData = QMobileData.mobileData;

		List<MobileDataSummary> content = queryFactory
				.select(Projections.constructor(MobileDataSummary.class,
						product.id,
						product.price,
						product.itemId,
						product.member.name,
						mobileData.remainAmount,
						mobileData.unit,
						mobileData.pricePer100MB,
						mobileData.isSplitType
				))
				.from(product)
				.join(mobileData).on(mobileData.id.eq(product.itemId))
				.where(
						cursorId != null ? product.id.lt(cursorId) : null,
						dataAmount != null ? mobileData.remainAmount.goe(dataAmount) : null
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

		LocalDateTime now = LocalDateTime.now();

		// 거리 계산
		NumberExpression<Double> distance = Expressions.numberTemplate(Double.class,
				"ST_DISTANCE_SPHERE(POINT({0}, {1}), POINT({2}, {3}))",
				longitude, latitude, wifi.longitude, wifi.latitude);

		List<WifiSummary> content = queryFactory
				.select(Projections.constructor(WifiSummary.class,
						product.id,
						product.price,
						product.itemId,
						product.member.name,
						wifi.title,
						wifi.latitude,
						wifi.longitude,
						wifi.imageUrl,
						review.rating.avg().coalesce(0.0),
						distance.divide(1000.0)
				))
				.from(product)
				.groupBy(product.id)
				.join(wifi).on(wifi.id.eq(product.itemId))
				.leftJoin(review).on(review.productId.eq(product.id))
				.where(
						cursorId != null ? product.id.gt(cursorId) : null,
						isOpen ? wifi.startTime.loe(now).and(wifi.endTime.goe(now)) : null
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
}
