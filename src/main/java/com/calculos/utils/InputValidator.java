package com.calculos.utils;

import com.calculos.utils.exceptions.ValidationException;

/**
 * Clase de utilidad para validar y parsear las entradas de texto de la UI.
 * Centraliza toda la lógica de validación para asegurar consistencia y robustez,
 * lanzando una ValidationException con mensajes claros y contextuales si la
 * validación falla.
 *
 * Esta clase está marcada como 'final' y tiene un constructor privado para
 * prevenir la herencia y la instanciación, siguiendo las mejores prácticas para
 * clases de utilidad estáticas.
 *
 * @author Tu Nombre (Equipo de Desarrollo)
 */
public final class InputValidator {

    /**
     * Constructor privado para prevenir la instanciación de esta clase de utilidad.
     */
    private InputValidator() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada.");
    }

    /**
     * Parsea un texto a un double, validando que no esté vacío y sea un número.
     * Es el método base para el resto de validaciones numéricas.
     *
     * @param text      El texto del campo de entrada.
     * @param fieldName El nombre descriptivo del campo para los mensajes de error.
     * @return El valor double parseado.
     * @throws ValidationException si la validación falla.
     */
    public static double parseDouble(String text, String fieldName) throws ValidationException {
        String trimmedText = validateNotEmpty(text, fieldName);
        try {
            return Double.parseDouble(trimmedText.replace(',', '.')); // Reemplaza coma por punto para flexibilidad
        } catch (NumberFormatException e) {
            throw new ValidationException("El campo '" + fieldName + "' debe ser un número válido (ej: 3.14 o 3,14).");
        }
    }

    /**
     * Parsea un texto a un entero, asegurando que no tenga decimales.
     *
     * @param text      El texto del campo de entrada.
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
     * Parsea un texto a un entero y valida que no sea negativo (>= 0).
     *
     * @param text      El texto del campo de entrada.
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
     * Parsea un texto a un entero y valida que sea estrictamente positivo (> 0).
     *
     * @param text      El texto del campo de entrada.
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
     * Parsea un texto a un double y valida que esté dentro de un rango inclusivo [min, max].
     *
     * @param text El texto del campo de entrada.
     * @param fieldName El nombre del campo para mensajes de error.
     * @param min El valor mínimo permitido.
     * @param max El valor máximo permitido.
     * @return El valor double validado.
     * @throws ValidationException si la validación falla.
     */
    public static double parseDoubleInRange(String text, String fieldName, double min, double max) throws ValidationException {
        double value = parseDouble(text, fieldName);
        if (value < min || value > max) {
            throw new ValidationException(String.format("El campo '%s' debe estar en el rango [%.2f, %.2f].", fieldName, min, max));
        }
        return value;
    }

    /**
     * Método de utilidad privado que comprueba si un String es nulo, vacío o solo contiene espacios en blanco.
     * Es reutilizado por todos los métodos públicos de parseo para evitar duplicación de código.
     *
     * @param text      El texto a validar.
     * @param fieldName El nombre del campo para el mensaje de error.
     * @return El texto sin espacios al principio o al final (trimmed).
     * @throws ValidationException si el texto es nulo o está vacío.
     */
    private static String validateNotEmpty(String text, String fieldName) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("El campo '" + fieldName + "' no puede estar vacío.");
        }
        return text.trim();
    }
}