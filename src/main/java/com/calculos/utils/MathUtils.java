package com.calculos.utils;

/**
 * Clase de utilidad para operaciones matemáticas extendidas.
 * Proporciona funcionalidades que no están directamente disponibles en la clase
 * {@link java.lang.Math}, como la conversión de números decimales a fracciones
 * y el cálculo del Máximo Común Divisor (GCD).
 * Esta clase está diseñada como una utilidad estática, por lo que no es instanciable.
 */
public final class MathUtils {

    private static final int MAX_DENOMINATOR = 1000; // Límite para evitar fracciones enormes y poco intuitivas.
    private static final double EPSILON = 1.0E-9;    // Tolerancia para la comparación de doubles.

    /**
     * Constructor privado para prevenir la instanciación de esta clase de utilidad.
     */
    private MathUtils() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada.");
    }

    /**
     * Convierte un número double a su representación fraccional más simple.
     * Si el número es un entero, lo devuelve como un String simple.
     * Utiliza un algoritmo de fracciones continuas para encontrar la mejor aproximación racional.
     *
     * @param value El número double a convertir.
     * @return Un String representando la fracción (ej: "3/2", "-5", "7/8").
     */
    public static String toFraction(double value) {
        // Manejar casos especiales para una salida limpia
        if (Double.isNaN(value)) return "Indefinido";
        if (Double.isInfinite(value)) return "Infinito";
        if (Math.abs(value) < EPSILON) return "0";
        if (Math.abs(value - Math.round(value)) < EPSILON) {
            return String.valueOf((long) Math.round(value));
        }

        // --- Algoritmo de Fracciones Continuas ---
        double h1 = 1, h2 = 0;
        double k1 = 0, k2 = 1;
        boolean isNegative = value < 0;
        if (isNegative) {
            value = -value;
        }

        double b = value;
        do {
            long a = (long) Math.floor(b);
            double aux = h1; h1 = a * h1 + h2; h2 = aux;
            aux = k1; k1 = a * k1 + k2; k2 = aux;
            b = 1 / (b - a);
        } while (Math.abs(value - h1 / k1) > value * EPSILON && k1 <= MAX_DENOMINATOR);

        long numerator = (long) h1;
        long denominator = (long) k1;

        if (isNegative) {
            numerator = -numerator;
        }

        return numerator + "/" + denominator;
    }

    /**
     * Calcula el Máximo Común Divisor (GCD) de dos números largos.
     * Este es un método auxiliar que podría ser usado por otros algoritmos de fracciones,
     * aunque el método de fracciones continuas no lo necesita directamente.
     * Implementa el algoritmo euclidiano, que es altamente eficiente.
     *
     * @param a El primer número.
     * @param b El segundo número.
     * @return El GCD de a y b.
     */
    public static long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}