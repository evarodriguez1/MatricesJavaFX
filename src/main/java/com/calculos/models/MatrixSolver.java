package com.calculos.models;

import java.util.Arrays;
import java.util.Optional;

/**
 * Clase de utilidad que proporciona algoritmos para resolver sistemas de ecuaciones lineales.
 * La lógica está contenida en un método estático que devuelve un objeto de dominio
 * {@link MatrixSolution}, separando completamente el cálculo de la presentación.
 *
 * @author Tu Nombre (Equipo de Desarrollo)
 */
public final class MatrixSolver {

    private static final double EPSILON = 1e-9; // Tolerancia para comparaciones de punto flotante

    private MatrixSolver() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada.");
    }

    /**
     * Resuelve un sistema de ecuaciones lineales [A][x] = [B] usando el método de Gauss-Jordan
     * con pivoteo parcial para mejorar la estabilidad numérica.
     *
     * @param a La matriz de coeficientes (NxN).
     * @param b El vector de resultados (Nx1).
     * @return Un objeto {@link MatrixSolution} que encapsula el tipo de solución y los resultados.
     */
    public static MatrixSolution solve(double[][] a, double[] b) {
        // Copia defensiva para no modificar los arrays originales
        double[][] augmentedMatrix = buildAugmentedMatrix(a, b);
        int n = augmentedMatrix.length;

        // Fase 1: Reducción por filas (Gauss-Jordan)
        int pivotCount = performRowReduction(augmentedMatrix);

        // Fase 2: Análisis del resultado
        if (hasInconsistentRow(augmentedMatrix)) {
            return new MatrixSolution(MatrixSolutionType.INCOMPATIBLE, Optional.empty());
        }

        if (pivotCount < n) {
            return new MatrixSolution(MatrixSolutionType.COMPATIBLE_INDETERMINATE, Optional.empty());
        }

        double[] solutions = extractSolutions(augmentedMatrix);
        return new MatrixSolution(MatrixSolutionType.COMPATIBLE_DETERMINATE, Optional.of(solutions));
    }

    private static double[][] buildAugmentedMatrix(double[][] a, double[] b) {
        int n = a.length;
        double[][] m = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            m[i] = Arrays.copyOf(a[i], n + 1); // Más limpio y seguro
            m[i][n] = b[i];
        }
        return m;
    }

    private static int performRowReduction(double[][] m) {
        int n = m.length;
        int pivotRow = 0;
        for (int col = 0; col < n && pivotRow < n; col++) {
            // Pivoteo Parcial: encontrar la fila con el máximo valor en la columna actual
            int maxRow = pivotRow;
            for (int i = pivotRow + 1; i < n; i++) {
                if (Math.abs(m[i][col]) > Math.abs(m[maxRow][col])) {
                    maxRow = i;
                }
            }
            swapRows(m, pivotRow, maxRow);

            // Si el pivote es efectivamente cero, esta columna es linealmente dependiente
            if (Math.abs(m[pivotRow][col]) < EPSILON) {
                continue; // Saltar a la siguiente columna
            }

            // Normalizar la fila del pivote (hacer el pivote 1)
            double pivotValue = m[pivotRow][col];
            for (int j = col; j < n + 1; j++) {
                m[pivotRow][j] /= pivotValue;
            }

            // Hacer ceros en las demás filas de la columna del pivote
            for (int i = 0; i < n; i++) {
                if (i != pivotRow) {
                    double factor = m[i][col];
                    for (int j = col; j < n + 1; j++) {
                        m[i][j] -= factor * m[pivotRow][j];
                    }
                }
            }
            pivotRow++;
        }
        return pivotRow; // El número de pivotes encontrados
    }

    private static boolean hasInconsistentRow(double[][] m) {
        int n = m.length;
        for (int i = 0; i < n; i++) {
            boolean allCoefficientsZero = true;
            for (int j = 0; j < n; j++) {
                if (Math.abs(m[i][j]) > EPSILON) {
                    allCoefficientsZero = false;
                    break;
                }
            }
            if (allCoefficientsZero && Math.abs(m[i][n]) > EPSILON) {
                return true; // Fila de ceros con resultado no nulo [0 0 0 | c], c != 0
            }
        }
        return false;
    }

    private static double[] extractSolutions(double[][] m) {
        int n = m.length;
        double[] solutions = new double[n];
        for (int i = 0; i < n; i++) {
            solutions[i] = m[i][n];
        }
        return solutions;
    }

    private static void swapRows(double[][] matrix, int r1, int r2) {
        double[] temp = matrix[r1];
        matrix[r1] = matrix[r2];
        matrix[r2] = temp;
    }
}