package com.calculos.models;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class PoissonSolver {

    // --- Métodos Auxiliares ---

    public static BigInteger factorial(int num) {
        if (num < 0) {
            return BigInteger.ZERO; // O lanzar IllegalArgumentException
        }
        BigInteger resultado = BigInteger.ONE;
        for (int i = 2; i <= num; i++) {
            resultado = resultado.multiply(BigInteger.valueOf(i));
        }
        return resultado;
    }

    // --- Lógica Principal de Cálculo ---

    /**
     * Calcula la probabilidad puntual P(X = x).
     */
    public static double probabilidadPoisson(double lambda, int x) {
        if (lambda <= 0 || x < 0) return 0.0;

        MathContext mc = new MathContext(30);

        // Fórmula: (λ^x * e^(-λ)) / x!
        BigDecimal lambdaElevadoX = BigDecimal.valueOf(lambda).pow(x, mc);
        BigDecimal factorial = new BigDecimal(factorial(x));
        BigDecimal expNegLambda = new BigDecimal(Math.exp(-lambda));

        BigDecimal result = lambdaElevadoX.multiply(expNegLambda, mc).divide(factorial, mc);

        return result.doubleValue();
    }

    /**
     * Calcula la probabilidad acumulada para un rango [desde, hasta].
     */
    public static double solvePoissonRange(double lambda, int desde, int hasta) throws IllegalArgumentException {
        if (lambda <= 0) {
            throw new IllegalArgumentException("El promedio (lambda) debe ser positivo.");
        }
        if (desde < 0 || desde > hasta) {
            throw new IllegalArgumentException("Rango de eventos inválido (desde < 0 o desde > hasta).");
        }

        double total = 0;
        // La distribución de Poisson no tiene un límite superior fijo,
        // pero la suma se detiene en 'hasta' si se busca un rango.
        for (int x = desde; x <= hasta; x++) {
            total += probabilidadPoisson(lambda, x);
        }
        return total;
    }

    /**
     * Calcula la probabilidad P(X >= desde) usando el complemento (1 - P(X <= desde-1)).
     */
    public static double solvePoissonMinimo(double lambda, int desde) throws IllegalArgumentException {
        if (lambda <= 0) {
            throw new IllegalArgumentException("El promedio (lambda) debe ser positivo.");
        }
        if (desde <= 0) return 1.0; // P(X >= 0) es 1

        // Calcular P(X <= desde-1)
        double acumuladaMenores = 0;
        // En teoría, el bucle va de 0 a infinito, pero la probabilidad puntual se vuelve insignificante
        // rápidamente. Para la GUI, la suma debe ser precisa. Usamos un límite alto (ej: 20*lambda)
        // para la cola, pero si se usa el complemento, solo sumamos hasta 'desde-1'.

        // Sumamos hasta el valor 'desde-1'
        for (int i = 0; i < desde; i++) {
            acumuladaMenores += probabilidadPoisson(lambda, i);
        }

        // Se usa 1 - P(X < desde) = 1 - P(X <= desde-1)
        return 1.0 - acumuladaMenores;
    }
}