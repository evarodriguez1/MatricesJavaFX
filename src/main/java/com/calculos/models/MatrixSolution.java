package com.calculos.models;

import java.util.Optional;

/**
 * Objeto de dominio inmutable que encapsula el resultado de la resolución de un sistema de ecuaciones.
 * @param type El tipo de solución encontrada (ej: COMPATIBLE_DETERMINATE).
 * @param solutions Un array opcional con los valores de las variables si la solución es única.
 */
public record MatrixSolution(MatrixSolutionType type, Optional<double[]> solutions) {

    /**
     * @return true si la solución es única y está presente.
     */
    public boolean hasUniqueSolution() {
        return type == MatrixSolutionType.COMPATIBLE_DETERMINATE && solutions.isPresent();
    }
}