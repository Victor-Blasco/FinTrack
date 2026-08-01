package com.victorblasco.fintrack.ingest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestor global de excepciones de controladores REST para banking-ingest-service.
 * Mapea excepciones de negocio a respuestas HTTP estructuradas con códigos estandarizados.
 *
 * @author Victor Blasco
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura y maneja la excepción de duplicidad de archivos CSV.
     *
     * @param ex excepción {@link DuplicateCsvException} capturada
     * @return respuesta HTTP 409 Conflict con mapa de detalles del error
     */
    @ExceptionHandler(DuplicateCsvException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateCsv(DuplicateCsvException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Captura y maneja errores de validación de campos de entrada DTO (Spring Validation).
     *
     * @param ex excepción {@link MethodArgumentNotValidException} capturada
     * @return respuesta HTTP 400 Bad Request con detalles por campo afectado
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        body.put("message", "Error de validación en la petición");
        body.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Captura y maneja argumentos ilegales pasados a servicios de dominio.
     *
     * @param ex excepción {@link IllegalArgumentException} capturada
     * @return respuesta HTTP 400 Bad Request
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
}
