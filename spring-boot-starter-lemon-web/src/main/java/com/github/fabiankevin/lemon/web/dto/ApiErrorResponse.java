package com.github.fabiankevin.lemon.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Error response object")
@Builder
public class ApiErrorResponse {
    @Schema(description = "Main error message", example = "Validation Failed")
    private String title;

    @Schema(description = "Detailed error description", example = "The field 'email' must be a valid email address.")
    private String details;

    @Schema(description = "Application-specific error code", example = "AUTH-OTP-001")
    private String code;

    @Schema(description = "Http status code", example = "400")
    private int status;

    @Schema(description = "List of validation errors", example = "[\"Name is required\"]")
    private List<String> errors;

    public ApiErrorResponse(String title, String details, String code, int status, List<String> errors) {
        this.title = title;
        this.details = details;
        this.code = code;
        this.status = status;
        this.errors = errors;
    }
}