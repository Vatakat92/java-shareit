package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidation(BusinessValidationException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Business validation failed";
        logError(HttpStatus.BAD_REQUEST, message, ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, null);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Bad request occurred";
        logError(HttpStatus.BAD_REQUEST, message, ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Conflict occurred";
        logError(HttpStatus.CONFLICT, message, ex);
        return buildErrorResponse(HttpStatus.CONFLICT, message, null);
    }

    @ExceptionHandler({java.lang.SecurityException.class, NotItemOwnerException.class})
    public ResponseEntity<ErrorResponse> handleForbidden(Exception ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Forbidden access";
        logError(HttpStatus.FORBIDDEN, message, ex);
        return buildErrorResponse(HttpStatus.FORBIDDEN, message, null);
    }

    @ExceptionHandler({NoSuchElementException.class, jakarta.persistence.EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Resource not found";
        logError(HttpStatus.NOT_FOUND, message, ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, message, null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        HttpStatus status = HttpStatus.resolve(code);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        logError(status, message, ex);
        return buildErrorResponse(status, message, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
        logError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", null);
    }

    private void logError(HttpStatus status, String message, Exception ex) {
        log.error("{} {}: {}", status.value(), status.getReasonPhrase(), message, ex);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, List<FieldErrorDto> errors) {
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