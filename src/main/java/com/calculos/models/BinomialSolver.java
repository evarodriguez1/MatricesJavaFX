package com.calculos.models; // Mismo paquete de modelos

import java.math.BigInteger;

public class BinomialSolver {

    // ----------------------------------------------------
    // Métodos de Soporte
    // ----------------------------------------------------

    public static BigInteger factorial(int num) {
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

    /**
     * Calcula la probabilidad puntual P(X=k).
     */
    public static double probabilidadBinomial(int n, int k, double p) {
        if (k < 0 || k > n) {
            return 0;
        }
        BigInteger combinatoria = combinatorio(n, k);
        return combinatoria.doubleValue() * Math.pow(p, k) * Math.pow(1 - p, n - k);
    }

    // ----------------------------------------------------
    // Lógica principal de cálculo
    // ----------------------------------------------------

    /**
     * Calcula la probabilidad acumulada para un rango [desde, hasta].
     * @param n Número total de ensayos.
     * @param p Probabilidad de éxito.
     * @param desde Valor mínimo de éxitos (inclusivo).
     * @param hasta Valor máximo de éxitos (inclusivo).
     * @return Probabilidad P(desde <= X <= hasta).
     */
    public static double solveBinomialRange(int n, double p, int desde, int hasta) throws IllegalArgumentException {
        if (n <= 0 || p < 0 || p > 1) {
            throw new IllegalArgumentException("Parámetros n y p no válidos.");
        }
        if (desde < 0 || hasta > n || desde > hasta) {
            throw new IllegalArgumentException("Rango de éxitos (desde, hasta) no válido. Debe estar entre 0 y " + n);
        }

        double total = 0;
        for (int x = desde; x <= hasta; x++) {
            total += probabilidadBinomial(n, x, p);
        }
        return total;
    }

    // Métodos para la Media y Varianza
    public static double getEsperanza(int n, double p) {
        return n * p;
    }

    public static double getVarianza(int n, double p) {
        return n * p * (1 - p);
    }
}