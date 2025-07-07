package com.dapanda.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class CommonResponse<T> {

    private final int code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private T data;

    public CommonResponse(int code, String message) {

        this.code = code;
        this.message = message;
    }

    protected CommonResponse(int code, String message, T data) {

        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponse<T> success(T data) {

        return new CommonResponse<>(200, "정상 처리 되었습니다.", data);
    }
}
