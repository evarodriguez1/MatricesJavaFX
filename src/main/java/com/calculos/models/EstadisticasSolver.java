package com.calculos.models;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Clase de utilidad (Solver) que proporciona métodos estáticos para realizar cálculos
 * de estadística descriptiva.
 * No mantiene estado y todos sus métodos son puros (no modifican los datos de entrada).
 */
public class EstadisticasSolver {

    // --- Medidas de Posición ---

    /**
     * Calcula la media aritmética (promedio) de una lista de números.
     * @param nums La lista de números.
     * @return La media, o 0 si la lista está vacía.
     */
    public static double media(List<Double> nums) {
        if (nums.isEmpty()) return 0;
        return nums.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    /**
     * Calcula la mediana (valor central) de una lista de números.
     * La lista se ordena internamente para garantizar el cálculo correcto.
     * @param nums La lista de números (no necesita estar pre-ordenada).
     * @return La mediana, o 0 si la lista está vacía.
     */
    public static double mediana(List<Double> nums) {
        if (nums.isEmpty()) return 0;
        // Se crea una copia ordenada para no modificar la lista original.
        List<Double> sortedNums = new ArrayList<>(nums);
        Collections.sort(sortedNums);
        int n = sortedNums.size();
        if (n % 2 == 0) {
            return (sortedNums.get(n / 2 - 1) + sortedNums.get(n / 2)) / 2.0;
        } else {
            return sortedNums.get(n / 2);
        }
    }

    /**
     * Encuentra la moda (el valor o valores más frecuentes) en una lista.
     * @param nums La lista de números.
     * @return Una lista de modas. Si es bimodal, la lista contendrá dos valores. Si no hay moda, la lista estará vacía.
     */
    public static List<Double> moda(List<Double> nums) {
        if (nums.isEmpty()) return List.of();

        Map<Double, Long> frecuencia = nums.stream()
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));

        long maxFreq = frecuencia.values().stream().max(Long::compare).orElse(0L);

        if (maxFreq <= 1) return List.of();

        return frecuencia.entrySet().stream()
                .filter(e -> e.getValue() == maxFreq)
                .map(Map.Entry::getKey)
                .sorted() // Se ordena la salida para consistencia.
                .collect(Collectors.toList());
    }

    /**
     * Calcula un cuartil específico (Q1, Q2, Q3) usando interpolación lineal.
     * La lista se ordena internamente.
     * @param nums La lista de números (no necesita estar pre-ordenada).
     * @param cuartil El número del cuartil a calcular (1, 2, o 3).
     * @return El valor del cuartil.
     */
    public static double cuartil(List<Double> nums, int cuartil) {
        if (nums.isEmpty()) return 0;
        if (cuartil < 1 || cuartil > 3) throw new IllegalArgumentException("El cuartil debe ser 1, 2 o 3.");

        List<Double> sortedNums = new ArrayList<>(nums);
        Collections.sort(sortedNums);
        int n = sortedNums.size();

        double pos = (cuartil / 4.0) * (n + 1);
        int indexInferior = (int) Math.floor(pos) - 1;

        if (indexInferior < 0) return sortedNums.get(0);
        if (indexInferior >= n - 1) return sortedNums.get(n - 1);

        double frac = pos - Math.floor(pos);
        return sortedNums.get(indexInferior) + frac * (sortedNums.get(indexInferior + 1) - sortedNums.get(indexInferior));
    }

    // --- Medidas de Dispersión ---

    /**
     * Calcula el rango (diferencia entre el valor máximo y mínimo).
     * La lista se ordena internamente.
     * @param nums La lista de números (no necesita estar pre-ordenada).
     * @return El rango, o 0 si la lista está vacía.
     */
    public static double rango(List<Double> nums) {
        if (nums.isEmpty()) return 0;
        List<Double> sortedNums = new ArrayList<>(nums);
        Collections.sort(sortedNums);
        return sortedNums.get(sortedNums.size() - 1) - sortedNums.get(0);
    }

    /**
     * Calcula la varianza poblacional (σ²).
     * @param nums La lista de números.
     * @return La varianza, o 0 si hay menos de dos datos.
     */
    public static double varianza(List<Double> nums) {
        if (nums.size() < 2) return 0;
        double media = media(nums);
        return nums.stream().mapToDouble(n -> Math.pow(n - media, 2)).sum() / nums.size();
    }

    /**
     * Calcula la desviación estándar poblacional (σ), que es la raíz cuadrada de la varianza.
     * @param nums La lista de números.
     * @return La desviación estándar.
     */
    public static double desviacionEstandar(List<Double> nums) {
        return Math.sqrt(varianza(nums));
    }

    /**
     * Calcula el rango intercuartílico (RIQ), que es la diferencia entre el tercer y el primer cuartil.
     * @param nums La lista de números.
     * @return El rango intercuartílico.
     */
    public static double rangoIntercuartilico(List<Double> nums) {
        if (nums.size() < 2) return 0;
        return cuartil(nums, 3) - cuartil(nums, 1);
    }

    /**
     * Calcula el coeficiente de variación (CV), una medida de dispersión relativa.
     * El resultado se expresa en porcentaje.
     * @param nums La lista de números.
     * @return El coeficiente de variación, o 0 si la media es 0.
     */
    public static double coeficienteVariacion(List<Double> nums) {
        double media = media(nums);
        if (media == 0) return 0;
        return (desviacionEstandar(nums) / Math.abs(media)) * 100; // Usar valor absoluto de la media
    }

    /**
     * Calcula el coeficiente de curtosis de Fisher (g₂), que mide el "apuntamiento" de la distribución.
     * @param numeros La lista de números.
     * @return El coeficiente de curtosis.
     */
    public static double coeficienteCurtosis(List<Double> numeros) {
        int n = numeros.size();
        if (n < 4) return 0;

        double media = media(numeros);
        double s = desviacionEstandar(numeros);

        if (s == 0) return 0;

        double momentoCentral4 = numeros.stream()
                .mapToDouble(x -> Math.pow(x - media, 4))
                .sum() / n;

        return (momentoCentral4 / Math.pow(s, 4)) - 3;
    }

    // --- Frecuencias ---

    /**
     * Genera una tabla de frecuencias absolutas para una lista de datos.
     * @param nums La lista de números.
     * @return Un Map ordenado por valor donde la clave es el número y el valor es su frecuencia.
     */
    public static Map<Double, Long> getFrecuenciasAbsolutas(List<Double> nums) {
        if (nums.isEmpty()) return Collections.emptyMap();
        return nums.stream()
                .collect(Collectors.groupingBy(n -> n, TreeMap::new, Collectors.counting()));
    }
}