package com.chatapp.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    public record ErrorBody(Instant timestamp, int status, String error, String message, String path) {}
    private ResponseEntity<ErrorBody> body(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorBody(Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
    }
    @ExceptionHandler(ResourceNotFoundException.class) ResponseEntity<ErrorBody> notFound(ResourceNotFoundException ex, HttpServletRequest req) { return body(HttpStatus.NOT_FOUND, ex.getMessage(), req); }
    @ExceptionHandler({BadRequestException.class, DataIntegrityViolationException.class}) ResponseEntity<ErrorBody> badRequest(RuntimeException ex, HttpServletRequest req) { return body(HttpStatus.BAD_REQUEST, ex instanceof BadRequestException ? ex.getMessage() : "The request conflicts with existing data.", req); }
    @ExceptionHandler(ForbiddenException.class) ResponseEntity<ErrorBody> forbidden(ForbiddenException ex, HttpServletRequest req) { return body(HttpStatus.FORBIDDEN, ex.getMessage(), req); }
    @ExceptionHandler(BadCredentialsException.class) ResponseEntity<ErrorBody> unauthorized(BadCredentialsException ex, HttpServletRequest req) { return body(HttpStatus.UNAUTHORIZED, "Invalid username/email or password.", req); }
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ErrorBody> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst().map(FieldError::getDefaultMessage).orElse("Please check your input.");
        return body(HttpStatus.BAD_REQUEST, message, req);
    }
    @ExceptionHandler(ConstraintViolationException.class) ResponseEntity<ErrorBody> constraint(ConstraintViolationException ex, HttpServletRequest req) { return body(HttpStatus.BAD_REQUEST, "Please check your input.", req); }
    @ExceptionHandler(Exception.class) ResponseEntity<ErrorBody> unexpected(Exception ex, HttpServletRequest req) { return body(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred.", req); }
}
