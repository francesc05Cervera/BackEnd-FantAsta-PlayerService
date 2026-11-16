package com.example.fantasta.player_service.Exceptions;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<?> forbidden(ForbiddenException exception) { return error(HttpStatus.FORBIDDEN, exception.getMessage()); }
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<?> notFound(NotFoundException exception) { return error(HttpStatus.NOT_FOUND, exception.getMessage()); }
    @ExceptionHandler(DuplicatePlayerException.class)
    ResponseEntity<?> conflict(DuplicatePlayerException exception) { return error(HttpStatus.CONFLICT, exception.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> invalid(MethodArgumentNotValidException exception) { return error(HttpStatus.BAD_REQUEST, "Request validation failed"); }
    @ExceptionHandler(TokenException.class)
    ResponseEntity<?> unauthorized(TokenException exception) { return error(HttpStatus.UNAUTHORIZED, exception.getMessage()); }
    @ExceptionHandler(InvalidPlayerRequestException.class)
    ResponseEntity<?> invalidPlayer(InvalidPlayerRequestException exception) { return error(HttpStatus.BAD_REQUEST, exception.getMessage()); }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<?> upstream(ResponseStatusException exception) {
        return error(HttpStatus.valueOf(exception.getStatusCode().value()), exception.getReason());
    }
    private ResponseEntity<?> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("timestamp", Instant.now().toString(), "status", status.value(), "error", status.getReasonPhrase(), "message", message));
    }
}
