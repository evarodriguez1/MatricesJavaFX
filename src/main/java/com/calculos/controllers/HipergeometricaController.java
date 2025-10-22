package com.calculos.controllers;

import com.calculos.models.HipergeometricaSolver;
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

public class HipergeometricaController {

    @FXML private TextField nPopulationField;
    @FXML private TextField kTotalField;
    @FXML private TextField nSampleField;
    @FXML private ComboBox<String> calculationType;
    @FXML private GridPane inputFieldsPane; // Contenedor dinámico
    @FXML private Label k1Label;
    @FXML private TextField k1Field;
    @FXML private Label k2Label;
    @FXML private TextField k2Field;
    @FXML private TextArea resultArea;

    private final Map<String, Integer> typeMap = new LinkedHashMap<>();
    private List<TextField> allFields;

    @FXML
    public void initialize() {
        allFields = Arrays.asList(nPopulationField, kTotalField, nSampleField, k1Field, k2Field);

        typeMap.put("P(X = k) - Puntual", 1);
        typeMap.put("P(X ≤ k) - Acumulada Inferior", 2);
        typeMap.put("P(X ≥ k) - Acumulada Superior", 3);
        typeMap.put("P(a ≤ X ≤ b) - Rango", 4);
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

            boolean isRange = typeMap.getOrDefault(newValue, 0) == 4;

            k1Label.setText(isRange ? "Límite inferior 'a':" : "Nº de Éxitos 'k':");
            k1Field.setPromptText(isRange ? "Ej: 1" : "Nº de éxitos en la muestra");

            k2Label.setVisible(isRange);
            k2Label.setManaged(isRange);
            k2Field.setVisible(isRange);
            k2Field.setManaged(isRange);

            if (!isRange) {
                k2Field.clear();
            }
        });
    }

    private int getValidatedInteger(TextField field, String fieldName) throws IllegalArgumentException {
        double value = InputValidator.parseDouble(field.getText(), fieldName);
        if (value < 0 || value % 1 != 0) {
            throw new IllegalArgumentException(String.format("El campo '%s' debe ser un número entero no negativo.", fieldName));
        }
        return (int) value;
    }

    @FXML
    private void onSolve() {
        try {
            int N = getValidatedInteger(nPopulationField, "Población Total (N)");
            int K = getValidatedInteger(kTotalField, "Éxitos Totales (K)");
            int n = getValidatedInteger(nSampleField, "Tamaño Muestra (n)");

            if (N <= 0 || K < 0 || n <= 0) throw new IllegalArgumentException("Los parámetros N, K y n deben ser positivos (K puede ser 0).");
            if (K > N) throw new IllegalArgumentException("Los Éxitos Totales (K) no pueden ser mayores que la Población (N).");
            if (n > N) throw new IllegalArgumentException("El Tamaño de la Muestra (n) no puede ser mayor que la Población (N).");

            String selectedType = calculationType.getValue();
            if (selectedType == null) {
                throw new IllegalArgumentException("Por favor, selecciona un tipo de cálculo.");
            }
            int option = typeMap.get(selectedType);

            int desde, hasta;
            int k1 = getValidatedInteger(k1Field, isRange(option) ? "Límite inferior 'a'" : "Valor de 'k'");

            switch (option) {
                case 1 -> desde = hasta = k1;
                case 2 -> { desde = 0; hasta = k1; }
                case 3 -> { desde = k1; hasta = Math.min(n, K); }
                case 4 -> {
                    desde = k1;
                    hasta = getValidatedInteger(k2Field, "Límite superior 'b'");
                }
                default -> throw new IllegalStateException("Opción de cálculo inesperada.");
            }

            if (hasta < desde) throw new IllegalArgumentException("El límite superior 'b' no puede ser menor que 'a'.");
            if (k1 > n || hasta > n) throw new IllegalArgumentException("El número de éxitos buscados (k, a, b) no puede ser mayor que el tamaño de la muestra (n).");
            if (k1 > K || hasta > K) throw new IllegalArgumentException("El número de éxitos buscados (k, a, b) no puede ser mayor que los éxitos totales en el lote (K).");

            double totalProb = HipergeometricaSolver.solveHipergeometricaRange(N, K, n, desde, hasta);
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

        // ✅ CORRECCIÓN: Restablecer estado inicial
        calculationType.setValue(null);
        inputFieldsPane.setVisible(false);
        inputFieldsPane.setManaged(false);
    }

    private boolean isRange(int option) {
        return option == 4;
    }

    private String formatResult(String selectedType, int desde, int hasta, double prob, int N, int K, int n) {
        String probDescription;
        int option = typeMap.get(selectedType);

        switch (option) {
            case 1: probDescription = String.format("obtener exactamente %d éxitos en la muestra", desde); break;
            case 2: probDescription = String.format("obtener como máximo %d éxitos en la muestra", hasta); break;
            case 3: probDescription = String.format("obtener como mínimo %d éxitos en la muestra", desde); break;
            case 4: probDescription = String.format("obtener entre %d y %d éxitos en la muestra", desde, hasta); break;
            default: probDescription = "cálculo especificado";
        }

        double media = HipergeometricaSolver.getEsperanza(N, K, n);
        double varianza = HipergeometricaSolver.getVarianza(N, K, n);

        return String.format(
                "--- Resultado del Cálculo ---\n" +
                        "La probabilidad de %s es: \n%,.8f (%.4f %%)\n\n" +
                        "--- Parámetros de la Distribución ---\n" +
                        "Valor Esperado (Media μ): %.4f\n" +
                        "Varianza (σ²): %.4f\n" +
                        "Desviación Estándar (σ): %.4f",
                probDescription, prob, prob * 100, media, varianza, Math.sqrt(varianza)
        );
    }
}