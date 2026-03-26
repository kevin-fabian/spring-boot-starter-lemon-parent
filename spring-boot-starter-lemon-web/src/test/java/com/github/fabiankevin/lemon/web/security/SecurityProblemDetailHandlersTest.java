package com.github.fabiankevin.lemon.web.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityProblemDetailHandlersTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void invalidTokenEntryPoint_writesProblemDetailResponseAndSanitizedHeader() throws Exception {
        DefaultInvalidTokenAuthenticationEntryPoint entryPoint = new DefaultInvalidTokenAuthenticationEntryPoint();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(new MockHttpServletRequest(), response, new AuthenticationException("token \"expired\"") { });

        JsonNode body = jsonMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(response.getHeader(HttpHeaders.WWW_AUTHENTICATE))
                .isEqualTo("Bearer error=\"invalid_token\", error_description=\"token 'expired'\"");
        assertThat(body.path("title").asText()).isEqualTo("Unauthorized");
        assertThat(body.path("detail").asText()).isEqualTo("Invalid or expired token");
        assertThat(body.path("status").asInt()).isEqualTo(401);
    }

    @Test
    void bearerAccessDeniedHandler_writesProblemDetailResponse() throws Exception {
        DefaultBearerAccessDeniedHandler handler = new DefaultBearerAccessDeniedHandler();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response, new AccessDeniedException("forbidden"));

        JsonNode body = jsonMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(response.getHeader(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer error=\"insufficient_scope\"");
        assertThat(body.path("title").asText()).isEqualTo("Forbidden");
        assertThat(body.path("detail").asText()).isEqualTo("Insufficient scope");
        assertThat(body.path("status").asInt()).isEqualTo(403);
    }
}

