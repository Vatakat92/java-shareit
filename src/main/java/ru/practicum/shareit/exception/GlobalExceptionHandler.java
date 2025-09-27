package ru.practicum.shareit.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, ValidationException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Bad request occurred";
        return logAndBuild(HttpStatus.BAD_REQUEST, message, null, ex);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Conflict occurred";
        return logAndBuild(HttpStatus.CONFLICT, message, null, ex);
    }

    @ExceptionHandler({java.lang.SecurityException.class, SecurityException.class})
    public ResponseEntity<ErrorResponse> handleForbidden(Exception ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Forbidden access";
        return logAndBuild(HttpStatus.FORBIDDEN, message, null, ex);
    }

    @ExceptionHandler({NoSuchElementException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Resource not found";
        return logAndBuild(HttpStatus.NOT_FOUND, message, null, ex);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        HttpStatus status = HttpStatus.resolve(code);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        return logAndBuild(status, message, null, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<FieldErrorDto> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDto(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        String joined = fieldErrors.stream()
                .map(fe -> fe.getField() + ": " + fe.getMessage())
                .collect(Collectors.joining(", "));
        return logAndBuild(HttpStatus.BAD_REQUEST, "Validation failed: " + joined, fieldErrors, ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldErrorDto> fieldErrors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String path = violation.getPropertyPath().toString();
                    String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return new FieldErrorDto(field, violation.getMessage());
                })
                .collect(Collectors.toList());
        String joined = fieldErrors.stream()
                .map(fe -> fe.getField() + ": " + fe.getMessage())
                .collect(Collectors.joining(", "));
        return logAndBuild(HttpStatus.BAD_REQUEST, "Validation failed: " + joined, fieldErrors, ex);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String typeName = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        String message = String.format("Parameter '%s' should be a valid '%s' but was '%s'",
                ex.getName(), typeName, value);
        return logAndBuild(HttpStatus.BAD_REQUEST, message, null, ex);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        String message = "Required request header '" + ex.getHeaderName() + "' is not present";
        return logAndBuild(HttpStatus.BAD_REQUEST, message, null, ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        String message = "Malformed JSON request";
        return logAndBuild(HttpStatus.BAD_REQUEST, message, null, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
        return logAndBuild(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", null, ex);
    }

    private ResponseEntity<ErrorResponse> logAndBuild(HttpStatus status, String message,
                                                      List<FieldErrorDto> errors, Exception ex) {
        if (status.is4xxClientError()) {
            if (ex != null && ex.getMessage() != null) {
                log.warn("{} {}: {}", status.value(), status.getReasonPhrase(), ex.getMessage());
            } else {
                log.warn("{} {}: {}", status.value(), status.getReasonPhrase(), message);
            }
        } else {
            log.error("{} {}: {}", status.value(), status.getReasonPhrase(), message, ex);
        }
        return build(status, message, errors);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, List<FieldErrorDto> errors) {
        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(Instant.now().toString())
                .errors(errors)
                .build();
        return new ResponseEntity<>(body, status);
    }
}