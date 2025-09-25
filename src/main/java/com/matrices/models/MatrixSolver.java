package com.matrices.models;

import java.util.Arrays;

public class MatrixSolver {

    public static String solveGaussJordan(double[][] A, double[] B) {
        int n = A.length;
        double[][] M = new double[n][n + 1];
        final double EPSILON = 1e-9; // Una pequeña tolerancia para comparar doubles

        // 1. Construir la matriz aumentada (esto estaba bien)
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, M[i], 0, n);
            M[i][n] = B[i];
        }

        // 2. Aplicar eliminación de Gauss-Jordan para obtener la forma escalonada reducida
        int pivotRow = 0;
        for (int col = 0; col < n && pivotRow < n; col++) {
            // --- Operación III: Intercambiar filas (Pivoteo parcial) ---
            // Buscar la fila con el valor absoluto más grande en la columna actual para usarla como pivote.
            // Esto mejora la estabilidad numérica y evita divisiones por cero.
            int maxRow = pivotRow;
            for (int i = pivotRow + 1; i < n; i++) {
                if (Math.abs(M[i][col]) > Math.abs(M[maxRow][col])) {
                    maxRow = i;
                }
            }
            // Intercambiar la fila del pivote actual con la fila máxima encontrada
            double[] temp = M[pivotRow];
            M[pivotRow] = M[maxRow];
            M[maxRow] = temp;

            // Si el pivote sigue siendo casi cero, esta columna no tiene pivote, pasamos a la siguiente
            if (Math.abs(M[pivotRow][col]) < EPSILON) {
                continue;
            }

            // --- Operación I: Multiplicar fila para que el pivote sea 1 ---
            double pivotValue = M[pivotRow][col];
            for (int j = col; j <= n; j++) {
                M[pivotRow][j] /= pivotValue;
            }

            // --- Operación II: Sumar a una fila un múltiplo de otra ---
            // Hacer cero los demás elementos de la columna del pivote
            for (int i = 0; i < n; i++) {
                if (i != pivotRow) {
                    double factor = M[i][col];
                    for (int j = col; j <= n; j++) {
                        M[i][j] -= factor * M[pivotRow][j];
                    }
                }
            }
            pivotRow++;
        }

        // 3. Analizar la matriz final para determinar el tipo de solución

        // Búsqueda de filas inconsistentes (ej: [0 0 0 | c] con c != 0)
        for (int i = 0; i < n; i++) {
            boolean allCoefficientsZero = true;
            for (int j = 0; j < n; j++) {
                if (Math.abs(M[i][j]) > EPSILON) {
                    allCoefficientsZero = false;
                    break;
                }
            }
            if (allCoefficientsZero && Math.abs(M[i][n]) > EPSILON) {
                return "Sistema Incompatible.\nNo tiene solución."; //
            }
        }

        // Si el número de pivotes (pivotRow) es menor que el número de incógnitas (n),
        // significa que hay variables libres, por lo tanto, infinitas soluciones.
        if (pivotRow < n) {
            return "Sistema Compatible Indeterminado.\nPosee infinitas soluciones."; //
        }

        // Si no es incompatible ni indeterminado, es determinado.
        StringBuilder sb = new StringBuilder("Sistema Compatible Determinado.\nSoluciones:\n"); // [cite: 73]
        for (int i = 0; i < n; i++) {
            // El valor de x_i se encuentra en la última columna de la fila i
            sb.append(String.format("x%d = %.4f\n", i + 1, M[i][n]));
        }
        return sb.toString();
    }
}