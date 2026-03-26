package com.github.fabiankevin.lemon.web;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.github.fabiankevin.lemon.web.exceptions.ApiException;
import com.github.fabiankevin.lemon.web.exceptions.BusinessRuleException;
import com.github.fabiankevin.lemon.web.exceptions.DomainException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRuleExceptions(BusinessRuleException ex) {
        log.debug("BusinessRuleException: {}", ex.getMessage(), ex);

        return problemResponse(HttpStatusCode.valueOf(ex.getHttpStatusCode()), ex.getTitle(), ex.getMessage(), ex.getCode(), null);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(DomainException ex) {
        log.debug("DomainException: {}", ex.getMessage(), ex);
        return problemResponse(HttpStatus.BAD_REQUEST, "Domain error", ex.getMessage());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleAppException(ApiException ex) {
        log.debug("ApiException: {}", ex.getMessage(), ex);

        return problemResponse(HttpStatusCode.valueOf(ex.getHttpStatusCode()), "Request failed", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        log.debug("MethodArgumentNotValidException: {}", ex.getMessage(), ex);

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .toList();
        return problemResponse(HttpStatus.BAD_REQUEST, "Invalid request parameters", "Request validation failed", null, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.debug("HttpMessageNotReadableException: {}", ex.getMessage(), ex);

        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
            return problemResponse(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request body",
                    "The request body contains values with an invalid format",
                    null,
                    invalidFormatException.getPath().stream()
                            .map(ref -> ref.getFieldName() != null
                                    ? "The value provided for '%s' has an incorrect format".formatted(ref.getFieldName())
                                    : "A value in the request body has an incorrect format")
                            .toList());
        }

        return problemResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request body",
                "The request body is not properly formatted or contains invalid JSON");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.debug("MethodArgumentTypeMismatchException: {}", ex.getMessage(), ex);
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "the expected type";
        String errorMessage = String.format("Parameter '%s' must be of type '%s'", ex.getName(), requiredType);
        return problemResponse(HttpStatus.BAD_REQUEST, "Type mismatch", errorMessage);
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ProblemDetail> handleRequestBindingException(ServletRequestBindingException ex) {
        log.debug("ServletRequestBindingException: {}", ex.getMessage(), ex);
        return problemResponse(
                HttpStatus.BAD_REQUEST,
                "Missing required request parameters",
                "Required request parameters or headers are missing or invalid");
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handlerMethodValidationException(HandlerMethodValidationException ex) {
        log.debug("HandlerMethodValidationException: {}", ex.getMessage(), ex);
        List<String> errors = ex.getValueResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return problemResponse(HttpStatus.BAD_REQUEST, "Invalid request", "Request validation failed", null, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(ConstraintViolationException ex) {
        log.debug("ConstraintViolationException: {}", ex.getMessage(), ex);
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        return problemResponse(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed", null, errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParams(MissingServletRequestParameterException ex) {
        log.debug("MissingServletRequestParameterException: {}", ex.getMessage(), ex);
        return problemResponse(
                HttpStatus.BAD_REQUEST,
                "Missing parameter",
                String.format("The required parameter '%s' of type '%s' is missing", ex.getParameterName(), ex.getParameterType()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetail> handleMissingHeader(MissingRequestHeaderException ex) {
        log.debug("MissingRequestHeaderException: {}", ex.getMessage(), ex);
        return problemResponse(HttpStatus.BAD_REQUEST, "Missing header", String.format("The required header '%s' is missing", ex.getHeaderName()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException ex) {
        log.debug("AccessDeniedException: {}", ex.getMessage(), ex);
        return problemResponse(HttpStatus.FORBIDDEN, "Access denied", "You don't have permission to access this resource");
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handle(HttpRequestMethodNotSupportedException ex) {
        log.debug("HttpRequestMethodNotSupportedException: {}", ex.getMessage(), ex);
        return problemResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method not allowed",
                String.format("The %s method is not allowed for this endpoint. Allowed methods are: %s", ex.getMethod(), ex.getSupportedHttpMethods()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        log.debug("HttpMediaTypeNotSupportedException: {}", ex.getMessage(), ex);
        return problemResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported media type",
                String.format("The content type '%s' is not supported. Supported types are: %s",
                        ex.getContentType(),
                        ex.getSupportedMediaTypes().stream()
                                .map(MediaType::toString)
                                .collect(Collectors.joining(", "))));
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<ProblemDetail> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex) {
        log.debug("AsyncRequestTimeoutException: {}", ex.getMessage(), ex);
        return problemResponse(HttpStatus.SERVICE_UNAVAILABLE, "Request timeout", "The request took too long to process and timed out");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        log.debug("handleGenericException: {}", ex.getMessage(), ex);
        return problemResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred. Please try again later or contact support if the problem persists.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.debug("MaxUploadSizeExceededException: {}", ex.getMessage(), ex);
        return problemResponse(HttpStatus.CONTENT_TOO_LARGE, "Content too large", "The uploaded content exceeds the maximum allowed size");
    }

    private ResponseEntity<ProblemDetail> problemResponse(HttpStatusCode status, String title, String detail) {
        return ResponseEntity.status(status).body(problemDetail(status, title, detail, null, null));
    }

    private ResponseEntity<ProblemDetail> problemResponse(HttpStatusCode status, String title, String detail, String code, List<String> errors) {
        return ResponseEntity.status(status).body(problemDetail(status, title, detail, code, errors));
    }

    private ProblemDetail problemDetail(HttpStatusCode status, String title, String detail, String code, List<String> errors) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);

        if (code != null && !code.isBlank()) {
            problemDetail.setProperty("code", code);
        }
        if (errors != null && !errors.isEmpty()) {
            problemDetail.setProperty("errors", errors);
        }

        return problemDetail;
    }
}
