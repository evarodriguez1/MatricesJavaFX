package com.calculos.controllers;

import com.calculos.models.GaussianaSolver;
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

public class GaussianaController {

    @FXML private TextField mediaField;
    @FXML private TextField varianzaField;
    @FXML private ComboBox<String> calculationType;
    @FXML private TextField aField;
    @FXML private TextField bField;
    @FXML private TextArea resultArea;

    private final Map<String, Integer> typeMap = new LinkedHashMap<>();
    private List<TextField> allFields;

    @FXML
    public void initialize() {
        allFields = Arrays.asList(mediaField, varianzaField, aField, bField);

        typeMap.put("P(X ≤ a) - Menor o Igual", 1);
        typeMap.put("P(X ≥ a) - Mayor o Igual", 2);
        typeMap.put("P(a ≤ X ≤ b) - Dentro de un Rango", 3);
        calculationType.setItems(FXCollections.observableArrayList(typeMap.keySet()));
        calculationType.getSelectionModel().selectFirst();

        // Listener para habilitar/deshabilitar bField (campo de rango)
        calculationType.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            boolean isRange = typeMap.getOrDefault(newValue, 0) == 3;
            bField.setDisable(!isRange);
            if (!isRange) {
                bField.clear();
            }
        });
    }

    @FXML
    private void onSolve() {
        clearErrorStyles();
        try {
            // 1. Validar los parámetros de la distribución
            double media = getValidatedDouble(mediaField, "Media (μ)");
            double varianza = getValidatedDouble(varianzaField, "Varianza (σ²)");
            if (varianza < 0) {
                throw new IllegalArgumentException("La varianza (σ²) no puede ser un número negativo.");
            }
            double desviacion = Math.sqrt(varianza);

            // 2. Determinar los límites a y b
            String selectedType = calculationType.getValue();
            int option = typeMap.getOrDefault(selectedType, 0);

            double a = getValidatedDouble(aField, "Punto 'a'");
            Double b = (option == 3) ? getValidatedDouble(bField, "Punto 'b'") : null;

            if (b != null && b < a) {
                // Si el usuario ingresa un rango inválido, lo corregimos automáticamente en lugar de lanzar un error.
                // Es una mejora de UX.
                double temp = a; a = b; b = temp;
            }

            // 3. Calcular los Z-Scores y la probabilidad
            String resultText;
            if (desviacion == 0) {
                resultText = "Con una varianza de cero, la distribución es un único punto (la media).\nLa probabilidad es 0 o 1, un caso determinista.";
            } else {
                double z1 = GaussianaSolver.calculateZScore(a, media, desviacion);
                Double z2 = (b != null) ? GaussianaSolver.calculateZScore(b, media, desviacion) : null;
                double prob;

                switch (option) {
                    case 1 -> prob = GaussianaSolver.solveNormalAcumulada(a, media, desviacion, false);
                    case 2 -> prob = GaussianaSolver.solveNormalAcumulada(a, media, desviacion, true);
                    case 3 -> prob = GaussianaSolver.solveNormalRango(a, b, media, desviacion);
                    default -> throw new IllegalStateException("Opción de cálculo inesperada.");
                }
                resultText = formatResult(a, b, prob, desviacion, z1, z2);
            }

            // 4. Mostrar el resultado
            resultArea.setText(resultText);

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

    private String formatResult(double a, Double b, double prob, double desviacion, double z1, Double z2) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("↳ La desviación estándar (σ o s) calculada es: %,.4f\n\n", desviacion));
        sb.append("--- Resultado ---\n");

        if (z2 != null && b != null) { // Caso de Rango
            sb.append(String.format("Z-score para el límite inferior (a = %.4f): %,.4f\n", a, z1));
            sb.append(String.format("Z-score para el límite superior (b = %.4f): %,.4f\n\n", b, z2));
            sb.append(String.format("La probabilidad P(%,.4f ≤ X ≤ %,.4f) es: %,.5f\n", a, b, prob));
        } else { // Casos de un solo punto
            String operator = typeMap.get(calculationType.getValue()).equals(1) ? "≤" : "≥";
            sb.append(String.format("Z-score para el punto (a = %.4f): %,.4f\n\n", a, z1));
            sb.append(String.format("La probabilidad P(X %s %,.4f) es: %,.5f\n", operator, a, prob));
        }

        sb.append(String.format("↳ Esto representa un %,.2f%% de probabilidad.", prob * 100));

        return sb.toString();
    }
}