package com.calculos.models;

import java.util.Objects;

/**
 * Representa una distribución de probabilidad Normal (Gaussiana).
 *
 * Encapsula los parámetros 'media' (μ) y 'desviación estándar' (σ) para
 * proporcionar una API orientada a objetos. El objeto es inmutable y valida
 * sus parámetros en la creación.
 *
 * Internamente, utiliza una aproximación de Hastings para calcular la Función
 * de Distribución Acumulada (CDF) de la distribución normal estándar.
 *
 * @author Tu Nombre (Equipo de Desarrollo)
 */
public final class NormalDistribution {

    private final double mean;
    private final double standardDeviation;

    /**
     * Constructor para una nueva distribución Normal.
     * @param mean La media (μ) de la distribución.
     * @param standardDeviation La desviación estándar (σ) de la distribución. Debe ser positiva.
     * @throws IllegalArgumentException si la desviación estándar no es positiva.
     */
    public NormalDistribution(double mean, double standardDeviation) throws IllegalArgumentException {
        if (standardDeviation <= 0) {
            throw new IllegalArgumentException("La desviación estándar (σ) debe ser un número positivo.");
        }
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    /**
     * Calcula la probabilidad acumulada hasta un punto P(X <= x).
     * @param x El valor del cuantil.
     * @return La probabilidad acumulada.
     */
    public double getCumulativeProbability(double x) {
        double z = calculateZScore(x);
        return cdfHastings(z);
    }

    /**
     * Calcula la probabilidad de que la variable sea mayor que un punto P(X >= x).
     * Esto es el complemento de la probabilidad acumulada.
     * @param x El valor del cuantil.
     * @return La probabilidad P(X >= x).
     */
    public double getComplementaryProbability(double x) {
        return 1.0 - getCumulativeProbability(x);
    }

    /**
     * Calcula la probabilidad de que la variable se encuentre en un rango [a, b].
     * @param a El límite inferior del rango.
     * @param b El límite superior del rango.
     * @return La probabilidad P(a <= X <= b).
     */
    public double getRangeProbability(double a, double b) {
        // Asegurar que a <= b internamente
        if (a > b) {
            double temp = a;
            a = b;
            b = temp;
        }
        return getCumulativeProbability(b) - getCumulativeProbability(a);
    }

    /**
     * Calcula la densidad de probabilidad para un punto x (la altura de la curva).
     * @param x El valor en el eje x.
     * @return La altura de la curva de la campana en el punto x.
     */
    public double getDensity(double x) {
        double exponent = -0.5 * Math.pow((x - this.mean) / this.standardDeviation, 2);
        return (1.0 / (this.standardDeviation * Math.sqrt(2 * Math.PI))) * Math.exp(exponent);
    }

    /**
     * Convierte un valor 'x' de esta distribución a su Z-score correspondiente.
     * @param x El valor a estandarizar.
     * @return El Z-score.
     */
    public double calculateZScore(double x) {
        return (x - this.mean) / this.standardDeviation;
    }

    // --- Métodos de acceso a los parámetros ---

    public double getMean() {
        return this.mean;
    }

    public double getStandardDeviation() {
        return this.standardDeviation;
    }

    // --- Motor de Cálculo CDF (Privado y Estático) ---

    /**
     * Aproximación de la Función de Distribución Acumulada (CDF) Normal estándar
     * por el método de Hastings (Abramowitz & Stegun 26.2.17).
     * Es estático porque no depende del estado (media, desviación) de la instancia.
     * @param z El Z-score (valor estandarizado).
     * @return La probabilidad acumulada P(Z <= z).
     */
    private static double cdfHastings(double z) {
        boolean isNegative = z < 0;
        double absZ = Math.abs(z);

        // Coeficientes de la aproximación polinómica
        final double b0 = 0.2316419;
        final double b1 = 0.319381530;
        final double b2 = -0.356563782;
        final double b3 = 1.781477937;
        final double b4 = -1.821255978;
        final double b5 = 1.330274429;

        // La función de densidad de probabilidad estándar phi(z)
        double phi = Math.exp(-0.5 * absZ * absZ) / Math.sqrt(2 * Math.PI);

        double t = 1.0 / (1.0 + b0 * absZ);
        double polynomial = (((((b5 * t + b4) * t + b3) * t + b2) * t + b1) * t);

        double approx = 1.0 - phi * polynomial;

        return isNegative ? (1.0 - approx) : approx;
    }
}