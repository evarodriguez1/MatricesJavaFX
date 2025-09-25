package com.calculos.controllers;

import com.calculos.models.PoissonSolver;
import com.calculos.utils.InputValidator;
import com.calculos.utils.PopupManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.LinkedHashMap;
import java.util.Map;

public class PoissonController {

    @FXML private TextField lambdaField; // Promedio de ocurrencias (λ)
    @FXML private ComboBox<String> calculationType;
    @FXML private TextField x1Field; // Límite 'x' o 'a'
    @FXML private TextField x2Field; // Límite 'b'
    @FXML private TextArea resultArea;

    private final Map<String, Integer> typeMap = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        // Opciones de cálculo
        typeMap.put("P(X = x) - Exacto", 1);
        typeMap.put("P(X ≤ x) - Como Máximo", 2);
        typeMap.put("P(X ≥ x) - Como Mínimo", 3);
        typeMap.put("P(a ≤ X ≤ b) - Rango", 4);

        calculationType.setItems(FXCollections.observableArrayList(typeMap.keySet()));
        calculationType.getSelectionModel().selectFirst();
    }

    @FXML
    private void onSolve() {
        try {
            // 1. Obtener y validar λ
            double lambda = InputValidator.parseDouble(lambdaField.getText(), "Lambda (λ)");
            if (lambda <= 0) {
                throw new IllegalArgumentException("El promedio de ocurrencias (λ) debe ser positivo.");
            }

            // 2. Obtener opción y límites (deben ser enteros)
            String selectedType = calculationType.getValue();
            if (selectedType == null) {
                throw new IllegalArgumentException("Debe seleccionar un tipo de cálculo.");
            }
            int option = typeMap.get(selectedType);

            int desde, hasta;
            int x1 = (int) validateIntegerInput(x1Field.getText(), "Valor de x/a");

            switch (option) {
                case 1 -> desde = hasta = x1; // Exacto P(X = x)
                case 2 -> { // Máximo P(X ≤ x)
                    desde = 0;
                    hasta = x1;
                }
                case 3 -> { // Mínimo P(X ≥ x)
                    desde = x1;
                    // En Poisson, 'hasta' es teóricamente infinito. Usaremos 'desde' para la lógica.
                    hasta = x1;
                }
                case 4 -> { // Rango P(a ≤ X ≤ b)
                    desde = x1;
                    int x2 = (int) validateIntegerInput(x2Field.getText(), "Valor de b");
                    hasta = x2;
                }
                default -> throw new IllegalArgumentException("Opción de cálculo no válida.");
            }

            // Validar límites
            if (desde < 0 || hasta < 0 || desde > hasta) {
                throw new IllegalArgumentException("El número de eventos debe ser un entero no negativo. El valor 'a' no puede ser mayor que 'b'.");
            }

            // 3. Calcular Probabilidad
            double totalProb;
            if (option == 3) {
                // Usamos el método de complemento para P(X ≥ x)
                totalProb = PoissonSolver.solvePoissonMinimo(lambda, desde);
            } else {
                // Usamos el método de rango para P(X = x), P(X ≤ x), P(a ≤ X ≤ b)
                totalProb = PoissonSolver.solvePoissonRange(lambda, desde, hasta);
            }

            // 4. Formatear y mostrar resultado
            String resultText = formatPoissonResult(option, desde, hasta, totalProb, lambda);
            resultArea.setText(resultText);

        } catch (IllegalArgumentException ex) {
            PopupManager.showError(ex.getMessage());
        } catch (Exception ex) {
            PopupManager.showError("Error desconocido al calcular la probabilidad de Poisson: " + ex.getMessage());
        }
    }

    private double validateIntegerInput(String text, String fieldName) {
        double value = InputValidator.parseDouble(text, fieldName);
        if (value % 1 != 0) {
            throw new IllegalArgumentException("El campo " + fieldName + " debe ser un número entero.");
        }
        return value;
    }

    private String formatPoissonResult(int option, int desde, int hasta, double prob, double lambda) {
        StringBuilder sb = new StringBuilder();
        String probDescription;

        // 1. Descripción
        if (option == 1) probDescription = String.format("P(X = %d)", desde);
        else if (option == 2) probDescription = String.format("P(X ≤ %d)", hasta);
        else if (option == 3) probDescription = String.format("P(X ≥ %d)", desde);
        else probDescription = String.format("P(%d ≤ X ≤ %d)", desde, hasta);

        sb.append("--- Probabilidad de Poisson ---\n");
        sb.append(String.format("Promedio de ocurrencias (λ): %.4f\n", lambda));
        sb.append(String.format("Cálculo: %s\n", probDescription));
        sb.append(String.format("Resultado: %.10f\n", prob));
        sb.append(String.format("Porcentaje: %.2f%%\n", prob * 100));

        sb.append("\n--- Otros Datos ---\n");
        // En Poisson, E[X] = Var[X] = λ
        sb.append(String.format("Esperanza E[X] (Media): %.4f\n", lambda));
        sb.append(String.format("Varianza Var[X]: %.4f\n", lambda));

        return sb.toString();
    }

    @FXML
    private void onClear() {
        lambdaField.clear();
        x1Field.clear();
        x2Field.clear();
        resultArea.clear();
        calculationType.getSelectionModel().selectFirst();
    }
}