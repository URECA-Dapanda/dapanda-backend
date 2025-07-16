package com.dapanda;

import com.dapanda.review.entity.ReviewSortOption;

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

		public static final Long PRODUCT_ID = 1L;
	}
}
