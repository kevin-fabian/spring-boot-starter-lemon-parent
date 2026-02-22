package com.github.fabiankevin.lemon.web.controllers;

import com.github.fabiankevin.lemon.web.GlobalExceptionHandler;
import com.github.fabiankevin.lemon.web.exceptions.ApiException;
import com.github.fabiankevin.lemon.web.exceptions.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestExceptionController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("autoconfig-test")
class TestExceptionControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TestService service;

    @Test
    void apiExceptionEndpoint_returnsBadRequestWithErrorBody() throws Exception {
        doThrow(new ApiException("Something went wrong", 400)).when(service).api();

        mockMvc.perform(get("/test/api-ex"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Request failed"))
                .andExpect(jsonPath("$.details").value("Something went wrong"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void accessDeniedEndpoint_returnsForbiddenResponse() throws Exception {
        doThrow(new AccessDeniedException("Access is denied")).when(service).accessDenied();

        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access denied"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void jsonEndpoint_withMalformedJson_returnsInvalidRequestFormat() throws Exception {
        String badJson = "{ name: 'missing-quotes' }";

        mockMvc.perform(post("/test/json").contentType(MediaType.APPLICATION_JSON).content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void missingParamEndpoint_returnsBadRequestWithMissingParameterMessage() throws Exception {
        mockMvc.perform(get("/test/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Missing parameter"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void methodNotAllowed_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/test/method-only"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Method not allowed"))
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void uploadEndpoint_returnsPayloadTooLarge() throws Exception {
        doThrow(new org.springframework.web.multipart.MaxUploadSizeExceededException(1024)).when(service).upload();

        mockMvc.perform(post("/test/upload"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.title").value("Content too large"))
                .andExpect(jsonPath("$.status").value(413));
    }

    @Test
    void putEndpoint_returnsMethodNotAllowedHandledOrDelegated() throws Exception {
        doThrow(new ApiException("Put failed", 400)).when(service).put();

        mockMvc.perform(put("/test/put"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Request failed"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void patchEndpoint_returnsMethodNotAllowedHandledOrDelegated() throws Exception {
        doThrow(new ApiException("Patch failed", 400)).when(service).patch();

        mockMvc.perform(patch("/test/patch"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Request failed"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteEndpoint_returnsMethodNotAllowedHandledOrDelegated() throws Exception {
        doThrow(new ApiException("Delete failed", 400)).when(service).delete();

        mockMvc.perform(delete("/test/delete"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Request failed"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void businessRuleExceptionEndpoint_returnsDomainErrorWithCode() throws Exception {
        doThrow(new BusinessRuleException("Business rule violated", 400, "BUS-001")).when(service).businessRule();

        mockMvc.perform(get("/test/business-rule"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Domain error"))
                .andExpect(jsonPath("$.details").value("Business rule violated"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("BUS-001"));
    }
}
