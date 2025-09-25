package com.calculos.models;

import java.util.*;
import java.util.stream.Collectors;

public class EstadisticasSolver {

    // La lista de números debe estar previamente ordenada para los cálculos de posición.

    // --- Medidas de Posición ---

    public static double media(List<Double> nums) {
        // Usamos Double::doubleValue para asegurar el tipo primitivo en el stream
        return nums.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public static double mediana(List<Double> nums) {
        if (nums.isEmpty()) return 0;
        int n = nums.size();
        if (n % 2 == 0) {
            // Promedio de los dos valores centrales
            return (nums.get(n / 2 - 1) + nums.get(n / 2)) / 2.0;
        } else {
            // Valor central
            return nums.get(n / 2);
        }
    }

    public static List<Double> moda(List<Double> nums) {
        if (nums.isEmpty()) return List.of();

        Map<Double, Long> frecuencia = nums.stream()
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));

        // Encuentra la frecuencia máxima
        long max = frecuencia.values().stream().max(Long::compare).orElse(0L);

        // Si la frecuencia máxima es 1 (todos únicos) o 0 (lista vacía), no hay moda.
        if (max <= 1) return List.of();

        // Retorna todos los valores con esa frecuencia máxima
        return frecuencia.entrySet().stream()
                .filter(e -> e.getValue() == max)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // Cuartil (usando el método de interpolación que usaste en tu código original)
    public static double cuartil(List<Double> nums, int cuartil) {
        if (nums.isEmpty()) return 0;
        if (cuartil <= 0 || cuartil >= 4) throw new IllegalArgumentException("El cuartil debe ser 1, 2 o 3.");

        int n = nums.size();
        // Posición = (k/4) * (n + 1)
        double pos = (cuartil / 4.0) * (n + 1);
        int indexInferior = (int) Math.floor(pos) - 1;

        if (indexInferior < 0) return nums.get(0);
        if (indexInferior >= n - 1) return nums.get(n - 1);

        double frac = pos - Math.floor(pos); // Parte decimal para interpolación

        return nums.get(indexInferior) + frac * (nums.get(indexInferior + 1) - nums.get(indexInferior));
    }

    // --- Medidas de Dispersión ---

    public static double rango(List<Double> nums) {
        if (nums.isEmpty()) return 0;
        return nums.get(nums.size() - 1) - nums.get(0);
    }

    public static double varianza(List<Double> nums) {
        if (nums.size() < 2) return 0;
        double media = media(nums);
        // Varianza poblacional: suma(xi - media)^2 / N
        return nums.stream().mapToDouble(n -> Math.pow(n - media, 2)).sum() / nums.size();
    }

    public static double desviacionEstandar(List<Double> nums) {
        return Math.sqrt(varianza(nums));
    }

    public static double rangoIntercuartilico(List<Double> nums) {
        if (nums.size() < 2) return 0;
        return cuartil(nums, 3) - cuartil(nums, 1);
    }

    public static double coeficienteVariacion(List<Double> nums) {
        double media = media(nums);
        if (media == 0) return 0;
        return (desviacionEstandar(nums) / media) * 100;
    }

    // Coeficiente de Curtosis (G2)
    public static double coeficienteCurtosis(List<Double> numeros) {
        int n = numeros.size();
        if (n < 4) return 0; // Se necesitan al menos 4 datos para una estimación fiable

        double media = media(numeros);
        double s = desviacionEstandar(numeros);

        if (s == 0) return 0; // Desviación cero, no hay dispersión

        // Suma de (xi – media)^4 (Momento central de orden 4)
        double sumaCuarta = numeros.stream()
                .mapToDouble(x -> Math.pow(x - media, 4))
                .sum();

        // Fórmula de Curtosis = [ Momento Central 4 / Desv.Est. ^ 4 ] - 3
        return ((sumaCuarta / n) / Math.pow(s, 4)) - 3;
    }

    // --- Frecuencias ---

    public static Map<Double, Long> getFrecuenciasAbsolutas(List<Double> nums) {
        if (nums.isEmpty()) return Collections.emptyMap();

        // Agrupa, cuenta y ordena por valor (clave)
        return nums.stream()
                .collect(Collectors.groupingBy(n -> n, TreeMap::new, Collectors.counting()));
    }
}