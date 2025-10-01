package com.calculos.controllers;

import com.calculos.models.HipergeometricaSolver;
import com.calculos.utils.InputValidator;
import com.calculos.utils.PopupManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HipergeometricaController {

    // Nombres de campo FXML
    @FXML private TextField nPopulationField; // Población total (N)
    @FXML private TextField kTotalField;      // Éxitos en la población (K)
    @FXML private TextField nSampleField;     // Tamaño de la muestra (n)
    @FXML private ComboBox<String> calculationType;
    @FXML private TextField k1Field;          // Límite 'k' o 'a'
    @FXML private TextField k2Field;          // Límite 'b'
    @FXML private TextArea resultArea;

    private final Map<String, Integer> typeMap = new LinkedHashMap<>();
    private List<TextField> allFields;

    @FXML
    public void initialize() {
        allFields = Arrays.asList(nPopulationField, kTotalField, nSampleField, k1Field, k2Field);

        typeMap.put("P(X = k) - Exacto", 1);
        typeMap.put("P(X ≤ k) - Como Máximo", 2);
        typeMap.put("P(X ≥ k) - Como Mínimo", 3);
        typeMap.put("P(a ≤ X ≤ b) - Rango", 4);

        calculationType.setItems(FXCollections.observableArrayList(typeMap.keySet()));
        calculationType.getSelectionModel().selectFirst();

        // Listener para gestionar el campo de rango k2
        calculationType.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            boolean isRange = typeMap.getOrDefault(newValue, 0) == 4;
            k2Field.setDisable(!isRange);
            if (!isRange) {
                k2Field.clear();
            }
        });
    }

    @FXML
    private void onSolve() {
        clearErrorStyles();
        try {
            // 1. Validar los parámetros de la población
            int N = getValidatedInteger(nPopulationField, "Población Total (N)");
            int K = getValidatedInteger(kTotalField, "Éxitos Totales (K)");
            int n = getValidatedInteger(nSampleField, "Tamaño Muestra (n)");

            if (N <= 0) throw new IllegalArgumentException("La Población Total (N) debe ser mayor que cero.");
            if (K > N) throw new IllegalArgumentException("Los Éxitos Totales (K) no pueden ser mayores que la Población (N).");
            if (n > N) throw new IllegalArgumentException("El Tamaño de la Muestra (n) no puede ser mayor que la Población (N).");

            // 2. Determinar el rango de cálculo
            String selectedType = calculationType.getValue();
            int option = typeMap.getOrDefault(selectedType, 0);

            int desde;
            int hasta;
            int k1 = getValidatedInteger(k1Field, "Valor de k (o a)");

            switch (option) {
                case 1 -> desde = hasta = k1;
                case 2 -> { desde = 0; hasta = k1; }
                case 3 -> { desde = k1; hasta = Math.min(n, K); }
                case 4 -> {
                    desde = k1;
                    hasta = getValidatedInteger(k2Field, "Valor de b");
                }
                default -> throw new IllegalArgumentException("Seleccione un tipo de cálculo válido.");
            }

            if (hasta < desde) throw new IllegalArgumentException("El valor de 'b' no puede ser menor que 'a' en un rango.");
            if (hasta > n) throw new IllegalArgumentException("El número de éxitos buscado ('b' o 'k') no puede ser mayor que la muestra (n).");
            if (hasta > K) throw new IllegalArgumentException("El número de éxitos buscado ('b' o 'k') no puede ser mayor que los éxitos totales (K).");


            // 3. Calcular la probabilidad
            double totalProb = HipergeometricaSolver.solveHipergeometricaRange(N, K, n, desde, hasta);

            // 4. Formatear y mostrar el resultado
            resultArea.setText(formatResult(selectedType, desde, hasta, totalProb, N, K, n));

        } catch (IllegalArgumentException e) {
            PopupManager.showError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            PopupManager.showError("Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    @FXML
    private void onClear() {
        allFields.forEach(TextField::clear);
        resultArea.clear();
        clearErrorStyles();
        calculationType.getSelectionModel().selectFirst();
    }

    // =============================================================
    // MÉTODOS DE AYUDA (Helpers)
    // =============================================================

    private int getValidatedInteger(TextField field, String fieldName) {
        double val = getValidatedDouble(field, fieldName);
        if (val % 1 != 0 || val < 0) {
            field.getStyleClass().add("error-field");
            throw new IllegalArgumentException(String.format("El campo '%s' debe ser un número entero no negativo.", fieldName));
        }
        return (int) val;
    }

    private double getValidatedDouble(TextField field, String fieldName) {
        try {
            double value = InputValidator.parseDouble(field.getText(), fieldName);
            field.getStyleClass().remove("error-field");
            return value;
        } catch (IllegalArgumentException e) {
            field.getStyleClass().add("error-field");
            throw e;
        }
    }

    private void clearErrorStyles() {
        allFields.forEach(field -> field.getStyleClass().remove("error-field"));
    }

    private String formatResult(String selectedType, int desde, int hasta, double prob, int N, int K, int n) {
        String probDescriptionText;
        String probType = selectedType.split(" - ")[1];

        switch(probType) {
            case "Exacto" -> probDescriptionText = String.format("obtener exactamente %d éxitos en la muestra", desde);
            case "Como Máximo" -> probDescriptionText = String.format("obtener como máximo %d éxitos en la muestra", hasta);
            case "Como Mínimo" -> probDescriptionText = String.format("obtener como mínimo %d éxitos en la muestra", desde);
            case "Rango" -> probDescriptionText = String.format("obtener entre %d y %d éxitos en la muestra", desde, hasta);
            default -> probDescriptionText = "";
        }

        double media = HipergeometricaSolver.getEsperanza(N, K, n);
        double varianza = HipergeometricaSolver.getVarianza(N, K, n);

        return String.format(
                """
                        --- Resultado ---
                        La probabilidad de %s es: %,.5f
                        ↳ Esto representa un %,.2f%% de probabilidad.
                        
                        --- Otros datos de interés ---
                        Valor Esperado (Media): %.4f
                        ↳ Es el número de éxitos promedio que esperarías en la muestra.
                        
                        Varianza: %.4f
                        ↳ Mide la dispersión de los resultados respecto a la media.""",
                probDescriptionText, prob, prob * 100, media, varianza
        );
    }
}