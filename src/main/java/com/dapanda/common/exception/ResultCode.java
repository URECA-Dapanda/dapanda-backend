package com.dapanda.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResultCode {

	SUCCESS(HttpStatus.OK, 0, "정상 처리 되었습니다."),

	// 글로벌 1000번대
	FORBIDDEN(HttpStatus.FORBIDDEN, 1003, "권한이 없습니다."),
	INVALID_PARAMETER(HttpStatus.BAD_REQUEST, 1006, "유효하지 않은 파라미터입니다."),

	// 회원 2000번대
	MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, 2005, "존재하지 않는 사용자입니다."),
	REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, 2014, "유효한 리프레쉬 토큰을 찾을 수 없습니다."),

	// 상품 3000번대
	INVALID_PRODUCT_SORT_OPTION(HttpStatus.BAD_REQUEST, 3000, "유효하지 않은 상품 정렬 조건입니다"),
	INVALID_ITEM_TYPE(HttpStatus.BAD_REQUEST, 3001, "유효하지 않은 상품 타입입니다."),
	PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST, 3002, "존재하지 않는 상품입니다."),
	INVALID_PRODUCT(HttpStatus.BAD_REQUEST, 3003, "유효하지 않은 상품입니다."),
	OTHER_PRODUCT(HttpStatus.BAD_REQUEST, 3004, "자신이 등록한 상품이 아닙니다."),
	EXCEEDED_TRANSFER_LIMIT(HttpStatus.BAD_REQUEST, 3005, "전송 가능한 데이터양을 초과했습니다."),
	INVALID_DATA_TRANSFER_AMOUNT(HttpStatus.BAD_REQUEST, 3006, "유효하지 않은 데이터 전송양입니다."),
	INVALID_TIME(HttpStatus.BAD_REQUEST, 3007, "시작 시간은 종료 시간보다 늦을 수 없습니다."),
	ALREADY_DELETED_PRODUCT(HttpStatus.BAD_REQUEST, 3008, "이미 삭제된 상품은 삭제할 수 없습니다."),
	MOBILE_DATA_NOT_FOUND(HttpStatus.BAD_REQUEST, 3009, "존재하지 않는 데이터 상품입니다."),
	WIFI_NOT_FOUND(HttpStatus.BAD_REQUEST, 3010, "존재하지 않는 와이파이 상품입니다."),
	NOT_FOUND_PLAN(HttpStatus.BAD_REQUEST, 3011, "회원의 요금제 정보가 존재하지 않습니다."),
	INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, 3013, "허용되지 않은 이미지 형식입니다."),
	NOT_ENOUGH_DATA(HttpStatus.BAD_REQUEST, 3014, "현재 보유한 데이터 양보다 많은 양을 판매할 수 없습니다."),
	PRODUCT_CANNOT_TRADE(HttpStatus.BAD_REQUEST, 3015, "다른 회원이 이미 구매한 분할 판매 상품은 변경할 수 없습니다."),

	// 거래 4000번대
	TRADE_NOT_FOUND(HttpStatus.BAD_REQUEST, 4000, "거래 이력을 찾을 수 없습니다."),
	OTHER_TRADE(HttpStatus.BAD_REQUEST, 4001, "다른 회원의 거래 이력입니다."),
	CANNOT_PURCHASE_OWN_PRODUCT(HttpStatus.BAD_REQUEST, 4002, "자신이 등록한 상품은 구매할 수 없습니다"),
	ALREADY_SOLD_OUT(HttpStatus.BAD_REQUEST, 4003, "이미 판매 완료된 상품입니다."),
	INSUFFICIENT_CASH(HttpStatus.BAD_REQUEST, 4004, "보유 캐시가 부족합니다"),
	INVALID_REMAIN_DATA_AMOUNT(HttpStatus.BAD_REQUEST, 4005, "남은 데이터양이 유효하지 않습니다."),
	INVALID_WIFI_OPERATION_TIME(HttpStatus.BAD_REQUEST, 4006, "와이파이 상품의 운영시간이 아닙니다."),
	EXCEEDED_PURCHASE_LIMIT(HttpStatus.BAD_REQUEST, 4007, "구매 가능한 데이터양을 초과했습니다."),

	// 결제 5000번대
	FAIL_PAYMENT_APPROVAL(HttpStatus.BAD_REQUEST, 5001, "결제 승인에 실패했습니다."),
	INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, 5002, "결제 금액이 유효하지 않습니다."),
	PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, 5003, "결제 요청 금액과 일치하지 않습니다."),
	INVALID_REQUEST_ID(HttpStatus.BAD_REQUEST, 5003, "유효하지 않은 요청 아이디입니다."),
	DUPLICATE_REQUEST(HttpStatus.BAD_REQUEST, 5005, "이미 처리된 요청입니다."),
	REQUEST_TIMEOUT(HttpStatus.BAD_REQUEST, 5006, "결제 요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요."),
	INVALID_CASH_AMOUNT(HttpStatus.BAD_REQUEST, 5007, "유효하지 않은 캐시 금액입니다."),

	// 리뷰 6000번대
	SELF_REVIEW(HttpStatus.BAD_REQUEST, 6000, "자신에게 리뷰를 작성할 수 없습니다."),
	OTHER_REVIEW(HttpStatus.BAD_REQUEST, 6001, "자신의 리뷰가 아닙니다."),
	REVIEW_NOT_FOUND(HttpStatus.BAD_REQUEST, 6002, "리뷰를 찾을 수 없습니다."),

	// 신고 7000번대
	DUPLICATE_REPORT(HttpStatus.CONFLICT, 7001, "이미 신고되었습니다."),
	SELF_REPORT(HttpStatus.BAD_REQUEST, 7002, "셀프 신고는 할 수 없습니다."),

	// 채팅 8000번대
	CHAT_OWN_PRODUCT(HttpStatus.BAD_REQUEST, 8000, "자기 상품의 채팅방은 생성할 수 없습니다."),
	CHAT_ROOM_NOT_FOUND(HttpStatus.BAD_REQUEST, 8001, "채팅방을 찾을 수 없습니다."),
	CHAT_ROOM_ACCESS_DENIED(HttpStatus.BAD_REQUEST, 8002, "해당 채팅방의 참가자가 아닙니다."),
	CHAT_MESSAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, 8004, "채팅 메시지를 찾을 수 없습니다."),

	// 알림 9000번대
	NOTIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, 9001, "해당 알림이 존재하지 않습니다.");

	private final HttpStatus status;
	private final int code;
	private final String message;
}
