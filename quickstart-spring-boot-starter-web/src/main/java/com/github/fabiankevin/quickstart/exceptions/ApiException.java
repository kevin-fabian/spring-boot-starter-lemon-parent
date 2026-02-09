package com.github.fabiankevin.quickstart.exceptions;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private static final int DEFAULT_HTTP_STATUS_CODE = 500;
    private final int httpStatusCode;

    public ApiException(String message) {
        super(message);
        this.httpStatusCode = DEFAULT_HTTP_STATUS_CODE;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = DEFAULT_HTTP_STATUS_CODE;
    }

    public ApiException(String message, Throwable cause, int httpStatusCode) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }

    public ApiException(String message, int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }
}
