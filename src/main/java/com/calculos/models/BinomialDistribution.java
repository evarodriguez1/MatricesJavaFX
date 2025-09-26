package com.calculos.models;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Representa una distribución de probabilidad Binomial.
 *
 * Encapsula los parámetros 'n' (ensayos) y 'p' (probabilidad de éxito)
 * para proporcionar una API orientada a objetos para realizar cálculos.
 * El objeto es inmutable y valida sus parámetros en la creación, garantizando
 * un estado consistente y seguro para todos los cálculos.
 *
 * Utiliza memoización (caché) para optimizar cálculos repetitivos de la
 * distribución de probabilidad completa.
 *
 * @author Tu Nombre (Equipo de Desarrollo)
 */
public final class BinomialDistribution {

    private final int n;
    private final double p;
    // Memoización (Caché): Almacena los resultados de la PMF (P(X=k)) para evitar recalcularlos.
    private final Map<Integer, Double> pmfCache = new HashMap<>();

    /**
     * Constructor para una nueva distribución Binomial.
     * @param n El número total de ensayos (debe ser > 0).
     * @param p La probabilidad de éxito en un solo ensayo (debe estar en [0, 1]).
     * @throws IllegalArgumentException si los parámetros no son válidos.
     */
    public BinomialDistribution(int n, double p) throws IllegalArgumentException {
        if (n <= 0) {
            throw new IllegalArgumentException("El número de ensayos (n) debe ser un entero positivo.");
        }
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("La probabilidad de éxito (p) debe estar en el rango [0, 1].");
        }
        this.n = n;
        this.p = p;
    }

    /**
     * Calcula la probabilidad acumulada para un rango P(desde <= X <= hasta).
     * @param from El número mínimo de éxitos a considerar (inclusivo).
     * @param to El número máximo de éxitos a considerar (inclusivo).
     * @return La probabilidad acumulada en el rango.
     * @throws IllegalArgumentException si el rango no es válido.
     */
    public double getProbabilityRange(int from, int to) throws IllegalArgumentException {
        if (from > to || from < 0 || to > this.n) {
            throw new IllegalArgumentException(String.format(
                    "Rango de éxitos inválido: [%d, %d]. Debe estar dentro de [0, %d].", from, to, this.n
            ));
        }
        double totalProbability = 0.0;
        for (int k = from; k <= to; k++) {
            totalProbability += getProbabilityAt(k);
        }
        return totalProbability;
    }

    /**
     * Calcula la probabilidad puntual P(X = k).
     * Este método utiliza memoización para mejorar el rendimiento en cálculos repetidos.
     * @param k El número exacto de éxitos.
     * @return La probabilidad P(X = k).
     */
    public double getProbabilityAt(int k) {
        if (k < 0 || k > n) {
            return 0.0; // Fuera del dominio, la probabilidad es 0.
        }
        // Revisa la caché primero
        return pmfCache.computeIfAbsent(k, this::calculateProbabilityAt);
    }

    /**
     * Genera la Función de Masa de Probabilidad (PMF) completa.
     * @return Un mapa donde la clave es el número de éxitos (k) y el valor es P(X=k).
     */
    public Map<Integer, Double> getFullDistribution() {
        if (pmfCache.size() != (n + 1)) { // Si la caché no está completa
            for (int k = 0; k <= n; k++) {
                getProbabilityAt(k); // Llama para poblar la caché
            }
        }
        return new HashMap<>(pmfCache); // Devuelve una copia para mantener la inmutabilidad
    }

    // --- Métodos Estadísticos ---

    public double getMean() {
        return this.n * this.p;
    }

    public double getVariance() {
        return this.n * this.p * (1 - this.p);
    }

    // --- Métodos Auxiliares de Cálculo (Privados) ---

    private double calculateProbabilityAt(int k) {
        BigInteger combinations = combinatorio(this.n, k);
        return combinations.doubleValue() * Math.pow(this.p, k) * Math.pow(1 - this.p, this.n - k);
    }

    private static BigInteger combinatorio(int n, int k) {
        if (k < 0 || k > n) {
            return BigInteger.ZERO;
        }
        if (k > n / 2) {
            k = n - k;
        }

        BigInteger resultado = BigInteger.ONE;
        for (int i = 0; i < k; i++) {
            resultado = resultado.multiply(BigInteger.valueOf(n - i))
                    .divide(BigInteger.valueOf(i + 1));
        }
        return resultado;
    }
}