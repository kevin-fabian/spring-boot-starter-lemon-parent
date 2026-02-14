package com.github.fabiankevin.lemon.web.exceptions;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(message, 404);
    }
}
