package com.victorblasco.fintrack.ingest.exception;

/**
 * Excepción lanzada cuando se intenta procesar un archivo CSV cuyo contenido SHA-256 ya fue registrado previamente.
 *
 * @author Victor Blasco
 */
public class DuplicateCsvException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje descriptivo.
     *
     * @param message mensaje explicativo del motivo del rechazo
     */
    public DuplicateCsvException(String message) {
        super(message);
    }
}
