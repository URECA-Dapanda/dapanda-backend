package com.dapanda;

import com.dapanda.review.entity.ReviewSortOption;
import java.time.LocalDateTime;

public final class TestConstants {

	public static final class Member {

		public static final Long USER_DETAILS_MEMBER_ID = 1L;
		public static final Long BUYER_MEMBER_ID = 2L;
		public static final Long SELLER_MEMBER_ID = 3L;
	}

	public static final class Pagination {

		public static final Long DEFAULT_CURSOR_ID = null;
		public static final int DEFAULT_SIZE = 2;
		public static final String DEFAULT_REVIEW_SORT_OPTION = ReviewSortOption.RECENT.name();
	}

	public static final class Review {

		public static final Long REVIEW_ID = 1L;
		public static final float RATING = 5.0f;
		public static final String COMMENT = "좋아요";
		public static final float NEW_RATING = 1.0f;
		public static final String NEW_COMMENT = "별로에요";
	}

	public static final class Trade {

		public static final Long TRADE_ID = 1L;
	}

	public static final class Product {

		public static final Long MEMBER_ID = 1L;
		public static final Long OTHER_MEMBER_ID = 2L;
		public static final Long PRODUCT_ID = 1L;
		public static final int NEW_PRICE = 9000;
		public static final float BEFORE_DATA_AMOUNT = 1.0F;
		public static final float BEFORE_REMAIN_AMOUNT = 1.0F;
		public static final float CHANGED_AMOUNT = 1.0F;
		public static final float EXCEED_CHANGED_AMOUNT = 3.0F;
		public static final float SELLING_DATA = 1.5F;
		public static final boolean SPLIT_TYPE = true;
		public static final float DATA_AMOUNT = 2.0F;
		public static final float REMAIN_AMOUNT = 1.0F;
		public static final int PRICE_PER_100MB = 300;
		public static final int PRICE = 3000;
		public static final String TITLE = "와이파이 팔아요";
		public static final String CHANGED_TITLE = "와이파이 팝니당";
		public static final String CONTENT = "서울시 강남구 할리스입니다";
		public static final String CHANGED_CONTENT = "서울시 강남구 할리스입니다람쥐";
		public static final double LATITUDE = 30F;
		public static final double CHANGED_LATITUDE = 35F;
		public static final double LONGITUDE = 126F;
		public static final double CHANGED_LONGITUDE = 150;
		public static final double AVERAGE_RATE = 3.5;
		public static final int REVIEW_COUNT = 3;
		public static final Long INVALID_PRODUCT_ID = 100L;
		public static final String IMAGE_URL_1 = "image1";
		public static final String IMAGE_URL_2 = "image2";
		public static final LocalDateTime START_TIME = LocalDateTime.of(2025, 3, 4, 10, 0);
		public static final LocalDateTime WRONG_START_TIME = LocalDateTime.of(2025, 3, 4, 10, 0);
		public static final LocalDateTime END_TIME = LocalDateTime.of(2025, 3, 4, 21, 0);
		public static final LocalDateTime WRONG_END_TIME = LocalDateTime.of(2024, 3, 4, 21, 0);
		public static final LocalDateTime UPDATED_AT = LocalDateTime.of(2025, 3, 3, 21, 0);
	}
}
