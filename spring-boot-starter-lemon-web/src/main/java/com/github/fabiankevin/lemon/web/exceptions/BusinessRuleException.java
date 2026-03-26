package com.github.fabiankevin.lemon.web.exceptions;

import lombok.Getter;

@Getter
public class BusinessRuleException extends ApiException {
    private String code;
    private String title;

    public BusinessRuleException(String message, int httpCode, String title, String code) {
        super(message, httpCode);
        this.title = title;
        this.code = code;
    }
}
