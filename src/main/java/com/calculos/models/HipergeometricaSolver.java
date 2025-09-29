package com.calculos.models;

import java.math.BigInteger;

public class HipergeometricaSolver {

    public static BigInteger factorial(int num) {
        if (num < 0) return BigInteger.ZERO;
        BigInteger resultado = BigInteger.ONE;
        for (int i = 2; i <= num; i++) {
            resultado = resultado.multiply(BigInteger.valueOf(i));
        }
        return resultado;
    }

    public static BigInteger combinatorio(int n, int k) {
        if (k < 0 || k > n) {
            return BigInteger.ZERO;
        }
        BigInteger numerador = factorial(n);
        BigInteger denominador = factorial(k).multiply(factorial(n - k));
        return numerador.divide(denominador);
    }

    // --- Lógica Principal de Cálculo ---

    /**
     * Calcula la probabilidad puntual P(X = x).
     */
    public static double probabilidadHipergeometrica(int N, int K, int n, int x) {
        // Fórmula: [ C(K, x) * C(N-K, n-x) ] / C(N, n)

        BigInteger exitos_combinatorio = combinatorio(K, x);
        BigInteger fracasos_combinatorio = combinatorio(N - K, n - x);
        BigInteger total_combinatorio = combinatorio(N, n);

        if (total_combinatorio.equals(BigInteger.ZERO)) {
            return 0.0;
        }

        return exitos_combinatorio.multiply(fracasos_combinatorio).doubleValue() / total_combinatorio.doubleValue();
    }

    /**
     * Calcula la probabilidad acumulada para un rango [desde, hasta].
     */
    public static double solveHipergeometricaRange(int N, int K, int n, int desde, int hasta) throws IllegalArgumentException {
        // Validaciones de entrada
        if (N <= 0 || K < 0 || K > N || n < 0 || n > N) {
            throw new IllegalArgumentException("Parámetros N, K, n no válidos.");
        }
        // Validaciones del rango de éxitos
        if (desde < 0 || hasta > n || hasta > K || desde > hasta) {
            throw new IllegalArgumentException(String.format("El rango de éxitos (%d a %d) es inválido para una muestra de %d con %d éxitos totales.", desde, hasta, n, K));
        }

        double total = 0;
        for (int k = desde; k <= hasta; k++) {
            // Se comprueba la validez interna del combinatorio
            if (k <= K && (n - k) <= (N - K)) {
                total += probabilidadHipergeometrica(N, K, n, k);
            }
        }
        return total;
    }

    public static double getEsperanza(int N, int K, int n) {
        if (N == 0) return 0.0; // Evitar división por cero.
        return n * ((double) K / N);
    }

    public static double getVarianza(int N, int K, int n) {
        if (N <= 1) return 0.0; // La varianza es cero si no se puede formar la muestra.
        double p = (double) K / N;
        double factorCorreccion = (double) (N - n) / (N - 1);
        return n * p * (1 - p) * factorCorreccion;
    }
}