package com.github.fabiankevin.lemon.web;

import com.github.fabiankevin.lemon.web.security.DefaultBearerAccessDeniedHandler;
import com.github.fabiankevin.lemon.web.security.DefaultInvalidTokenAuthenticationEntryPoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

@AutoConfiguration
@PropertySource("classpath:application-simple-default.properties")
public class LemonAutoConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public AuthenticationEntryPoint defaultInvalidTokenAuthenticationEntryPoint() {
        return new DefaultInvalidTokenAuthenticationEntryPoint();
    }

    @Bean
    public AccessDeniedHandler defaultBearerAccessDeniedHandler(){
        return new DefaultBearerAccessDeniedHandler();
    }
}
