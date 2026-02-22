package com.github.fabiankevin.lemon.web.exceptions;

import lombok.Getter;

@Getter
public class BusinessRuleException extends ApiException {
    private String code;

    public BusinessRuleException(String message, int httpCode, String code) {
        super(message, httpCode);
        this.code = code;
    }
}
