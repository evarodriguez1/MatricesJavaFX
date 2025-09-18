package com.matrices.utils;

public class InputValidator {
    public static double parseDouble(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + fieldName + " no puede estar vacío.");
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + fieldName + " debe ser un número válido.");
        }
    }
}
