package com.calculos.utils;

import com.calculos.utils.exceptions.ValidationException;

/**
 * Clase de utilidad para validar y parsear entradas de texto de la UI.
 * Proporciona métodos defensivos que lanzan una ValidationException
 * con mensajes claros y contextuales si la validación falla.
 *
 * Todas las validaciones limpian el texto (trim) y son sensibles a nulos/vacíos.
 */
public final class InputValidator {

    /**
     * Constructor privado para prevenir la instanciación de esta clase de utilidad.
     */
    private InputValidator() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada.");
    }

    /**
     * Parsea un texto a double. Falla si está vacío o no es un número.
     * @param text El texto del campo de entrada.
     * @param fieldName El nombre descriptivo del campo para los mensajes de error.
     * @return El valor double parseado.
     * @throws ValidationException si la validación falla.
     */
    public static double parseDouble(String text, String fieldName) throws ValidationException {
        String trimmedText = validateNotEmpty(text, fieldName);
        try {
            return Double.parseDouble(trimmedText);
        } catch (NumberFormatException e) {
            throw new ValidationException("El campo '" + fieldName + "' debe ser un número válido (ej: 3.14).");
        }
    }

    /**
     * Parsea un texto a un entero. Falla si está vacío, no es número o tiene decimales.
     * @param text El texto del campo de entrada.
     * @param fieldName El nombre descriptivo del campo para los mensajes de error.
     * @return El valor entero parseado.
     * @throws ValidationException si la validación falla.
     */
    public static int parseInt(String text, String fieldName) throws ValidationException {
        double doubleValue = parseDouble(text, fieldName);
        if (doubleValue % 1 != 0) {
            throw new ValidationException("El campo '" + fieldName + "' debe ser un número entero (sin decimales).");
        }
        return (int) doubleValue;
    }

    /**
     * Parsea un texto a un entero no negativo (>= 0).
     * @param text El texto del campo de entrada.
     * @param fieldName El nombre descriptivo del campo para los mensajes de error.
     * @return El valor entero no negativo.
     * @throws ValidationException si la validación falla.
     */
    public static int parseNonNegativeInt(String text, String fieldName) throws ValidationException {
        int intValue = parseInt(text, fieldName);
        if (intValue < 0) {
            throw new ValidationException("El campo '" + fieldName + "' no puede ser negativo.");
        }
        return intValue;
    }

    /**
     * Parsea un texto a un entero estrictamente positivo (> 0).
     * @param text El texto del campo de entrada.
     * @param fieldName El nombre descriptivo del campo para los mensajes de error.
     * @return El valor entero positivo.
     * @throws ValidationException si la validación falla.
     */
    public static int parsePositiveInt(String text, String fieldName) throws ValidationException {
        int intValue = parseInt(text, fieldName);
        if (intValue <= 0) {
            throw new ValidationException("El campo '" + fieldName + "' debe ser un número positivo (mayor que cero).");
        }
        return intValue;
    }

    /**
     * Valida que un campo de texto no esté vacío, nulo o solo contenga espacios.
     * Este método privado es reutilizado por todos los métodos públicos.
     * @return El texto limpio (trimmed).
     * @throws ValidationException si el campo está vacío.
     */
    private static String validateNotEmpty(String text, String fieldName) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("El campo '" + fieldName + "' no puede estar vacío.");
        }
        return text.trim();
    }
}