package com.calculos.models;

public class GaussianaSolver {

    // La lógica de cálculo de probabilidad de la CDF de Hastings (tu aproximación)
    // Se mantiene como estaba, ya que es el núcleo del cálculo.
    private static double cdfHastings(double z) {
        boolean neg = z < 0;
        double absZ = Math.abs(z);
        double phi = Math.exp(-0.5 * absZ * absZ) / Math.sqrt(2 * Math.PI);
        double b0 = 0.2316419, b1 = 0.319381530, b2 = -0.356563782, b3 = 1.781477937, b4 = -1.821255978, b5 = 1.330274429;
        double t = 1.0 / (1.0 + b0 * absZ);
        double pol = (((((b5 * t + b4) * t + b3) * t + b2) * t + b1) * t);
        double approx = 1.0 - phi * pol;
        return neg ? (1.0 - approx) : approx;
    }

    /**
     * Calcula el Z-score.
     */
    public static double calculateZScore(double x, double media, double desviacion) {
        if (desviacion <= 0) {
            throw new IllegalArgumentException("La desviación estándar debe ser positiva para el cálculo normal.");
        }
        return (x - media) / desviacion;
    }

    /**
     * Calcula la probabilidad acumulada P(X <= x) o P(X >= x).
     * @param x El valor a evaluar.
     * @param media La media de la distribución.
     * @param desviacion La desviación estándar de la distribución.
     * @param esMayor True para P(X >= x), False para P(X <= x).
     * @return La probabilidad calculada.
     */
    public static double solveNormalAcumulada(double x, double media, double desviacion, boolean esMayor) {
        // La validación de desviación se hace en calculateZScore, pero la repetimos para claridad
        if (desviacion <= 0) {
            throw new IllegalArgumentException("La desviación estándar debe ser positiva.");
        }

        double z = calculateZScore(x, media, desviacion);
        double cdf = cdfHastings(z);

        return esMayor ? (1.0 - cdf) : cdf;
    }

    /**
     * Calcula la probabilidad en un rango P(a <= X <= b).
     * @param a Límite inferior.
     * @param b Límite superior.
     * @param media La media de la distribución.
     * @param desviacion La desviación estándar de la distribución.
     * @return La probabilidad P(a <= X <= b).
     */
    public static double solveNormalRango(double a, double b, double media, double desviacion) {
        if (desviacion <= 0) {
            throw new IllegalArgumentException("La desviación estándar debe ser positiva.");
        }

        // Asegurar que a <= b para el cálculo
        if (a > b) {
            double temp = a;
            a = b;
            b = temp;
        }

        double z1 = calculateZScore(a, media, desviacion);
        double z2 = calculateZScore(b, media, desviacion);

        return cdfHastings(z2) - cdfHastings(z1);
    }
}