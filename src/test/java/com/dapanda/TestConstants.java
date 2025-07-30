package com.dapanda;

import com.dapanda.report.entity.ReportTargetCategory;
import com.dapanda.review.entity.ReviewSortOption;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class TestConstants {

	public static final class Member {

		public static final Long USER_DETAILS_MEMBER_ID = 1L;
		public static final Long BUYER_MEMBER_ID = 2L;
		public static final Long SELLER_MEMBER_ID = 3L;
		public static final Long MEMBER_ID = 1L;
		public static final Long OTHER_MEMBER_ID = 2L;
		public static final int CASH_0 = 0;
		public static final int CASH_3000 = 3000;
		public static final int CASH_5000 = 5000;
		public static final BigDecimal BUYING_DATA = BigDecimal.valueOf(1.0);
		public static final BigDecimal SELLING_DATA = BigDecimal.valueOf(1.0);
		public static final String PROFILE_IMAGE_URL = "imageUrl.jpg";
	}

	public static final class Pagination {

		public static final Long DEFAULT_CURSOR_ID = null;
		public static final int DEFAULT_SIZE_2 = 2;
		public static final String DEFAULT_REVIEW_SORT_OPTION = ReviewSortOption.RECENT.name();
		public static final int CHAT_MESSAGE_HISTORY_DEFAULT_SIZE = 20;
	}

	public static final class Review {

		public static final Long REVIEW_ID = 1L;
		public static final float RATING = 5.0f;
		public static final String COMMENT = "좋아요";
		public static final float NEW_RATING = 1.0f;
		public static final String NEW_COMMENT = "별로에요";
		public static final float AVERAGE_RATE = 3.5F;
		public static final int REVIEW_COUNT = 3;
	}

	public static final class Report {

		public static final Long REPORT_ID = 1L;
		public static final Long TARGET_ID = 1L;
		public static final String REASON = "너무 비싸요";
		public static final ReportTargetCategory REPORT_TARGET_CATEGORY_PRODUCT = ReportTargetCategory.PRODUCT;
	}

	public static final class Trade {

		public static final Long TRADE_ID_1 = 1L;
		public static final Long TRADE_ID_2 = 2L;
	}

	public static final class Product {

		public static final Long PRODUCT_ID = 1L;
		public static final int NEW_PRICE_9000 = 9000;
		public static final int PRICE_500 = 500;
		public static final int PRICE_1500 = 1500;
		public static final int PRICE_3000 = 3000;
		public static final Long INVALID_PRODUCT_ID = 100L;
		public static final LocalDateTime UPDATED_AT = LocalDateTime.of(2025, 3, 3, 21, 0);
	}

	public static final class MobileData {

		public static final Long MOBILE_DATA_ID = 1L;
		public static final BigDecimal BEFORE_DATA_AMOUNT = BigDecimal.valueOf(1.0);
		public static final BigDecimal BEFORE_REMAIN_AMOUNT = BigDecimal.valueOf(1.0);
		public static final BigDecimal CHANGED_AMOUNT = BigDecimal.valueOf(1.0);
		public static final BigDecimal EXCEED_CHANGED_AMOUNT = BigDecimal.valueOf(3.0);
		public static final BigDecimal SELLING_DATA = BigDecimal.valueOf(1.5);
		public static final boolean SPLIT_TYPE = true;
		public static final BigDecimal DATA_AMOUNT_1 = BigDecimal.valueOf(1.0);
		public static final BigDecimal DATA_AMOUNT_2 = BigDecimal.valueOf(2.0);
		public static final BigDecimal REMAIN_AMOUNT_1 = BigDecimal.valueOf(1.0);
		public static final BigDecimal REMAIN_AMOUNT_2 = BigDecimal.valueOf(2.0);
		public static final int PRICE_PER_100MB_300 = 300;
		public static final int PRICE_PER_100MB_150 = 150;
	}

	public static final class Wifi {

		public static final Long WIFI_ID = 1L;
		public static final String TITLE = "와이파이 팔아요";
		public static final String CHANGED_TITLE = "와이파이 팝니당";
		public static final String CONTENT = "서울시 강남구 할리스입니다";
		public static final String CHANGED_CONTENT = "서울시 강남구 할리스입니다람쥐";
		public static final double LATITUDE = 30F;
		public static final double CHANGED_LATITUDE = 35F;
		public static final double LONGITUDE = 126F;
		public static final double CHANGED_LONGITUDE = 150;
		public static final String ADDRESS = "서울특별시 강남구";
		public static final String IMAGE_URL_1 = "image1";
		public static final String IMAGE_URL_2 = "image2";
		public static final LocalDateTime START_DATETIME = LocalDateTime.of(2025, 3, 4, 10, 0);
		public static final LocalDateTime WRONG_START_DATETIME = LocalDateTime.of(2025, 3, 4, 10,
				0);
		public static final LocalDateTime END_DATETIME = LocalDateTime.of(2025, 3, 4, 21, 0);
		public static final LocalDateTime WRONG_END_DATETIME = LocalDateTime.of(2024, 3, 4, 21, 0);
		public static final LocalTime START_TIME = LocalTime.of(10, 0);
		public static final LocalTime END_TIME = LocalTime.of(22, 0);
	}

	public static final class Plan {

		public static final BigDecimal PROVIDING_DATA_AMOUNT_10 = BigDecimal.valueOf(10);

	}

	public static final class Chat {

		public static final Long CHAT_ROOM_ID = 1L;
		public static final Long CHAT_PARTICIPANT_ID_1 = 1L;
		public static final Long CHAT_PARTICIPANT_ID_2 = 2L;
		public static final String CHAT_MESSAGE = "안녕하세요~~";
		public static final Long CHAT_MESSAGE_ID = 1L;
	}

	public static final class Payment {

		public static final Long PAYMENT_ID = 1L;
		public static final String REQUEST_ID = "REQUEST_ID_1";
		public static final String DUPLICATE_REQUEST_ID = "refund:REQUEST_ID_1";
		public static final String INVALID_REQUEST_ID = " ";
		public static final int CHARGE_AMOUNT_3000 = 3000;
		public static final int REFUND_AMOUNT_3000 = 3000;
		public static final String APPROVED_AT = "2025-07-25T13:44:09+09:00";
		public static final int TOTAL_AMOUNT_3000 = 3000;
	}
}
