package com.victorblasco.exception;

/**
 * Excepción de negocio lanzada cuando el correo electrónico o la contraseña ingresados son erróneos.
 *
 * @author Victor Blasco
 */
public class InvalidCredentialsException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param message mensaje explicativo del error
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
