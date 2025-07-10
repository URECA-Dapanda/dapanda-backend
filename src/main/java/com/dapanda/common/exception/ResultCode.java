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
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, 1001, "필수 입력값이 누락되었습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 1002, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, 1003, "권한이 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, 1004, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1005, "서버 내부 오류가 발생했습니다."),

    // 회원 2000번대
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, 2000, "이미 가입된 이메일입니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, 2001, "이미 사용 중인 사용자명입니다."),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, 2002, "비밀번호가 보안 기준에 맞지 않습니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, 2003, "이메일 형식이 올바르지 않습니다."),
    INVALID_MEMBER_NAME_FORMAT(HttpStatus.BAD_REQUEST, 2004, "아이디 형식이 올바르지 않습니다."),

    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, 2005, "존재하지 않는 사용자입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, 2006, "비밀번호가 일치하지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, 2007, "잠긴 계정입니다. 관리자에게 문의하세요."),

    NOT_LOGGED_IN(HttpStatus.UNAUTHORIZED, 2008, "로그인 상태가 아닙니다."),
    ALREADY_LOGGED_OUT(HttpStatus.BAD_REQUEST, 2009, "이미 로그아웃된 상태입니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 2010, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, 2011, "토큰이 만료되었습니다."),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, 2012, "토큰이 제공되지 않았습니다."),
    TOKEN_REISSUE_FAILED(HttpStatus.UNAUTHORIZED, 2013, "토큰 재발급에 실패했습니다."),

    // 리뷰 6000번대
    SELF_REVIEW(HttpStatus.BAD_REQUEST, 6000, "자신에게 리뷰를 작성할 수 없습니다.")
    ;

    private final HttpStatus status;
    private final int code;
    private final String message;
}
