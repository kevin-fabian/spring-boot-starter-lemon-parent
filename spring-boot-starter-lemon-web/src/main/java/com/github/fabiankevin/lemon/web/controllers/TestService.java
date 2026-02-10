package com.github.fabiankevin.lemon.web.controllers;

import com.github.fabiankevin.lemon.web.controllers.dtos.SimpleDto;

public interface TestService {
    void api();
    void accessDenied();
    void json(SimpleDto dto);
    void requireParam(int id);
    String methodOnly();
    void upload();
    void put();
    void patch();
    void delete();
}

