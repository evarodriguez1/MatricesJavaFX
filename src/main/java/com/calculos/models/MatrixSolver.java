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

    private static final double EPSILON = 1e-9;

    // --- MÉTODOS PÚBLICOS PRINCIPALES ---

    public static SolveResult solveGaussJordan(String[][] A_str, String[] B_str, boolean useFractions) {
        try {
            if (useFractions) {
                int n = A_str.length;
                BigFraction[][] M_frac = new BigFraction[n][n + 1];
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) M_frac[i][j] = parseToBigFraction(A_str[i][j]);
                    M_frac[i][n] = parseToBigFraction(B_str[i]);
                }
                return solveWithFractions(M_frac);
            } else {
                int n = A_str.length;
                double[][] M_double = new double[n][n + 1];
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) M_double[i][j] = parseToDouble(A_str[i][j]);
                    M_double[i][n] = parseToDouble(B_str[i]);
                }
                return solveWithDoubles(M_double);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error en la conversión de entrada: " + e.getMessage());
        }
    }

    public static class VerificationResult {
        public final String equationResults;
        public final boolean isCorrect;

        public VerificationResult(String equationResults, boolean isCorrect) {
            this.equationResults = equationResults;
            this.isCorrect = isCorrect;
        }
    }

    public static VerificationResult checkSolution(String[][] A_str, String[] B_str, String X_str, boolean useFractions) {
        int n = A_str.length;
        String[] xValues = parseSolutions(X_str);
        if (xValues.length != n) {
            return new VerificationResult("Error: La cantidad de soluciones encontradas no coincide con el tamaño del sistema.", false);
        }

        StringBuilder sb = new StringBuilder("=== Corroboración por Sustitución ===\n\n");

        try {
            if (useFractions) {
                BigFraction[][] A = new BigFraction[n][n];
                BigFraction[] B = new BigFraction[n];
                BigFraction[] X = new BigFraction[n];
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) A[i][j] = parseToBigFraction(A_str[i][j]);
                    B[i] = parseToBigFraction(B_str[i]);
                    X[i] = parseToBigFraction(xValues[i]);
                }
                boolean allCorrect = true;
                for (int i = 0; i < n; i++) {
                    BigFraction result = BigFraction.ZERO;
                    for (int j = 0; j < n; j++) {
                        result = result.add(A[i][j].multiply(X[j]));
                    }
                    boolean correct = result.reduce().equals(B[i].reduce());
                    if (!correct) allCorrect = false;
                    sb.append(String.format("Ecuación %d: (%s)\n", i + 1, formatFractionRow(A[i], X)));
                    sb.append(String.format("↳ Resultado: %s | Valor Esperado: %s | ¿Coincide?: %s\n\n",
                            result.reduce().toString().replace(" / ", "/"),
                            B[i].reduce().toString().replace(" / ", "/"),
                            correct ? "✅ SÍ" : "❌ NO"));
                }
                sb.append(allCorrect ? "¡CORRECTO! La solución satisface todas las ecuaciones con cálculo exacto." : "¡ERROR! La solución no satisface todas las ecuaciones.");
                return new VerificationResult(sb.toString(), allCorrect);
            } else {
                double[][] A = new double[n][n];
                double[] B = new double[n];
                double[] X = new double[n];
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) A[i][j] = parseToDouble(A_str[i][j]);
                    B[i] = parseToDouble(B_str[i]);
                    X[i] = parseToDouble(xValues[i]);
                }
                boolean allCorrect = true;
                for (int i = 0; i < n; i++) {
                    double result = 0.0;
                    for (int j = 0; j < n; j++) result += A[i][j] * X[j];
                    boolean correct = Math.abs(result - B[i]) < EPSILON;
                    if (!correct) allCorrect = false;
                    sb.append(String.format("Ecuación %d:\n", i + 1));
                    sb.append(String.format("↳ Resultado: %.4f | Valor Esperado: %.4f | ¿Coincide?: %s\n\n",
                            result, B[i], correct ? "✅ SÍ" : "❌ NO"));
                }
                sb.append(allCorrect ? "¡CORRECTO! La solución satisface todas las ecuaciones con aproximación decimal." : "¡ERROR! La solución no satisface todas las ecuaciones.");
                return new VerificationResult(sb.toString(), allCorrect);
            }
        } catch (Exception e) {
            return new VerificationResult("Error durante la verificación: " + e.getMessage(), false);
        }
    }

    // --- LÓGICA DE RESOLUCIÓN INTERNA ---

    private static SolveResult solveWithFractions(BigFraction[][] M) {
        int n = M.length;
        StringBuilder steps = new StringBuilder("=== Resolución con Fracciones Exactas (Gauss-Jordan) ===\n\n");
        steps.append("1. Matriz Aumentada Inicial:\n").append(printMatrix(M)).append("\n");
        int pivotRow = 0;
        for (int col = 0; col < n && pivotRow < n; col++) {
            steps.append(String.format("--- PASO: Procesando Columna %d ---\n", col + 1));
            int maxRow = pivotRow;
            for (int i = pivotRow + 1; i < n; i++) {
                if (M[i][col].abs().compareTo(M[maxRow][col].abs()) > 0) maxRow = i;
            }
            steps.append(String.format("Buscando pivote en la Columna %d a partir de la Fila %d.\n", col + 1, pivotRow + 1));
            steps.append(String.format("El valor absoluto máximo (%s) se encontró en la Fila %d.\n", M[maxRow][col].abs().reduce().toString().replace(" / ","/"), maxRow + 1));
            if (maxRow != pivotRow) {
                steps.append(String.format("Intercambiando Fila %d con Fila %d para usar el pivote más grande.\n", pivotRow + 1, maxRow + 1));
                BigFraction[] temp = M[pivotRow];
                M[pivotRow] = M[maxRow];
                M[maxRow] = temp;
                steps.append(printMatrix(M)).append("\n");
            }
            if (M[pivotRow][col].equals(BigFraction.ZERO)) {
                steps.append("El pivote es Cero. No se puede procesar esta columna, se continúa con la siguiente.\n\n");
                continue;
            }
            BigFraction pivot = M[pivotRow][col];
            if (!pivot.equals(BigFraction.ONE)) {
                steps.append(String.format("Normalizando la Fila %d para que el pivote sea 1.\nOperación: F%d = F%d / (%s)\n", pivotRow + 1, pivotRow + 1, pivotRow + 1, pivot.reduce().toString().replace(" / ", "/")));
                for (int j = col; j <= n; j++) M[pivotRow][j] = M[pivotRow][j].divide(pivot);
                steps.append(printMatrix(M)).append("\n");
            }
            for (int i = 0; i < n; i++) {
                if (i != pivotRow) {
                    BigFraction factor = M[i][col];
                    if (!factor.equals(BigFraction.ZERO)) {
                        steps.append(String.format("Eliminando el elemento de la Fila %d en la columna del pivote.\nOperación: F%d = F%d - (%s) * F%d\n", i + 1, i + 1, i + 1, factor.reduce().toString().replace(" / ", "/"), pivotRow + 1));
                        for (int j = col; j <= n; j++) M[i][j] = M[i][j].subtract(factor.multiply(M[pivotRow][j]));
                        steps.append(printMatrix(M)).append("\n");
                    }
                }
            }
            pivotRow++;
        }
        steps.append("--- ANÁLISIS DEL SISTEMA ---\n");
        int rankA = 0;
        for (int i = 0; i < n; i++) {
            boolean allZero = true;
            for (int j = 0; j < n; j++) if (!M[i][j].equals(BigFraction.ZERO)) { allZero = false; break; }
            if (allZero) {
                if (!M[i][n].equals(BigFraction.ZERO)) {
                    steps.append("Se encontró una inconsistencia en la Fila ").append(i + 1).append(" (0 = k donde k ≠ 0).\n");
                    return new SolveResult("Sistema Incompatible (Sin solución)\nRango(A) = " + rankA + ", Rango(A|b) = " + (rankA + 1), steps.toString());
                }
            } else rankA++;
        }
        if (rankA < n) {
            steps.append("El rango de la matriz es menor que el número de incógnitas.\n");
            return new SolveResult("Sistema Compatible Indeterminado (Infinitas soluciones)\nRango(A) = Rango(A|b) = " + rankA + " < Nº de incógnitas", steps.toString());
        }
        steps.append("La matriz está en forma escalonada reducida. Se extraen las soluciones.\n");
        StringBuilder sol = new StringBuilder("Sistema Compatible Determinado\nSoluciones:\n");
        for (int i = 0; i < n; i++) sol.append(String.format("x%d = %s\n", i + 1, M[i][n].reduce().toString().replace(" / ", "/")));
        return new SolveResult(sol.toString(), steps.toString());
    }

    private static SolveResult solveWithDoubles(double[][] M) {
        int n = M.length;
        StringBuilder steps = new StringBuilder("=== Resolución con Aproximación Decimal (Gauss-Jordan) ===\n\n");
        steps.append("1. Matriz Aumentada Inicial:\n").append(printMatrix(M)).append("\n");
        int pivotRow = 0;
        for (int col = 0; col < n && pivotRow < n; col++) {
            steps.append(String.format("--- PASO: Procesando Columna %d ---\n", col + 1));
            int maxRow = pivotRow;
            for (int i = pivotRow + 1; i < n; i++) if (Math.abs(M[i][col]) > Math.abs(M[maxRow][col])) maxRow = i;
            steps.append(String.format("Buscando pivote en la Columna %d a partir de la Fila %d.\n", col + 1, pivotRow + 1));
            steps.append(String.format("El valor absoluto máximo (%.2f) se encontró en la Fila %d.\n", M[maxRow][col], maxRow + 1));
            if (maxRow != pivotRow) {
                steps.append(String.format("Intercambiando Fila %d con Fila %d para usar el pivote más grande.\n", pivotRow + 1, maxRow + 1));
                double[] temp = M[pivotRow];
                M[pivotRow] = M[maxRow];
                M[maxRow] = temp;
                steps.append(printMatrix(M)).append("\n");
            }
            if (Math.abs(M[pivotRow][col]) < EPSILON) {
                steps.append("El pivote es cercano a Cero. No se puede procesar esta columna, se continúa.\n\n");
                continue;
            }
            double pivot = M[pivotRow][col];
            if (Math.abs(pivot - 1.0) > EPSILON) {
                steps.append(String.format("Normalizando la Fila %d para que el pivote sea 1.\nOperación: F%d = F%d / (%.4f)\n", pivotRow + 1, pivotRow + 1, pivotRow + 1, pivot));
                for (int j = col; j <= n; j++) M[pivotRow][j] /= pivot;
                steps.append(printMatrix(M)).append("\n");
            }
            for (int i = 0; i < n; i++) {
                if (i != pivotRow) {
                    double factor = M[i][col];
                    if (Math.abs(factor) > EPSILON) {
                        steps.append(String.format("Eliminando el elemento de la Fila %d en la columna del pivote.\nOperación: F%d = F%d - (%.4f) * F%d\n", i + 1, i + 1, i + 1, factor, pivotRow + 1));
                        for (int j = col; j <= n; j++) M[i][j] -= factor * M[pivotRow][j];
                        steps.append(printMatrix(M)).append("\n");
                    }
                }
            }
            pivotRow++;
        }
        steps.append("--- ANÁLISIS DEL SISTEMA ---\n");
        int rankA = 0;
        for (int i = 0; i < n; i++) {
            boolean allZero = true;
            for (int j = 0; j < n; j++) if (Math.abs(M[i][j]) > EPSILON) { allZero = false; break; }
            if (allZero) {
                if (Math.abs(M[i][n]) > EPSILON) {
                    steps.append("Se encontró una inconsistencia en la Fila ").append(i + 1).append(" (0 ≈ k donde k ≠ 0).\n");
                    return new SolveResult("Sistema Incompatible (Sin solución)\nRango(A) = " + rankA + ", Rango(A|b) = " + (rankA + 1), steps.toString());
                }
            } else rankA++;
        }
        if (rankA < n) {
            steps.append("El rango de la matriz es menor que el número de incógnitas.\n");
            return new SolveResult("Sistema Compatible Indeterminado (Infinitas soluciones)\nRango(A) = Rango(A|b) = " + rankA + " < Nº de incógnitas", steps.toString());
        }
        steps.append("La matriz está en forma escalonada reducida. Se extraen las soluciones.\n");
        StringBuilder sol = new StringBuilder("Sistema Compatible Determinado\nSoluciones (Aproximadas):\n");
        for (int i = 0; i < n; i++) sol.append(String.format("x%d ≈ %.4f\n", i + 1, M[i][n]));
        return new SolveResult(sol.toString(), steps.toString());
    }

    // --- MÉTODOS DE AYUDA (Helpers) ---

    private static String centerString(String text, int width) {
        if (text.length() >= width) return text;
        int totalPadding = width - text.length();
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
    }

    private static String printMatrix(BigFraction[][] M) {
        StringBuilder sb = new StringBuilder();
        int nCols = M[0].length;
        int[] maxWidths = new int[nCols];

        for (int j = 0; j < nCols; j++) {
            for (BigFraction[] row : M) {
                // ✅ CORRECCIÓN: Usa la longitud del string compacto ("2/5") para el cálculo del ancho
                String fractionStr = row[j].reduce().toString().replace(" / ", "/");
                int len = fractionStr.length();
                if (len > maxWidths[j]) maxWidths[j] = len;
            }
        }
        for (BigFraction[] row : M) {
            sb.append("[");
            for (int j = 0; j < nCols; j++) {
                if (j > 0) sb.append(" ");
                if (j == nCols - 1) sb.append("| ");
                // ✅ CORRECCIÓN: Reemplaza los espacios antes de centrar y mostrar
                String text = row[j].reduce().toString().replace(" / ", "/");
                sb.append(centerString(text, maxWidths[j]));
            }
            sb.append(" ]\n");
        }
        return sb.toString();
    }

    private static String printMatrix(double[][] M) {
        StringBuilder sb = new StringBuilder();
        int nCols = M[0].length;
        for (double[] row : M) {
            sb.append("[");
            for (int j = 0; j < nCols; j++) {
                if (j == nCols - 1) sb.append(" |");
                sb.append(String.format("%8.2f", row[j]));
            }
            sb.append(" ]\n");
        }
        return sb.toString();
    }

    private static String[] parseSolutions(String X_str) {
        if (X_str == null || X_str.isEmpty()) return new String[0];
        String anchor = "Soluciones (Aproximadas):";
        int start = X_str.indexOf(anchor);
        if (start == -1) { anchor = "Soluciones:"; start = X_str.indexOf(anchor); }
        if (start == -1) return new String[0];
        String solutionBlock = X_str.substring(start + anchor.length());
        return solutionBlock.lines()
                .filter(line -> line.contains("="))
                .map(line -> line.substring(line.indexOf('=') + 1).replace('≈', ' ').trim())
                .toArray(String[]::new);
    }

    private static String formatFractionRow(BigFraction[] A_row, BigFraction[] X) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < A_row.length; j++) {
            sb.append(String.format("(%s) * (%s)",
                    A_row[j].reduce().toString().replace(" / ", "/"),
                    X[j].reduce().toString().replace(" / ", "/")));
            if (j < A_row.length - 1) {
                sb.append(" + ");
            }
        }
        return sb.toString();
    }

    private static BigFraction parseToBigFraction(String s) {
        s = s.trim();
        if (s.contains("/")) {
            String[] parts = s.split("/");
            if (parts.length != 2) throw new NumberFormatException("Formato de fracción inválido: " + s);
            try {
                BigInteger num = new BigInteger(parts[0].trim());
                BigInteger den = new BigInteger(parts[1].trim());
                if (den.equals(BigInteger.ZERO)) throw new ArithmeticException("Denominador cero");
                return new BigFraction(num, den);
            } catch (NumberFormatException e) {
                return new BigFraction(parseToDouble(s));
            }
        } else {
            try {
                return new BigFraction(new BigInteger(s));
            } catch (NumberFormatException e) {
                return new BigFraction(Double.parseDouble(s));
            }
        }
    }

    private static double parseToDouble(String s) {
        s = s.trim().replace(",", ".");
        if (s.contains("/")) {
            String[] parts = s.split("/");
            if (parts.length != 2) throw new NumberFormatException("Formato de fracción inválido para double: " + s);
            return Double.parseDouble(parts[0].trim()) / Double.parseDouble(parts[1].trim());
        } else {
            return Double.parseDouble(s);
        }
    }
}