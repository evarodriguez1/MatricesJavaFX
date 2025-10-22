package com.calculos.controllers;

import com.calculos.models.GaussianaSolver;
import com.calculos.utils.InputValidator;
import com.calculos.utils.PopupManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GaussianaController {

    @FXML private TextField mediaField;
    @FXML private TextField varianzaField;
    @FXML private ComboBox<String> calculationType;
    @FXML private GridPane inputFieldsPane; // Contenedor dinámico
    @FXML private Label aLabel;
    @FXML private TextField aField;
    @FXML private Label bLabel;
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

        // ✅ IMPLEMENTACIÓN: Listener mejorado para controlar la visibilidad
        calculationType.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (newValue == null) {
                inputFieldsPane.setVisible(false);
                inputFieldsPane.setManaged(false);
                return;
            }

            inputFieldsPane.setVisible(true);
            inputFieldsPane.setManaged(true);

            boolean isRange = typeMap.getOrDefault(newValue, 0) == 3;

            aLabel.setText(isRange ? "Límite inferior 'a':" : "Punto 'a':");
            aField.setPromptText(isRange ? "ej: -1.5" : "ej: 2.0");

            bLabel.setVisible(isRange);
            bLabel.setManaged(isRange);
            bField.setVisible(isRange);
            bField.setManaged(isRange);

            if (!isRange) {
                bField.clear();
            }
        });
    }

    @FXML
    private void onSolve() {
        try {
            double media = InputValidator.parseDouble(mediaField.getText(), "Media (μ)");
            double varianza = InputValidator.parseDouble(varianzaField.getText(), "Varianza (σ²)");
            if (varianza < 0) {
                throw new IllegalArgumentException("La varianza (σ²) no puede ser un número negativo.");
            }
            double desviacion = Math.sqrt(varianza);

            String selectedType = calculationType.getValue();
            if (selectedType == null) {
                throw new IllegalArgumentException("Por favor, selecciona un tipo de cálculo.");
            }
            int option = typeMap.get(selectedType);

            double a = InputValidator.parseDouble(aField.getText(), isRange(option) ? "Límite inferior 'a'" : "Punto 'a'");
            Double b = isRange(option) ? InputValidator.parseDouble(bField.getText(), "Límite superior 'b'") : null;

            if (b != null && b < a) {
                // Mejora de UX: intercambia automáticamente si b < a
                double temp = a; a = b; b = temp;
            }

            String resultText;
            if (desviacion == 0 && a != media) { // Si la desviación es 0, la probabilidad es 1 solo si el punto es la media, sino 0.
                resultText = "Con una varianza de cero, la distribución es un único punto en la media (μ).\n" +
                        "La probabilidad de cualquier valor distinto a la media es 0.";
            } else {
                double z1 = GaussianaSolver.calculateZScore(a, media, desviacion);
                Double z2 = (b != null) ? GaussianaSolver.calculateZScore(b, media, desviacion) : null;
                double prob;

                switch (option) {
                    case 1: prob = GaussianaSolver.solveNormalAcumulada(a, media, desviacion, false); break;
                    case 2: prob = GaussianaSolver.solveNormalAcumulada(a, media, desviacion, true); break;
                    case 3: prob = GaussianaSolver.solveNormalRango(a, b, media, desviacion); break;
                    default: throw new IllegalStateException("Opción de cálculo inesperada.");
                }
                resultText = formatResult(a, b, prob, desviacion, z1, z2);
            }

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

        // ✅ CORRECCIÓN: Restablecer estado inicial
        calculationType.setValue(null);
        inputFieldsPane.setVisible(false);
        inputFieldsPane.setManaged(false);
    }

    private boolean isRange(int option) {
        return option == 3;
    }

    private String formatResult(double a, Double b, double prob, double desviacion, double z1, Double z2) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Desviación Estándar (σ) calculada: %,.4f\n\n", desviacion));
        sb.append("--- Resultado del Cálculo ---\n");

        if (z2 != null && b != null) { // Caso Rango
            sb.append(String.format("Z-score de 'a' (%.2f): %,.4f\n", a, z1));
            sb.append(String.format("Z-score de 'b' (%.2f): %,.4f\n\n", b, z2));
            sb.append(String.format("Probabilidad P(%.2f ≤ X ≤ %.2f): \n%,.8f (%.4f %%)", a, b, prob, prob * 100));
        } else { // Casos < o >
            String operator = typeMap.get(calculationType.getValue()) == 1 ? "≤" : "≥";
            sb.append(String.format("Z-score de 'a' (%.2f): %,.4f\n\n", a, z1));
            sb.append(String.format("Probabilidad P(X %s %.2f): \n%,.8f (%.4f %%)", operator, a, prob, prob * 100));
        }

        return sb.toString();
    }
}