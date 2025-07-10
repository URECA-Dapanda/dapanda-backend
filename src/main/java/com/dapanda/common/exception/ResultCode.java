package com.dapanda.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResultCode {

    SUCCESS(HttpStatus.OK, 0, "정상 처리 되었습니다."),

    // 글로벌 1000번대
    INVALID_INPUT(HttpStatus.BAD_REQUEST, 1000, "잘못된 입력값입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 1001, "유효하지 않은 토큰입니다."),

    // 회원 2000번대
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, 2005, "존재하지 않는 사용자입니다."),

    // 상품 3000번대

    // 거래 4000번대

    // 결제 5000번대
    FAIL_PAYMENT_PROCESSING(HttpStatus.INTERNAL_SERVER_ERROR, 5000, "내부 오류로 결제 처리에 실패했습니다."),
    FAIL_PAYMENT_APPROVAL(HttpStatus.BAD_REQUEST, 5001, "결제 승인에 실패했습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, 5002, "결제 금액이 유효하지 않습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, 5003, "결제 요청 금액과 일치하지 않습니다."),

    // 리뷰 6000번대

    // 신고 7000번대

    // 채팅 8000번대

    // 관리자 9000번대

    // 이벤트 10000번대

    // 알림 11000번대

    // 요금제 추천 12000번대

    ;

    private final HttpStatus status;
    private final int code;
    private final String message;
}
