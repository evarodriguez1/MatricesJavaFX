package com.calculos.models;

/**
 * Representa los posibles tipos de solución para un sistema de ecuaciones lineales.
 */
public enum MatrixSolutionType {
    INCOMPATIBLE("Sistema Incompatible", "El sistema no tiene solución."),
    COMPATIBLE_INDETERMINATE("Sistema Compatible Indeterminado", "El sistema posee infinitas soluciones."),
    COMPATIBLE_DETERMINATE("Sistema Compatible Determinado", "El sistema tiene una solución única.");

    private final String title;
    private final String description;

    MatrixSolutionType(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
}