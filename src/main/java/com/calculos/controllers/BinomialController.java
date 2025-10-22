package com.calculos.controllers;

import com.calculos.models.BinomialSolver;
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

public class BinomialController {

    // === CAMPOS FXML ===
    @FXML private TextField nField;
    @FXML private TextField pField;
    @FXML private ComboBox<String> calculationType;
    @FXML private GridPane inputFieldsPane;
    @FXML private Label k1Label;
    @FXML private TextField k1Field;
    @FXML private Label k2Label;
    @FXML private TextField k2Field;
    @FXML private TextArea resultArea;

    // === VARIABLES DE CLASE ===
    private final Map<String, Integer> typeMap = new LinkedHashMap<>();
    private List<TextField> allFields;

    @FXML
    public void initialize() {
        allFields = Arrays.asList(nField, pField, k1Field, k2Field);

        typeMap.put("P(X = k) - Puntual", 1);
        typeMap.put("P(X ≤ k) - Acumulada Inferior", 2);
        typeMap.put("P(X ≥ k) - Acumulada Superior", 3);
        typeMap.put("P(a ≤ X ≤ b) - Rango", 4);
        calculationType.setItems(FXCollections.observableArrayList(typeMap.keySet()));

        calculationType.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (newValue == null) {
                inputFieldsPane.setVisible(false);
                inputFieldsPane.setManaged(false);
                return;
            }

            inputFieldsPane.setVisible(true);
            inputFieldsPane.setManaged(true);

            boolean isRange = typeMap.getOrDefault(newValue, 0) == 4;

            k1Label.setText(isRange ? "Límite inferior 'a':" : "Valor de 'k':");
            k1Field.setPromptText(isRange ? "Ej: 2" : "Número de éxitos");

            k2Label.setVisible(isRange);
            k2Label.setManaged(isRange);
            k2Field.setVisible(isRange);
            k2Field.setManaged(isRange);

            if (!isRange) {
                k2Field.clear();
            }
        });
    }

    /**
     * ✅ CORRECCIÓN: Nuevo método helper que usa InputValidator.parseDouble
     * y luego verifica si el número es un entero válido.
     */
    private int getValidatedInteger(TextField field, String fieldName) throws IllegalArgumentException {
        // Usa el validador proporcionado para obtener un double. Esto maneja campos vacíos y formato.
        double value = InputValidator.parseDouble(field.getText(), fieldName);

        // Realiza la validación específica para enteros aquí en el controlador.
        if (value < 0) {
            throw new IllegalArgumentException(String.format("El campo '%s' debe ser un número no negativo.", fieldName));
        }
        if (value % 1 != 0) {
            throw new IllegalArgumentException(String.format("El campo '%s' debe ser un número entero (sin decimales).", fieldName));
        }
        return (int) value;
    }

    @FXML
    private void onSolve() {
        try {
            // ✅ CORRECCIÓN: Las llamadas a la validación ahora usan el nuevo método helper.
            int n = getValidatedInteger(nField, "Total Ensayos (n)");
            double p = InputValidator.parseDouble(pField.getText(), "Prob. de Éxito (p)");

            if (n <= 0) throw new IllegalArgumentException("El número de ensayos (n) debe ser mayor que cero.");
            if (p < 0 || p > 1) throw new IllegalArgumentException("La probabilidad (p) debe estar entre 0 y 1.");

            String selectedType = calculationType.getValue();
            if (selectedType == null) {
                throw new IllegalArgumentException("Por favor, selecciona un tipo de cálculo.");
            }
            int option = typeMap.get(selectedType);

            int desde;
            int hasta;
            int k1 = getValidatedInteger(k1Field, isRange(option) ? "Límite inferior 'a'" : "Valor de 'k'");

            switch (option) {
                case 1 -> desde = hasta = k1;
                case 2 -> { desde = 0; hasta = k1; }
                case 3 -> { desde = k1; hasta = n; }
                case 4 -> {
                    desde = k1;
                    hasta = getValidatedInteger(k2Field, "Límite superior 'b'");
                }
                default -> throw new IllegalStateException("Opción de cálculo no válida.");
            }

            if (hasta < desde) throw new IllegalArgumentException("El límite superior 'b' no puede ser menor que 'a'.");
            if (hasta > n) throw new IllegalArgumentException("Los valores de éxito no pueden ser mayores que 'n'.");

            double totalProb = BinomialSolver.solveBinomialRange(n, p, desde, hasta);
            double esperanza = BinomialSolver.getEsperanza(n, p);
            double varianza = BinomialSolver.getVarianza(n, p);

            resultArea.setText(formatResult(selectedType, desde, hasta, totalProb, esperanza, varianza));

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
        calculationType.setValue(null);
        inputFieldsPane.setVisible(false);
        inputFieldsPane.setManaged(false);
    }

    private boolean isRange(int option) {
        return option == 4;
    }

    private String formatResult(String selectedType, int desde, int hasta, double prob, double esperanza, double varianza) {
        String probDescription;
        int option = typeMap.get(selectedType);

        switch (option) {
            case 1: probDescription = String.format("obtener exactamente %d éxitos", hasta); break;
            case 2: probDescription = String.format("obtener como máximo %d éxitos", hasta); break;
            case 3: probDescription = String.format("obtener como mínimo %d éxitos", desde); break;
            case 4: probDescription = String.format("obtener entre %d y %d éxitos (inclusive)", desde, hasta); break;
            default: probDescription = "cálculo especificado";
        }

        return String.format(
                "--- Resultado del Cálculo ---\n" +
                        "La probabilidad de %s es: \n%,.8f (%.4f %%)\n\n" +
                        "--- Parámetros de la Distribución ---\n" +
                        "Valor Esperado (Media μ): %.4f\n" +
                        "Varianza (σ²): %.4f\n" +
                        "Desviación Estándar (σ): %.4f",
                probDescription, prob, prob * 100, esperanza, varianza, Math.sqrt(varianza)
        );
    }
}