package com.dapanda.common.exception;

import org.springframework.http.HttpStatus;

public interface BaseException {

    HttpStatus getStatus();
    int getCode();
    String getMessage();
}
