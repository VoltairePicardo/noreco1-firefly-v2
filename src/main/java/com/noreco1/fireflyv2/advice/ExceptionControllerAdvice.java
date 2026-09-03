package com.noreco1.fireflyv2.advice;

import com.noreco1.fireflyv2.exception.BusinessException;
import com.noreco1.fireflyv2.exception.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionControllerAdvice.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = error instanceof FieldError fieldError
                    ? fieldError.getField()
                    : error.getObjectName();
            errors.put(fieldName, error.getDefaultMessage());
        });
        logger.warn("Validation error: {}", errors);
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .path(request.getDescription(false))
                .validationErrors(errors)
                .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        logger.warn("Bad credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Invalid username or password")
                .path(request.getDescription(false))
                .build());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, WebRequest request) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Authentication Failed")
                .message("Authentication failed")
                .path(request.getDescription(false))
                .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        logger.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error("Access Denied")
                .message("You do not have permission to perform this action.")
                .path(request.getDescription(false))
                .build());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, WebRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getReason() != null ? ex.getReason() : "Unexpected error")
                .path(request.getDescription(false))
                .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        logger.error("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Violation")
                .message("A data constraint was violated. Please check your input.")
                .path(request.getDescription(false))
                .build());
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(Exception ex, WebRequest request) {
        logger.warn("No resource found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("The requested resource was not found")
                .path(request.getDescription(false))
                .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        logger.warn("Malformed request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Malformed JSON request")
                .path(request.getDescription(false))
                .build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, WebRequest request) {
        logger.warn("Missing parameter: {}", ex.getParameterName());
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Required parameter '" + ex.getParameterName() + "' is missing")
                .path(request.getDescription(false))
                .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        logger.warn("Type mismatch for '{}': expected {}", ex.getName(), requiredType);
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Parameter '" + ex.getName() + "' should be of type " + requiredType)
                .path(request.getDescription(false))
                .build());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        String supported = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().stream().map(Object::toString).collect(Collectors.joining(", "))
                : "none";
        logger.warn("Method '{}' not supported on '{}'. Supported: [{}]", ex.getMethod(), request.getDescription(false), supported);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ErrorResponse.builder()
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Method Not Allowed")
                .message("Method '" + ex.getMethod() + "' is not supported. Supported: [" + supported + "]")
                .path(request.getDescription(false))
                .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
        logger.warn("Constraint violations: {}", errors);
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Constraint violation")
                .path(request.getDescription(false))
                .validationErrors(errors)
                .build());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, WebRequest request) {
        logger.warn("Business exception: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(ErrorResponse.builder()
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        logger.error("Unexpected exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getDescription(false))
                .build());
    }
}
