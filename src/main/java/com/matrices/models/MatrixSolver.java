package com.matrices.models;

public class MatrixSolver {

    public static String solveGaussJordan(double[][] A, double[] B) {
        int n = A.length;
        double[][] M = new double[n][n + 1];

        // Construir la matriz aumentada
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, M[i], 0, n);
            M[i][n] = B[i];
        }

        // Aplicar Gauss-Jordan
        //
        for (int i = 0; i < n; i++) {
            // Buscar pivote
            if (M[i][i] == 0) {
                boolean swapped = false;
                for (int k = i + 1; k < n; k++) {
                    if (M[k][i] != 0) {
                        double[] temp = M[i];
                        M[i] = M[k];
                        M[k] = temp;
                        swapped = true;
                        break;
                    }
                }
                if (!swapped) {
                    return "Sistema Incompatible o Indeterminado.";
                }
            }
            //commit
            // Normalizar la fila
            double pivot = M[i][i];
            for (int j = 0; j <= n; j++) {
                M[i][j] /= pivot;
            }

            // Hacer ceros en las demás filas
            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = M[k][i];
                    for (int j = 0; j <= n; j++) {
                        M[k][j] -= factor * M[i][j];
                    }
                }
            }
        }

        // Extraer soluciones
        StringBuilder sb = new StringBuilder("Sistema Compatible Determinado.\nSoluciones:\n");
        for (int i = 0; i < n; i++) {
            sb.append("x").append(i + 1).append(" = ").append(M[i][n]).append("\n");
        }

        return sb.toString();
    }
}


