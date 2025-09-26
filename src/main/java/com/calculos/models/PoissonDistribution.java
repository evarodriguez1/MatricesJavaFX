package com.calculos.models;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Representa una distribución de probabilidad de Poisson.
 *
 * Encapsula el parámetro 'lambda' (λ), la tasa promedio de ocurrencia de eventos.
 * El objeto es inmutable, valida lambda en su creación y ofrece una API robusta
 * para cálculos de probabilidad, utilizando BigDecimal para máxima precisión.
 *
 * Utiliza memoización para optimizar el rendimiento de los cálculos.
 *
 * @author Tu Nombre (Equipo de Desarrollo)
 */
public final class PoissonDistribution {

    private final double lambda;
    // Precisión para los cálculos con BigDecimal
    private static final MathContext PRECISION = new MathContext(30);
    // Caché para los resultados de P(X=k)
    private final Map<Integer, Double> pmfCache = new HashMap<>();

    /**
     * Constructor para una nueva distribución de Poisson.
     * @param lambda La tasa promedio de ocurrencias (λ). Debe ser un número positivo.
     * @throws IllegalArgumentException si lambda no es positivo.
     */
    public PoissonDistribution(double lambda) throws IllegalArgumentException {
        if (lambda <= 0) {
            throw new IllegalArgumentException("La tasa promedio (lambda) debe ser un número positivo.");
        }
        this.lambda = lambda;
    }

    /**
     * Calcula la probabilidad acumulada P(desde <= X <= hasta).
     * @param from El número mínimo de ocurrencias (inclusivo).
     * @param to El número máximo de ocurrencias (inclusivo).
     * @return La probabilidad acumulada en el rango.
     * @throws IllegalArgumentException si el rango es inválido.
     */
    public double getProbabilityRange(int from, int to) throws IllegalArgumentException {
        if (from > to || from < 0) {
            throw new IllegalArgumentException(String.format("Rango inválido [%d, %d].", from, to));
        }
        double totalProbability = 0.0;
        for (int k = from; k <= to; k++) {
            totalProbability += getProbabilityAt(k);
        }
        return totalProbability;
    }

    /**
     * Calcula la probabilidad P(X >= k) usando el método del complemento para eficiencia.
     * @param k El número mínimo de ocurrencias (inclusivo).
     * @return La probabilidad P(X >= k).
     */
    public double getComplementaryProbability(int k) {
        if (k <= 0) return 1.0;
        double cumulativeLower = 0.0;
        for (int i = 0; i < k; i++) {
            cumulativeLower += getProbabilityAt(i);
        }
        return 1.0 - cumulativeLower;
    }

    /**
     * Calcula la probabilidad puntual P(X = k). Utiliza memoización.
     * @param k El número exacto de ocurrencias.
     * @return La probabilidad P(X = k).
     */
    public double getProbabilityAt(int k) {
        if (k < 0) return 0.0;
        return pmfCache.computeIfAbsent(k, this::calculateProbabilityAt);
    }

    /**
     * Genera la PMF hasta un punto donde la probabilidad se vuelve insignificante,
     * adecuado para visualización gráfica.
     * @return Un mapa con los pares (k, P(X=k)).
     */
    public Map<Integer, Double> getDistributionForGraphing() {
        // En Poisson, el dominio es infinito. Calculamos hasta un punto razonable
        // donde la cola de la distribución es despreciable. Una heurística común
        // es la media + un número de desviaciones estándar (sqrt(lambda)).
        int limit = (int) Math.ceil(this.lambda + 6 * Math.sqrt(this.lambda));

        // Asegura que todos los valores hasta el límite estén en la caché
        for (int k = 0; k <= limit; k++) {
            getProbabilityAt(k);
        }
        return new HashMap<>(pmfCache);
    }

    // --- Métodos Estadísticos ---

    /**
     * @return La media (Valor Esperado) de la distribución, que es igual a lambda.
     */
    public double getMean() {
        return this.lambda;
    }

    /**
     * @return La varianza de la distribución, que también es igual a lambda.
     */
    public double getVariance() {
        return this.lambda;
    }

    // --- Motor de Cálculo (Privado) ---

    private double calculateProbabilityAt(int k) {
        // Fórmula: (λ^k * e^(-λ)) / k!
        BigDecimal bigLambda = BigDecimal.valueOf(this.lambda);

        BigDecimal lambdaPowerK = bigLambda.pow(k, PRECISION);
        BigDecimal expNegLambda = BigDecimal.valueOf(Math.exp(-this.lambda));
        BigDecimal kFactorial = new BigDecimal(factorial(k));

        return lambdaPowerK.multiply(expNegLambda, PRECISION)
                .divide(kFactorial, PRECISION)
                .doubleValue();
    }

    private static BigInteger factorial(int num) {
        // factorial(0) es 1.
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= num; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }
}