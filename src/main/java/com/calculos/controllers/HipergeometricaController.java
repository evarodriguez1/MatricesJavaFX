package com.calculos.controllers;

import com.calculos.models.HipergeometricaSolver;
import com.calculos.utils.InputValidator;
import com.calculos.utils.PopupManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.LinkedHashMap;
import java.util.Map;

public class HipergeometricaController {

    @FXML private TextField nField;  // Población total (N)
    @FXML private TextField kTotalField; // Éxitos en la población (K)
    @FXML private TextField nSampleField; // Tamaño de la muestra (n)
    @FXML private ComboBox<String> calculationType;
    @FXML private TextField k1Field; // Límite 'k' o 'a'
    @FXML private TextField k2Field; // Límite 'b'
    @FXML private TextArea resultArea;

    private final Map<String, Integer> typeMap = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        // Opciones de cálculo
        typeMap.put("P(X = k) - Exacto", 1);
        typeMap.put("P(X ≤ k) - Como Máximo", 2);
        typeMap.put("P(X ≥ k) - Como Mínimo", 3);
        typeMap.put("P(a ≤ X ≤ b) - Rango", 4);

        calculationType.setItems(FXCollections.observableArrayList(typeMap.keySet()));
        calculationType.getSelectionModel().selectFirst();
    }

    @FXML
    private void onSolve() {
        try {
            // 1. Obtener y validar parámetros (deben ser enteros)
            int N = (int) validateIntegerInput(nField.getText(), "N (Población Total)");
            int K = (int) validateIntegerInput(kTotalField.getText(), "K (Éxitos Totales)");
            int n = (int) validateIntegerInput(nSampleField.getText(), "n (Tamaño Muestra)");

            // Validaciones básicas de la distribución
            if (N <= 0) throw new IllegalArgumentException("N debe ser positivo.");
            if (K < 0 || K > N) throw new IllegalArgumentException("K debe estar entre 0 y N.");
            if (n < 0 || n > N) throw new IllegalArgumentException("n debe estar entre 0 y N.");

            // 2. Obtener opción y límites
            String selectedType = calculationType.getValue();
            if (selectedType == null) {
                throw new IllegalArgumentException("Debe seleccionar un tipo de cálculo.");
            }
            int option = typeMap.get(selectedType);

            int desde, hasta;
            int k1 = (int) validateIntegerInput(k1Field.getText(), "Valor de k/a");

            switch (option) {
                case 1 -> desde = hasta = k1;
                case 2 -> { // Máximo P(X ≤ k)
                    desde = 0;
                    hasta = k1;
                }
                case 3 -> { // Mínimo P(X ≥ k)
                    desde = k1;
                    // El máximo de éxitos posible es min(n, K)
                    hasta = Math.min(n, K);
                }
                case 4 -> { // Rango P(a ≤ X ≤ b)
                    desde = k1;
                    int k2 = (int) validateIntegerInput(k2Field.getText(), "Valor de b");
                    hasta = k2;
                }
                default -> throw new IllegalArgumentException("Opción de cálculo no válida.");
            }

            // Validaciones de rango contra los límites de la distribución
            if (desde < 0 || hasta > n || hasta > K || desde > hasta) {
                throw new IllegalArgumentException(String.format("Rango de éxitos inválido. Valores deben ser >= 0, <= muestra (%d), y <= éxitos totales (%d).", n, K));
            }

            // 3. Calcular Probabilidad
            double totalProb = HipergeometricaSolver.solveHipergeometricaRange(N, K, n, desde, hasta);

            // 4. Formatear y mostrar resultado
            String resultText = formatHipergeometricaResult(option, desde, hasta, totalProb, N, K, n);
            resultArea.setText(resultText);

        } catch (IllegalArgumentException ex) {
            PopupManager.showError(ex.getMessage());
        } catch (Exception ex) {
            PopupManager.showError("Error desconocido al calcular la probabilidad hipergeométrica: " + ex.getMessage());
        }
    }

    private double validateIntegerInput(String text, String fieldName) {
        double value = InputValidator.parseDouble(text, fieldName);
        if (value % 1 != 0) {
            throw new IllegalArgumentException("El campo " + fieldName + " debe ser un número entero.");
        }
        return value;
    }

    private String formatHipergeometricaResult(int option, int desde, int hasta, double prob, int N, int K, int n) {
        StringBuilder sb = new StringBuilder();
        String probDescription;

        // 1. Descripción
        if (option == 1) probDescription = String.format("P(X = %d)", desde);
        else if (option == 2) probDescription = String.format("P(X ≤ %d)", hasta);
        else if (option == 3) probDescription = String.format("P(X ≥ %d)", desde);
        else probDescription = String.format("P(%d ≤ X ≤ %d)", desde, hasta);

        // 2. Media y Varianza (fórmulas)
        double media = n * (double) K / N;
        double factorCorreccion = (double) (N - n) / (N - 1);
        double varianza = n * ((double) K / N) * ((double) (N - K) / N) * factorCorreccion;

        sb.append("--- Probabilidad Hipergeométrica ---\n");
        sb.append(String.format("Población (N): %d, Éxitos Totales (K): %d, Muestra (n): %d\n", N, K, n));
        sb.append(String.format("Cálculo: %s\n", probDescription));
        sb.append(String.format("Resultado: %.10f\n", prob));
        sb.append(String.format("Porcentaje: %.2f%%\n", prob * 100));

        sb.append("\n--- Otros Datos ---\n");
        sb.append(String.format("Esperanza E[X]: %.4f\n", media));
        sb.append(String.format("Varianza Var[X]: %.4f\n", varianza));

        return sb.toString();
    }

    @FXML
    private void onClear() {
        nField.clear();
        kTotalField.clear();
        nSampleField.clear();
        k1Field.clear();
        k2Field.clear();
        resultArea.clear();
        calculationType.getSelectionModel().selectFirst();
    }
}