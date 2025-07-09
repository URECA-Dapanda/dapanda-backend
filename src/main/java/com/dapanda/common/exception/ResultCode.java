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
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, 2000, "회원을 찾을 수 없습니다."),

    // 리뷰 6000번대
    SELF_REVIEW(HttpStatus.BAD_REQUEST, 6000, "자신에게 리뷰를 작성할 수 없습니다.")
    ;

    private final HttpStatus status;
    private final int code;
    private final String message;
}
