package com.dapanda.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CommonResponse<T> {

    private final int code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private T data;

    public CommonResponse(ResultCode resultcode) {

        this.code = resultcode.getCode();
        this.message = resultcode.getMessage();
    }

    public CommonResponse(String validationMessage){
        this.code = HttpStatus.BAD_REQUEST.value();
        this.message = validationMessage;
    }

    protected CommonResponse(ResultCode resultcode, T data) {

        this.code = resultcode.getCode();
        this.message = resultcode.getMessage();
        this.data = data;
    }

    public static <T> CommonResponse<T> success(T data) {

        return new CommonResponse<>(ResultCode.SUCCESS, data);
    }
}
