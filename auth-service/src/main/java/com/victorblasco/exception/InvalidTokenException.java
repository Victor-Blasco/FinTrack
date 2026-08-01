package com.victorblasco.exception;

/**
 * Excepción de negocio lanzada cuando un token JWT no supera las verificaciones de firma o fecha de expiración.
 *
 * @author Victor Blasco
 */
public class InvalidTokenException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param message mensaje explicativo del error
     */
    public InvalidTokenException(String message) {
        super(message);
    }
}
