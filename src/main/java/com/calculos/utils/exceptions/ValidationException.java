package com.calculos.utils.exceptions;

/**
 * Excepción personalizada para representar errores de validación de la entrada del usuario.
 * Hereda de Exception para forzar a los controladores a manejarla explícitamente,
 * lo que promueve un código más robusto y previene fallos silenciosos.
 */
public class ValidationException extends Exception {

    /**
     * Constructor que acepta el mensaje de error que se mostrará al usuario.
     * @param message El mensaje descriptivo del error de validación.
     */
    public ValidationException(String message) {
        super(message);
    }
}