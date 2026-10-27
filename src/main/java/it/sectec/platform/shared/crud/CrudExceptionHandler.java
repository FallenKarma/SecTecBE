package it.sectec.platform.shared.crud;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = CrudController.class)
public class CrudExceptionHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CrudExceptionHandler.class);
    @ExceptionHandler(CrudException.class)
    ResponseEntity<Map<String, String>> business(CrudException ex) {
        log.debug("CRUD request rejected: status={} code={}", ex.status(), ex.getMessage());
        return ResponseEntity.status(ex.status()).body(Map.of("code", ex.getMessage()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> conflict(DataIntegrityViolationException ex) {
        log.warn("CRUD write rejected: DATA_CONFLICT; database integrity constraint violated");
        return ResponseEntity.status(409).body(Map.of("code", "DATA_CONFLICT"));
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, ConstraintViolationException.class})
    ResponseEntity<Map<String, String>> invalid(Exception ex) {
        log.debug("CRUD validation failed: exceptionType={}", ex.getClass().getSimpleName());
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_REQUEST"));
    }
}
