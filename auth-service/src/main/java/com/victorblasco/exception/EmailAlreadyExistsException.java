package com.victorblasco.exception;

/**
 * Excepción de negocio lanzada cuando se intenta registrar un email que ya existe en la base de datos.
 *
 * @author Victor Blasco
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param message mensaje explicativo del error
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
