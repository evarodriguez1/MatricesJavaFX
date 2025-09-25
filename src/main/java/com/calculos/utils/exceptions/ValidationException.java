package com.calculos.utils.exceptions;

/**
 * Excepción personalizada para representar errores de validación de la entrada del usuario.
 * Hereda de Exception en lugar de RuntimeException para forzar a los controladores
 * a manejarla explícitamente, promoviendo un código más robusto.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}