package com.calculos.controllers;

import com.calculos.models.PoissonSolver;
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

public class PoissonController {

    @FXML private TextField lambdaField;
    @FXML private ComboBox<String> calculationType;
    @FXML private GridPane inputFieldsPane; // Contenedor dinámico
    @FXML private Label x1Label;
    @FXML private TextField x1Field;
    @FXML private Label x2Label;
    @FXML private TextField x2Field;
    @FXML private TextArea resultArea;

    private final Map<String, Integer> typeMap = new LinkedHashMap<>();
    private List<TextField> allFields;

    @FXML
    public void initialize() {
        allFields = Arrays.asList(lambdaField, x1Field, x2Field);

        typeMap.put("P(X = x) - Puntual", 1);
        typeMap.put("P(X ≤ x) - Acumulada Inferior", 2);
        typeMap.put("P(X ≥ x) - Acumulada Superior", 3);
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

            x1Label.setText(isRange ? "Límite inferior 'a':" : "Nº de Eventos 'x':");
            x1Field.setPromptText(isRange ? "Ej: 2" : "Número de eventos");

            x2Label.setVisible(isRange);
            x2Label.setManaged(isRange);
            x2Field.setVisible(isRange);
            x2Field.setManaged(isRange);

            if (!isRange) {
                x2Field.clear();
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
            double lambda = InputValidator.parseDouble(lambdaField.getText(), "Promedio (λ)");
            if (lambda <= 0) {
                throw new IllegalArgumentException("El promedio de ocurrencias (λ) debe ser un número positivo.");
            }

            String selectedType = calculationType.getValue();
            if (selectedType == null) {
                throw new IllegalArgumentException("Por favor, selecciona un tipo de cálculo.");
            }
            int option = typeMap.get(selectedType);

            int desde;
            int hasta = 0; // Inicializada para evitar error de compilación
            int x1 = getValidatedInteger(x1Field, isRange(option) ? "Límite inferior 'a'" : "Valor de 'x'");

            switch (option) {
                case 1 -> desde = hasta = x1;
                case 2 -> { desde = 0; hasta = x1; }
                case 3 -> desde = x1;
                case 4 -> {
                    desde = x1;
                    hasta = getValidatedInteger(x2Field, "Límite superior 'b'");
                }
                default -> throw new IllegalStateException("Opción de cálculo inesperada.");
            }

            if (option == 4 && hasta < desde) {
                throw new IllegalArgumentException("El límite superior 'b' no puede ser menor que 'a'.");
            }

            double totalProb;
            if (option == 3) {
                totalProb = PoissonSolver.solvePoissonMinimo(lambda, desde);
            } else {
                totalProb = PoissonSolver.solvePoissonRange(lambda, desde, hasta);
            }

            resultArea.setText(formatResult(selectedType, desde, hasta, totalProb, lambda));

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

    private String formatResult(String selectedType, int desde, int hasta, double prob, double lambda) {
        String probDescription;
        int option = typeMap.get(selectedType);

        switch (option) {
            case 1: probDescription = String.format("que ocurran exactamente %d eventos", desde); break;
            case 2: probDescription = String.format("que ocurran como máximo %d eventos", hasta); break;
            case 3: probDescription = String.format("que ocurran como mínimo %d eventos", desde); break;
            case 4: probDescription = String.format("que ocurran entre %d y %d eventos", desde, hasta); break;
            default: probDescription = "cálculo especificado";
        }

        return String.format(
                "--- Resultado del Cálculo ---\n" +
                        "La probabilidad de %s es: \n%,.8f (%.4f %%)\n\n" +
                        "--- Parámetros de la Distribución ---\n" +
                        "Media (μ) y Varianza (σ²) son iguales a λ en Poisson.\n" +
                        "Valor Esperado y Varianza = %.4f",
                probDescription, prob, prob * 100, lambda
        );
    }
}