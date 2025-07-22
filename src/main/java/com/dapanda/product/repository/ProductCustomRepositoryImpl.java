package com.dapanda.product.repository;

import static com.dapanda.member.entity.QMember.member;
import static com.dapanda.product.entity.QMobileData.mobileData;
import static com.dapanda.product.entity.QProduct.product;
import static com.dapanda.product.entity.QProductImage.productImage;
import static com.dapanda.product.entity.QWifi.wifi;
import static com.dapanda.review.entity.QReview.review;
import static com.dapanda.trade.entity.QTradeDetails.tradeDetails;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.request.ReadSellingProductRequest;
import com.dapanda.product.dto.response.FindMarketPriceResponse;
import com.dapanda.product.dto.response.MobileDataInfoResponse;
import com.dapanda.product.dto.response.ReadSellingProductResponse;
import com.dapanda.product.dto.response.WifiInfoResponse;
import com.dapanda.product.entity.ItemType;
import com.dapanda.product.entity.ProductSortOption;
import com.dapanda.product.entity.ProductState;
import com.dapanda.product.entity.QProductImage;
import com.dapanda.trade.dto.MobileDataScrap;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.dapanda.product.entity.QMobileData.mobileData;
import static com.dapanda.product.entity.QProduct.product;
import static com.dapanda.product.entity.QProductImage.productImage;
import static com.dapanda.product.entity.QWifi.wifi;
import static com.dapanda.review.entity.QReview.review;
import static com.dapanda.trade.entity.QTradeDetails.tradeDetails;

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
						member.averageRating,
						member.reviewCount,
						mobileData.isSplitType,
						product.updatedAt
				))
				.from(product)
				.join(mobileData).on(product.itemId.eq(mobileData.id))
				.leftJoin(member).on(product.member.id.eq(member.id))
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

	@Override
	public List<ReadSellingProductResponse> findSellingProduct(ReadSellingProductRequest request) {

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
				.where(
						product.member.id.eq(request.memberId()),
						request.productState() != null ? product.state.eq(request.productState()) : null,
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
							tuple.get(product.createdAt),
							tuple.get(product.updatedAt)
					);
				})
				.toList();
	}

	@Override
	public List<MobileDataScrap> findMobileDataScrap(float dataAmount) {

		return queryFactory
				.select(Projections.constructor(MobileDataScrap.class,
						product.id,
						mobileData.id,
						product.member.name,
						product.price,
						Expressions.constant(0),
						mobileData.remainAmount,
						Expressions.constant(0f),
						mobileData.pricePer100MB,
						mobileData.isSplitType,
						product.updatedAt
				))
				.from(product)
				.join(mobileData).on(product.itemId.eq(mobileData.id))
				.where(
						product.state.eq(ProductState.ACTIVE),
						mobileData.remainAmount.gt(0)
				)
				.orderBy(
						mobileData.pricePer100MB.asc(),
						mobileData.remainAmount.desc(),
						mobileData.isSplitType.asc()
				)
				.limit(200) // 필요에 따라 조절
				.fetch();
	}

	@Override
	public Float sumSoldMobileDataAmountByMemberId(Long memberId) {

		Float sum = queryFactory
				.select(mobileData.dataAmount.sum())
				.from(product)
				.join(mobileData).on(product.itemId.eq(mobileData.id))
				.where(
						product.member.id.eq(memberId),
						product.state.eq(ProductState.ACTIVE)
				)
				.fetchOne();

		return sum != null ? sum : 0f;
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
				.select(product.price)
				.from(product)
				.where(
						product.itemType.eq(itemType),
						product.updatedAt.after(oneMonthAgo),
						product.state.eq(ProductState.SOLD_OUT)
				)
				.orderBy(product.updatedAt.desc())
				.limit(1)
				.fetchOne();

		Double averagePrice = queryFactory
				.select(product.price.avg())
				.from(product)
				.where(
						product.itemType.eq(itemType),
						product.updatedAt.after(oneMonthAgo),
						product.state.eq(ProductState.SOLD_OUT)
				)
				.fetchOne();

		return FindMarketPriceResponse.of(
				recentPrice != null ? recentPrice : 0,
				averagePrice != null ? averagePrice.intValue() : 0
		);
	}

}
