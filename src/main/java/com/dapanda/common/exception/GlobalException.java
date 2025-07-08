package com.dapanda.common.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public class GlobalException extends RuntimeException {

    private final BaseException baseException;

    public HttpStatus getStatus(){

        return baseException.getStatus();
    }

    public int getCode(){

        return baseException.getCode();
    }

    public String getMessage(){

        return baseException.getMessage();
    }
}
