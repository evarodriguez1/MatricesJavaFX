package com.calculos.models;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Representa una distribución de probabilidad Hipergeométrica.
 * Encapsula los parámetros 'N' (población), 'K' (éxitos en población) y 'n' (tamaño de muestra)
 * para proporcionar una API orientada a objetos. El objeto es inmutable y valida sus
 * parámetros en la creación, garantizando un estado consistente.
 * Utiliza memoización (caché) para optimizar el cálculo de la PMF completa,
 * lo que es crucial para la visualización gráfica de la distribución.
 */
public final class HypergeometricDistribution {

    private final int N, K, n;
    // Memoización (Caché) para los resultados de P(X=k)
    private final Map<Integer, Double> pmfCache = new HashMap<>();

    /**
     * Constructor para una nueva distribución Hipergeométrica.
     * @param N El tamaño total de la población (debe ser > 0).
     * @param K El número total de éxitos en la población (debe estar en [0, N]).
     * @param n El tamaño de la muestra extraída (debe estar en [0, N]).
     * @throws IllegalArgumentException si los parámetros no son válidos.
     */
    public HypergeometricDistribution(int N, int K, int n) throws IllegalArgumentException {
        if (N <= 0 || K < 0 || n < 0 || K > N || n > N) {
            throw new IllegalArgumentException(String.format(
                    "Parámetros de distribución inválidos. Se recibió N=%d, K=%d, n=%d.", N, K, n));
        }
        this.N = N;
        this.K = K;
        this.n = n;
    }

    /**
     * Calcula la probabilidad acumulada para un rango P(desde <= X <= hasta).
     * @param from El número mínimo de éxitos a considerar (inclusivo).
     * @param to El número máximo de éxitos a considerar (inclusivo).
     * @return La probabilidad acumulada en el rango.
     * @throws IllegalArgumentException si el rango no es válido.
     */
    public double getProbabilityRange(int from, int to) throws IllegalArgumentException {
        // Valida la coherencia lógica del rango.
        if (from > to || from < 0) {
            throw new IllegalArgumentException(String.format(
                    "Rango de éxitos inválido: [%d, %d]. 'Desde' debe ser <= 'Hasta' y no negativo.", from, to));
        }

        // El número máximo de éxitos nunca puede ser mayor que la muestra o el total de éxitos.
        int maxPossibleSuccesses = Math.min(this.n, this.K);
        if (to > maxPossibleSuccesses) {
            throw new IllegalArgumentException(String.format(
                    "El valor 'hasta' (%d) no puede superar el máximo de éxitos posibles (%d).", to, maxPossibleSuccesses));
        }

        double totalProbability = 0.0;
        for (int k = from; k <= to; k++) {
            totalProbability += getProbabilityAt(k);
        }
        return totalProbability;
    }

    /**
     * Calcula la probabilidad puntual P(X = k).
     * Utiliza memoización (caché) para optimizar cálculos repetidos.
     * @param k El número exacto de éxitos.
     * @return La probabilidad P(X = k).
     */
    public double getProbabilityAt(int k) {
        // El rango válido para k en la hipergeométrica es max(0, n+K-N) <= k <= min(n, K).
        // Esta comprobación más simple cubre todos los casos donde el combinatorio sería cero.
        if (k < 0 || k > this.K || k > this.n || (this.n - k) > (this.N - this.K)) {
            return 0.0;
        }
        return pmfCache.computeIfAbsent(k, this::calculateProbabilityAt);
    }

    /**
     * Genera la Función de Masa de Probabilidad (PMF) completa para la gráfica.
     * @return Un mapa donde la clave es el número de éxitos (k) y el valor es P(X=k).
     */
    public Map<Integer, Double> getFullDistribution() {
        int maxSuccesses = Math.min(n, K);
        if (pmfCache.size() != (maxSuccesses + 1)) {
            for (int k = 0; k <= maxSuccesses; k++) {
                getProbabilityAt(k); // Puebla la caché
            }
        }
        return new HashMap<>(pmfCache);
    }

    // --- Métodos Estadísticos ---

    public double getMean() {
        return (double) this.n * ((double) this.K / this.N);
    }

    public double getVariance() {
        if (this.N <= 1) return 0.0; // Evita división por cero en el factor de corrección.
        double p = (double) this.K / this.N;
        double correctionFactor = (double) (this.N - this.n) / (this.N - 1);
        return (double) this.n * p * (1 - p) * correctionFactor;
    }

    // --- Motor de Cálculo (Privado y Estático) ---

    private double calculateProbabilityAt(int k) {
        BigInteger totalCombinations = combinatorio(this.N, this.n);
        if (totalCombinations.equals(BigInteger.ZERO)) {
            return 0.0;
        }
        BigInteger successCombinations = combinatorio(this.K, k);
        BigInteger failureCombinations = combinatorio(this.N - this.K, this.n - k);

        return successCombinations.multiply(failureCombinations)
                .doubleValue() / totalCombinations.doubleValue();
    }

    private static BigInteger combinatorio(int n, int k) {
        if (k < 0 || k > n) {
            return BigInteger.ZERO;
        }
        if (k > n / 2) {
            k = n - k;
        }

        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < k; i++) {
            result = result.multiply(BigInteger.valueOf(n - i))
                    .divide(BigInteger.valueOf(i + 1));
        }
        return result;
    }
}