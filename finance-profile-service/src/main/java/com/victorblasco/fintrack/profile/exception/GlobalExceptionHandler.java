package com.victorblasco.fintrack.profile.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global y centralizado de excepciones REST para finance-profile-service.
 * Estandariza la respuesta de error en formato JSON coherente con el resto del ecosistema.
 *
 * @author Victor Blasco
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura errores de argumentos ilegales o no válidos (por ejemplo, identificadores ausentes).
     *
     * @param ex excepción {@link IllegalArgumentException} interceptada
     * @return {@link ResponseEntity} con código HTTP 400 Bad Request y cuerpo estructurado
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Captura fallos de validación en DTOs anotados con {@code @Valid}.
     *
     * @param ex excepción {@link MethodArgumentNotValidException} con los errores de campo
     * @return {@link ResponseEntity} con código HTTP 400 Bad Request y detalle por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        body.put("message", "Error de validación en los campos de la petición");
        body.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
