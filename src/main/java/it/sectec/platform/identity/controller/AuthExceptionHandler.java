package it.sectec.platform.identity.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes = {AuthController.class, AccountLifecycleController.class})
public class AuthExceptionHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthExceptionHandler.class);
    @ExceptionHandler(it.sectec.platform.shared.crud.CrudException.class)
    ResponseEntity<Map<String, String>> business(it.sectec.platform.shared.crud.CrudException ex) {
        log.debug("Account request rejected: status={} code={}", ex.status(), ex.getMessage());
        return ResponseEntity.status(ex.status()).body(Map.of("code", ex.getMessage()));
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<Map<String, String>> invalidRequest(Exception exception) {
        log.debug("Account validation failed: exceptionType={}", exception.getClass().getSimpleName());
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_REQUEST"));
    }
}

