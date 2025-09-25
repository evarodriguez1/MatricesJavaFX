package com.calculos.controllers;

import com.calculos.models.GaussianaSolver;
import com.calculos.utils.InputValidator;
import com.calculos.utils.PopupManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.LinkedHashMap;
import java.util.Map;

public class GaussianaController {

    @FXML private TextField mediaField;
    @FXML private TextField varianzaField;
    @FXML private ComboBox<String> calculationType;
    @FXML private TextField aField; // Límite 'a'
    @FXML private TextField bField; // Límite 'b' (solo para rangos)
    @FXML private TextArea resultArea;

    private final Map<String, Integer> typeMap = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        // Opciones de cálculo de probabilidad
        typeMap.put("P(X ≤ a) - Menor o Igual", 1);
        typeMap.put("P(X ≥ a) - Mayor o Igual", 2);
        typeMap.put("P(a ≤ X ≤ b) - Dentro de un Rango", 3);

        calculationType.setItems(FXCollections.observableArrayList(typeMap.keySet()));
        calculationType.getSelectionModel().selectFirst();
    }

    @FXML
    private void onSolve() {
        try {
            // 1. Obtener y validar parámetros
            double media = InputValidator.parseDouble(mediaField.getText(), "Media (μ)");
            double varianza = InputValidator.parseDouble(varianzaField.getText(), "Varianza (σ²)");

            if (varianza < 0) {
                throw new IllegalArgumentException("La varianza no puede ser negativa.");
            }
            double desviacion = Math.sqrt(varianza);

            // 2. Obtener opción y límites
            String selectedType = calculationType.getValue();
            if (selectedType == null) {
                throw new IllegalArgumentException("Debe seleccionar un tipo de cálculo.");
            }
            int option = typeMap.get(selectedType);

            double a = InputValidator.parseDouble(aField.getText(), "Límite 'a'");
            Double b = null;
            if (option == 3) { // Si es rango, necesitamos 'b'
                b = InputValidator.parseDouble(bField.getText(), "Límite 'b'");
            }

            // Manejo del caso de desviación cero
            if (desviacion == 0) {
                resultArea.setText("Varianza/Desviación Cero: La probabilidad es determinista (0 o 1). " +
                        "Este módulo no está diseñado para ese caso extremo.");
                return;
            }

            // 3. Calcular Probabilidad
            double prob;
            double z1, z2;
            String resultText;

            switch (option) {
                case 1: // P(X ≤ a)
                    prob = GaussianaSolver.solveNormalAcumulada(a, media, desviacion, false);
                    z1 = GaussianaSolver.calculateZScore(a, media, desviacion);
                    resultText = formatGaussianaResult(a, null, prob, option, z1, null);
                    break;
                case 2: // P(X ≥ a)
                    prob = GaussianaSolver.solveNormalAcumulada(a, media, desviacion, true);
                    z1 = GaussianaSolver.calculateZScore(a, media, desviacion);
                    resultText = formatGaussianaResult(a, null, prob, option, z1, null);
                    break;
                case 3: // P(a ≤ X ≤ b)
                    if (a > b) {
                        double temp = a; a = b; b = temp; // Asegurar a <= b
                    }
                    prob = GaussianaSolver.solveNormalRango(a, b, media, desviacion);
                    z1 = GaussianaSolver.calculateZScore(a, media, desviacion);
                    z2 = GaussianaSolver.calculateZScore(b, media, desviacion);
                    resultText = formatGaussianaResult(a, b, prob, option, z1, z2);
                    break;
                default:
                    throw new IllegalArgumentException("Opción de cálculo de Gaussiana no válida.");
            }

            // 4. Mostrar resultado
            resultArea.setText(resultText);

        } catch (IllegalArgumentException ex) {
            PopupManager.showError(ex.getMessage());
        } catch (Exception ex) {
            PopupManager.showError("Error desconocido al calcular la probabilidad Gaussiana: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String formatGaussianaResult(double a, Double b, double prob, int option, double z1, Double z2) {
        StringBuilder sb = new StringBuilder();

        sb.append("--- Probabilidad Normal ---\n");

        if (option == 1) {
            sb.append(String.format("Cálculo: P(X ≤ %.4f)\n", a));
        } else if (option == 2) {
            sb.append(String.format("Cálculo: P(X ≥ %.4f)\n", a));
        } else {
            sb.append(String.format("Cálculo: P(%.4f ≤ X ≤ %.4f)\n", a, b));
        }

        sb.append(String.format("Resultado: %.8f\n", prob));
        sb.append(String.format("Porcentaje: %.2f%%\n", prob * 100));

        sb.append("\n--- Z-Scores (Valores Estandarizados) ---\n");
        sb.append(String.format("Z-score (%.4f): %.4f\n", a, z1));

        if (z2 != null) {
            sb.append(String.format("Z-score (%.4f): %.4f\n", b, z2));
        }

        return sb.toString();
    }

    @FXML
    private void onClear() {
        mediaField.clear();
        varianzaField.clear();
        aField.clear();
        bField.clear();
        resultArea.clear();
        calculationType.getSelectionModel().selectFirst();
    }
}