package com.calculos.models;

import org.apache.commons.math3.fraction.BigFraction;
import java.math.BigInteger;
import java.util.Arrays;

public class MatrixSolver {

    public static class SolveResult {
        public final String summary;
        public final String steps;

        public SolveResult(String summary, String steps) {
            this.summary = summary;
            this.steps = steps;
        }
    }

    // Tolerancia para comparaciones con punto flotante (dobles)
    private static final double EPSILON = 1e-9;

    /**
     * Resuelve un sistema 3x3 usando Gauss–Jordan.
     * Acepta Strings para mantener la precisión de las fracciones de entrada.
     *
     * @param A_str matriz de coeficientes como String
     * @param B_str vector de términos independientes como String
     * @param useFractions si es true, se usarán fracciones para cálculos exactos.
     * @return Un objeto SolveResult con el resumen y los pasos.
     */
    public static SolveResult solveGaussJordan(String[][] A_str, String[] B_str, boolean useFractions) {
        if (useFractions) {
            // Convertir todo a BigFraction
            int n = A_str.length;
            BigFraction[][] M_frac = new BigFraction[n][n + 1];

            try {
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        M_frac[i][j] = parseToBigFraction(A_str[i][j]);
                    }
                    M_frac[i][n] = parseToBigFraction(B_str[i]);
                }
            } catch (Exception e) {
                // Relanzar la excepción para que el controlador la capture
                throw new IllegalArgumentException("Error al convertir a fracción/número: " + e.getMessage());
            }

            return solveWithFractions(M_frac);
        } else {
            // Si NO se usan fracciones, convertimos a double para el solver de punto flotante
            int n = A_str.length;
            double[][] M_double = new double[n][n + 1];
            try {
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        M_double[i][j] = parseToDouble(A_str[i][j]);
                    }
                    M_double[i][n] = parseToDouble(B_str[i]);
                }
            } catch (Exception e) {
                // Relanzar la excepción para que el controlador la capture
                throw new IllegalArgumentException("Error al convertir a decimal/número: " + e.getMessage());
            }
            return solveWithDoubles(M_double);
        }
    }

    /**
     * Convierte una cadena (ej. "1", "3/4", "-1.5") a BigFraction.
     * Esto asegura que las fracciones de entrada se representan exactamente.
     */
    private static BigFraction parseToBigFraction(String s) {
        s = s.trim();
        if (s.contains("/")) {
            String[] parts = s.split("/");
            if (parts.length != 2) throw new NumberFormatException("Formato de fracción inválido: " + s);

            try {
                // Intenta usar BigInteger para enteros
                BigInteger num = new BigInteger(parts[0].trim());
                BigInteger den = new BigInteger(parts[1].trim());
                if (den.equals(BigInteger.ZERO)) throw new ArithmeticException("Denominador cero");
                return new BigFraction(num, den);
            } catch (NumberFormatException e) {
                // Si falla, significa que al menos un componente es un decimal (ej. "1.5/2").
                // Volvemos a la representación Double (menos ideal pero maneja entradas mixtas).
                return new BigFraction(parseToDouble(s));
            }
        } else {
            // Si es un entero o decimal simple (ej. "2" o "1.5")
            try {
                // Intenta leer como entero para máxima precisión
                return new BigFraction(new BigInteger(s));
            } catch (NumberFormatException e) {
                // Si falla, es un decimal.
                return new BigFraction(Double.parseDouble(s));
            }
        }
    }

    /**
     * Convierte una cadena (ej. "1", "3/4", "-1.5") a double.
     * Usado si el usuario NO selecciona cálculo exacto.
     */
    private static double parseToDouble(String s) {
        s = s.trim();
        // Permite comas como separador decimal para flexibilidad
        s = s.replace(",", ".");
        if (s.contains("/")) {
            String[] parts = s.split("/");
            if (parts.length != 2) throw new NumberFormatException("Formato de fracción inválido para double: " + s);
            return Double.parseDouble(parts[0].trim()) / Double.parseDouble(parts[1].trim());
        } else {
            return Double.parseDouble(s);
        }
    }

    /**
     * Lógica de resolución interna usando BigFraction para máxima precisión.
     */
    private static SolveResult solveWithFractions(BigFraction[][] M) {
        int n = M.length;
        StringBuilder steps = new StringBuilder("=== Resolución con Fracciones Exactas (Gauss–Jordan) ===\n\n");
        steps.append("Matriz Aumentada Inicial:\n").append(printMatrix(M)).append("\n");

        int pivotRow = 0;
        for (int col = 0; col < n && pivotRow < n; col++) {
            // Pivoting parcial
            int maxRow = pivotRow;
            for (int i = pivotRow + 1; i < n; i++) {
                if (M[i][col].abs().compareTo(M[maxRow][col].abs()) > 0) {
                    maxRow = i;
                }
            }

            // Intercambiar filas
            if (maxRow != pivotRow) {
                BigFraction[] temp = M[pivotRow];
                M[pivotRow] = M[maxRow];
                M[maxRow] = temp;
                steps.append(String.format("Fila %d ↔ Fila %d\n", pivotRow + 1, maxRow + 1));
                steps.append(printMatrix(M)).append("\n");
            }

            // Si el pivote es cero, esta columna no puede ser pivoteada
            if (M[pivotRow][col].equals(BigFraction.ZERO)) {
                steps.append("Columna ").append(col + 1).append(" sin pivote válido, se continúa.\n");
                continue;
            }

            // Normalizar la fila del pivote (hacer que el pivote sea 1)
            BigFraction pivot = M[pivotRow][col];
            if (!pivot.equals(BigFraction.ONE)) {
                steps.append(String.format("Fila %d = Fila %d / (%s)\n", pivotRow + 1, pivotRow + 1, pivot.reduce()));
                for (int j = col; j <= n; j++) {
                    M[pivotRow][j] = M[pivotRow][j].divide(pivot);
                }
                steps.append(printMatrix(M)).append("\n");
            }

            // Eliminar otras entradas en la columna del pivote
            for (int i = 0; i < n; i++) {
                if (i != pivotRow) {
                    BigFraction factor = M[i][col];
                    if (!factor.equals(BigFraction.ZERO)) {
                        steps.append(String.format("Fila %d = Fila %d - (%s) * Fila %d\n", i + 1, i + 1, factor.reduce(), pivotRow + 1));
                        for (int j = col; j <= n; j++) {
                            M[i][j] = M[i][j].subtract(factor.multiply(M[pivotRow][j]));
                        }
                        steps.append(printMatrix(M)).append("\n");
                    }
                }
            }
            pivotRow++;

        }

        // Análisis y clasificación del sistema
        int rankA = 0;
        for (int i = 0; i < n; i++) {
            boolean allZero = true;
            for (int j = 0; j < n; j++) {
                if (!M[i][j].equals(BigFraction.ZERO)) {
                    allZero = false;
                    break;
                }
            }
            if (allZero) {
                if (!M[i][n].equals(BigFraction.ZERO)) {
                    steps.append("Se encontró una inconsistencia en la fila ").append(i + 1).append(" (0 = k ≠ 0).\n");
                    return new SolveResult("Sistema Incompatible (Sin solución)\nRango(A) = " + rankA + ", Rango(A|b) = " + (rankA + 1), steps.toString());
                }
            } else {
                rankA++;
            }
        }

        if (rankA < n) {
            return new SolveResult(
                    "Sistema Compatible Indeterminado (Infinitas soluciones)\n" +
                            "Rango(A) = Rango(A|b) = " + rankA + " < Nº de incógnitas",
                    steps.toString()
            );
        }

        // Sistema Compatible Determinado
        StringBuilder sol = new StringBuilder("Sistema Compatible Determinado\nSoluciones:\n");
        for (int i = 0; i < n; i++) {
            // Usamos .reduce() para simplificar las fracciones finales
            sol.append(String.format("x%d = %s\n", i + 1, M[i][n].reduce().toString()));
        }
        return new SolveResult(sol.toString(), steps.toString());
    }

    /**
     * Lógica de resolución interna usando doubles (punto flotante).
     */
    private static SolveResult solveWithDoubles(double[][] M) {
        int n = M.length;
        StringBuilder steps = new StringBuilder("=== Resolución con Aproximación Decimal (Gauss–Jordan) ===\n\n");
        steps.append("Matriz Aumentada Inicial:\n").append(printMatrix(M)).append("\n");

        int pivotRow = 0;
        for (int col = 0; col < n && pivotRow < n; col++) {
            // Pivoting parcial: encontrar la fila con el mayor valor absoluto en la columna actual
            int maxRow = pivotRow;
            for (int i = pivotRow + 1; i < n; i++) {
                if (Math.abs(M[i][col]) > Math.abs(M[maxRow][col])) {
                    maxRow = i;
                }
            }

            // Intercambiar filas si es necesario
            if (maxRow != pivotRow) {
                double[] temp = M[pivotRow];
                M[pivotRow] = M[maxRow];
                M[maxRow] = temp;
                steps.append(String.format("Fila %d ↔ Fila %d\n", pivotRow + 1, maxRow + 1));
                steps.append(printMatrix(M)).append("\n");
            }

            // Si el pivote es cercano a cero, esta columna no puede ser pivoteada
            if (Math.abs(M[pivotRow][col]) < EPSILON) {
                steps.append("Columna ").append(col + 1).append(" sin pivote válido (cercano a cero), se continúa.\n");
                continue;
            }

            // Normalizar la fila del pivote (hacer que el pivote sea 1)
            double pivot = M[pivotRow][col];
            if (Math.abs(pivot - 1.0) > EPSILON) {
                steps.append(String.format("Fila %d = Fila %d / (%.4f)\n", pivotRow + 1, pivotRow + 1, pivot));
                for (int j = col; j <= n; j++) {
                    M[pivotRow][j] /= pivot;
                }
                steps.append(printMatrix(M)).append("\n");
            }

            // Eliminar otras entradas en la columna del pivote
            for (int i = 0; i < n; i++) {
                if (i != pivotRow) {
                    double factor = M[i][col];
                    if (Math.abs(factor) > EPSILON) {
                        steps.append(String.format("Fila %d = Fila %d - (%.4f) * Fila %d\n", i + 1, i + 1, factor, pivotRow + 1));
                        for (int j = col; j <= n; j++) {
                            M[i][j] -= factor * M[pivotRow][j];
                        }
                        steps.append(printMatrix(M)).append("\n");
                    }
                }
            }
            pivotRow++;
        }

        // Análisis y clasificación del sistema
        int rankA = 0;
        for (int i = 0; i < n; i++) {
            boolean allZero = true;
            for (int j = 0; j < n; j++) {
                if (Math.abs(M[i][j]) > EPSILON) {
                    allZero = false;
                    break;
                }
            }
            if (allZero) {
                if (Math.abs(M[i][n]) > EPSILON) {
                    steps.append("Se encontró una inconsistencia en la fila ").append(i + 1).append(" (0 ≈ k ≠ 0).\n");
                    return new SolveResult("Sistema Incompatible (Sin solución)\nRango(A) = " + rankA + ", Rango(A|b) = " + (rankA + 1), steps.toString());
                }
            } else {
                rankA++;
            }
        }

        if (rankA < n) {
            return new SolveResult(
                    "Sistema Compatible Indeterminado (Infinitas soluciones)\n" +
                            "Rango(A) = Rango(A|b) = " + rankA + " < Nº de incógnitas",
                    steps.toString()
            );
        }

        // Sistema Compatible Determinado
        StringBuilder sol = new StringBuilder("Sistema Compatible Determinado\nSoluciones (Aproximadas):\n");
        for (int i = 0; i < n; i++) {
            // M[i][n] contiene el resultado, ya que la matriz está en forma reducida por filas
            sol.append(String.format("x%d ≈ %.4f\n", i + 1, M[i][n]));
        }
        return new SolveResult(sol.toString(), steps.toString());
    }

    // Método de impresión para BigFraction (sin cambios)
    private static String printMatrix(BigFraction[][] M) {
        // ... (Tu implementación original para BigFraction) ...
        StringBuilder sb = new StringBuilder();
        int[] maxWidths = new int[M[0].length];
        // Calcular anchos máximos
        for (int j = 0; j < M[0].length; j++) {
            for (int i = 0; i < M.length; i++) {
                int len = M[i][j].reduce().toString().length(); // Usamos .reduce() para la impresión
                if (len > maxWidths[j]) {
                    maxWidths[j] = len;
                }
            }
        }

        for (BigFraction[] row : M) {
            sb.append("[ ");
            for (int j = 0; j < row.length; j++) {
                // Usamos .reduce() para asegurar que la fracción se muestre simplificada
                sb.append(String.format("%" + (maxWidths[j] + 1) + "s", row[j].reduce().toString()));
            }
            sb.append(" ]\n");
        }
        return sb.toString();
    }

    // Método de impresión para double (Corregido para formato)
    private static String printMatrix(double[][] M) {
        StringBuilder sb = new StringBuilder();
        // Usamos un formato fijo (ej. 6 decimales) para que sea legible
        for (double[] row : M) {
            sb.append("[ ");
            for (double val : row) {
                // Añadimos espacios para una mejor alineación
                sb.append(String.format("%10.4f ", val));
            }
            sb.append(" ]\n");
        }
        return sb.toString();
    }
}
