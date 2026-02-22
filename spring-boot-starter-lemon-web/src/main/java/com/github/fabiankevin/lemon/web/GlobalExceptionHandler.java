package com.github.fabiankevin.lemon.web;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.github.fabiankevin.lemon.web.dto.ApiErrorResponse;
import com.github.fabiankevin.lemon.web.exceptions.ApiException;
import com.github.fabiankevin.lemon.web.exceptions.BusinessRuleException;
import com.github.fabiankevin.lemon.web.exceptions.DomainException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRuleExceptions(BusinessRuleException ex) {
        log.debug("BusinessRuleException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatusCode.valueOf(400))
                .body(ApiErrorResponse.builder()
                        .title("Domain error")
                        .details(ex.getMessage())
                        .status(400)
                        .code(ex.getCode())
                        .build());

    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(DomainException ex) {
        log.debug("DomainException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatusCode.valueOf(400))
                .body(ApiErrorResponse.builder()
                        .title("Domain error")
                        .details(ex.getMessage())
                        .status(400)
                        .build());

    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(ApiException ex) {
        log.debug("ApiException: {}", ex.getMessage(), ex);

        return ResponseEntity.status(ex.getHttpStatusCode())
                .body(ApiErrorResponse.builder()
                        .title("Request failed")
                        .details(ex.getMessage())
                        .status(ex.getHttpStatusCode())
                        .build());

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        log.debug("MethodArgumentNotValidException: {}", ex.getMessage(), ex);

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ApiErrorResponse.builder()
                .title("Invalid request parameters")
                .status(HttpStatus.BAD_REQUEST.value())
                .errors(errors)
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.debug("HttpMessageNotReadableException: {}", ex.getMessage(), ex);

        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
            return ApiErrorResponse.builder()
                    .title("Invalid request body")
                    .status(HttpStatus.BAD_REQUEST.value())
                    .errors(invalidFormatException.getPath().stream()
                            .map(ref -> "The value provided for '%s' has an incorrect format".formatted(ref.getFieldName()))
                            .toList())
                    .build();
        }

        return ApiErrorResponse.builder()
                .title("Invalid request body")
                .status(HttpStatus.BAD_REQUEST.value())
                .details("The request body is not properly formatted or contains invalid JSON")
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.debug("MethodArgumentTypeMismatchException: {}", ex.getMessage(), ex);
        String errorMessage = String.format("Parameter '%s' must be of type '%s'", ex.getName(), ex.getRequiredType().getSimpleName());
        return ApiErrorResponse.builder()
                .title("Type mismatch")
                .status(HttpStatus.BAD_REQUEST.value())
                .details(errorMessage)
                .build();
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleRequestBindingException(ServletRequestBindingException ex) {
        log.debug("ServletRequestBindingException: {}", ex.getMessage(), ex);
        return ApiErrorResponse.builder()
                .title("Missing required request parameters")
                .status(HttpStatus.BAD_REQUEST.value())
                .details("Required request parameters or headers are missing or invalid")
                .build();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handlerMethodValidationException(HandlerMethodValidationException ex) {
        log.debug("HandlerMethodValidationException: {}", ex.getMessage(), ex);
        return ApiErrorResponse.builder()
                .title("Invalid request")
                .status(HttpStatus.BAD_REQUEST.value())
                .errors(ex.getValueResults().getFirst().getResolvableErrors().stream()
                        .map(MessageSourceResolvable::getDefaultMessage)
                        .toList())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleConstraintViolationException(ConstraintViolationException ex) {
        log.debug("ConstraintViolationException: {}", ex.getMessage(), ex);
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        return ApiErrorResponse.builder()
                .title("Validation failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .errors(errors)
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMissingParams(MissingServletRequestParameterException ex) {
        log.debug("MissingServletRequestParameterException: {}", ex.getMessage(), ex);
        return ApiErrorResponse.builder()
                .title("Missing parameter")
                .status(HttpStatus.BAD_REQUEST.value())
                .details(String.format("The required parameter '%s' of type '%s' is missing",
                        ex.getParameterName(), ex.getParameterType()))
                .build();
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMissingHeader(MissingRequestHeaderException ex) {
        log.debug("MissingRequestHeaderException: {}", ex.getMessage(), ex);
        return ApiErrorResponse.builder()
                .title("Missing header")
                .status(HttpStatus.BAD_REQUEST.value())
                .details(String.format("The required header '%s' is missing", ex.getHeaderName()))
                .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleAccessDeniedException(AccessDeniedException ex) {
        log.debug("AccessDeniedException: {}", ex.getMessage(), ex);
        return ApiErrorResponse.builder()
                .title("Access denied")
                .status(HttpStatus.FORBIDDEN.value())
                .details("You don't have permission to access this resource")
                .build();
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiErrorResponse handle(HttpRequestMethodNotSupportedException ex) {
        log.debug("HttpRequestMethodNotSupportedException: {}", ex.getMessage(), ex);
        return ApiErrorResponse.builder()
                .title("Method not allowed")
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .details(String.format("The %s method is not allowed for this endpoint. Allowed methods are: %s", ex.getMethod(), ex.getSupportedHttpMethods()))
                .build();
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ApiErrorResponse handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        log.debug("HttpMediaTypeNotSupportedException: {}", ex.getMessage(), ex);
        return ApiErrorResponse.builder()
                .title("Unsupported media type")
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .details(String.format("The content type '%s' is not supported. Supported types are: %s",
                        ex.getContentType(),
                        ex.getSupportedMediaTypes().stream()
                                .map(MediaType::toString)
                                .collect(Collectors.joining(", "))))
                .build();
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiErrorResponse handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex) {
        log.debug("AsyncRequestTimeoutException: {}", ex.getMessage(), ex);
        return ApiErrorResponse.builder()
                .title("Request timeout")
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .details("The request took too long to process and timed out")
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGenericException(Exception ex) {
        log.debug("handleGenericException: {}", ex.getMessage(), ex);
        return ApiErrorResponse.builder()
                .title("Internal server error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .details("An unexpected error occurred. Please try again later or contact support if the problem persists.")
                .build();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    public ApiErrorResponse handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.debug("MaxUploadSizeExceededException: {}", ex.getMessage(), ex);
        return ApiErrorResponse.builder()
                .title("Content too large")
                .status(HttpStatus.CONTENT_TOO_LARGE.value())
                .details("The uploaded content exceeds the maximum allowed size")
                .build();
    }
}
