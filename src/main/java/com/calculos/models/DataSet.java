package com.calculos.models;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Representa un conjunto de datos para análisis estadístico.
 *
 * Esta clase encapsula una lista de números y proporciona una API rica para
 * realizar cálculos estadísticos sobre ellos. Al ser creada, la instancia valida
 * y ordena los datos, garantizando un estado interno consistente y fiable para
 * todos los cálculos posteriores.
 *
 * Este modelo orientado a objetos reemplaza el enfoque de Solver estático,
 * promoviendo un código más seguro, limpio y mantenible.
 *
 * @author Tu Nombre (Equipo de Desarrollo)
 */
public final class DataSet {

    private final List<Double> data; // Lista de datos, siempre ordenada e inmutable.
    private final int size;

    /**
     * Constructor que crea un conjunto de datos a partir de una lista de números.
     * La lista se ordena internamente para garantizar la validez de los cálculos.
     *
     * @param numbers La lista de números crudos. No debe ser nula.
     */
    public DataSet(List<Double> numbers) {
        // Hacemos una copia defensiva para garantizar la inmutabilidad
        // y ordenamos los datos una sola vez.
        this.data = numbers.stream()
                .sorted()
                .collect(Collectors.toUnmodifiableList());
        this.size = this.data.size();
    }

    // --- Medidas de Posición ---

    /**
     * @return La media aritmética del conjunto de datos.
     */
    public double getMean() {
        if (isEmpty()) return 0.0;
        return data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * @return La mediana (valor central) del conjunto de datos.
     */
    public double getMedian() {
        if (isEmpty()) return 0.0;
        if (size % 2 == 0) {
            return (data.get(size / 2 - 1) + data.get(size / 2)) / 2.0;
        } else {
            return data.get(size / 2);
        }
    }

    /**
     * @return Una lista de las modas (valores más frecuentes). Lista vacía si no hay moda.
     */
    public List<Double> getMode() {
        if (isEmpty()) return Collections.emptyList();

        Map<Double, Long> frequencies = data.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        long maxFrequency = frequencies.values().stream().max(Long::compare).orElse(0L);

        if (maxFrequency <= 1) return Collections.emptyList();

        return frequencies.entrySet().stream()
                .filter(entry -> entry.getValue() == maxFrequency)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Calcula el k-ésimo cuartil (1, 2, o 3) usando interpolación lineal.
     * @param quartile El número del cuartil a calcular (1, 2, o 3).
     * @return El valor del cuartil.
     */
    public double getQuartile(int quartile) {
        if (isEmpty() || quartile < 1 || quartile > 3) return 0.0;

        double pos = (quartile / 4.0) * (size + 1);
        int lowerIndex = (int) Math.floor(pos) - 1;
        double fraction = pos - Math.floor(pos);

        if (lowerIndex < 0) return data.get(0);
        if (lowerIndex >= size - 1) return data.get(size - 1);

        double lowerValue = data.get(lowerIndex);
        double upperValue = data.get(lowerIndex + 1);
        return lowerValue + fraction * (upperValue - lowerValue);
    }

    // --- Medidas de Dispersión ---

    /**
     * @return El rango (diferencia entre el máximo y el mínimo).
     */
    public double getRange() {
        if (isEmpty()) return 0.0;
        return data.get(size - 1) - data.get(0);
    }

    /**
     * @return La varianza poblacional.
     */
    public double getVariance() {
        if (size < 2) return 0.0;
        double mean = getMean();
        return data.stream().mapToDouble(n -> Math.pow(n - mean, 2)).sum() / size;
    }

    /**
     * @return La desviación estándar poblacional.
     */
    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }

    /**
     * @return El rango intercuartílico (Q3 - Q1).
     */
    public double getInterquartileRange() {
        if (size < 2) return 0.0;
        return getQuartile(3) - getQuartile(1);
    }

    /**
     * @return El coeficiente de variación (en porcentaje).
     */
    public double getCoefficientOfVariation() {
        double mean = getMean();
        if (mean == 0) return 0.0;
        return (getStandardDeviation() / mean) * 100;
    }

    /**
     * @return El coeficiente de curtosis (Exceso de Curtosis, G2).
     */
    public double getKurtosis() {
        if (size < 4) return 0.0;
        double mean = getMean();
        double stdDev = getStandardDeviation();
        if (stdDev == 0) return 0.0;

        double m4 = data.stream().mapToDouble(x -> Math.pow(x - mean, 4)).sum() / size;
        return (m4 / Math.pow(stdDev, 4)) - 3;
    }

    // --- Frecuencias y Datos Básicos ---

    /**
     * @return Un mapa de las frecuencias absolutas, ordenado por valor.
     */
    public Map<Double, Long> getAbsoluteFrequencies() {
        if (isEmpty()) return Collections.emptyMap();
        return data.stream().collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));
    }

    /**
     * @return La lista de datos ordenada.
     */
    public List<Double> getSortedData() {
        return data; // Ya es inmutable y ordenada
    }

    /**
     * @return El tamaño del conjunto de datos.
     */
    public int getSize() {
        return size;
    }

    /**
     * @return Verdadero si el conjunto de datos está vacío.
     */
    public boolean isEmpty() {
        return size == 0;
    }
}