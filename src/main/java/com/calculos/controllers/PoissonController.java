package com.calculos.controllers;

import com.calculos.models.PoissonSolver;
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

public class PoissonController {

    @FXML private TextField lambdaField;
    @FXML private ComboBox<String> calculationType;
    @FXML private TextField x1Field;
    @FXML private TextField x2Field;
    @FXML private TextArea resultArea;

    private final Map<String, Integer> typeMap = new LinkedHashMap<>();
    private List<TextField> allFields; // Lista para manejo eficiente

    /**
     * Se ejecuta al cargar la vista para configuraciones iniciales.
     */
    @FXML
    public void initialize() {
        allFields = Arrays.asList(lambdaField, x1Field, x2Field);

        typeMap.put("P(X = x) - Exacto", 1);
        typeMap.put("P(X ≤ x) - Como Máximo", 2);
        typeMap.put("P(X ≥ x) - Como Mínimo", 3);
        typeMap.put("P(a ≤ X ≤ b) - Rango", 4);
        calculationType.setItems(FXCollections.observableArrayList(typeMap.keySet()));
        calculationType.getSelectionModel().selectFirst();

        // Listener para habilitar/deshabilitar x2Field (campo de rango)
        calculationType.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            boolean isRange = typeMap.getOrDefault(newValue, 0) == 4;
            x2Field.setDisable(!isRange);
            if (!isRange) {
                x2Field.clear();
            }
        });
    }

    /**
     * Resuelve la probabilidad de Poisson y muestra los resultados.
     */
    @FXML
    private void onSolve() {
        clearErrorStyles();
        try {
            // 1. Validar el parámetro lambda
            double lambda = getValidatedDouble(lambdaField, "Promedio (λ)");
            if (lambda <= 0) {
                throw new IllegalArgumentException("El promedio de ocurrencias (λ) debe ser un número positivo.");
            }

            // 2. Determinar el rango de cálculo (desde, hasta)
            String selectedType = calculationType.getValue();
            int option = typeMap.getOrDefault(selectedType, 0);

            int desde;
            int hasta;
            int x1 = getValidatedInteger(x1Field, "Valor de x (o a)");

            switch (option) {
                case 1 -> desde = hasta = x1; // Exacto
                case 2 -> { desde = 0; hasta = x1; } // Máximo
                case 3 -> {
                    // Lógica especial para 'como mínimo', el cálculo se hace con un método diferente
                    desde = x1;
                    hasta = -1; // Usamos un valor sentinela para identificar este caso en el formateo
                }
                case 4 -> {
                    desde = x1;
                    hasta = getValidatedInteger(x2Field, "Valor de b");
                }
                default -> throw new IllegalArgumentException("Por favor, seleccione un tipo de cálculo válido.");
            }

            if (option == 4 && hasta < desde) {
                throw new IllegalArgumentException("El valor de 'b' no puede ser menor que 'a' en un rango.");
            }

            // 3. Calcular la probabilidad usando el Solver apropiado
            double totalProb;
            if (option == 3) {
                totalProb = PoissonSolver.solvePoissonMinimo(lambda, desde);
            } else {
                totalProb = PoissonSolver.solvePoissonRange(lambda, desde, hasta);
            }

            // 4. Formatear y mostrar el resultado amigable
            resultArea.setText(formatResult(selectedType, desde, hasta, totalProb, lambda));

        } catch (IllegalArgumentException e) {
            PopupManager.showError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            PopupManager.showError("Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    /**
     * Limpia todos los campos de entrada y estilos de error.
     */
    @FXML
    private void onClear() {
        allFields.forEach(TextField::clear);
        resultArea.clear();
        clearErrorStyles();
        calculationType.getSelectionModel().selectFirst();
    }

    // =============================================================
    // MÉTODOS DE AYUDA (Helpers) - Lógica Interna
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

    /**
     * Formatea el resultado final en un formato amigable y "bajado a tierra".
     */
    private String formatResult(String selectedType, int desde, int hasta, double prob, double lambda) {
        String probDescriptionText;
        String fullProbNotation;
        String probType = selectedType.split(" - ")[1];

        // Construcción de la descripción del resultado
        switch(probType) {
            case "Exacto" -> {
                probDescriptionText = String.format("que ocurran exactamente %d eventos", desde);
                fullProbNotation = String.format("P(X = %d)", desde);
            }
            case "Como Máximo" -> {
                probDescriptionText = String.format("que ocurran como máximo %d eventos", hasta);
                fullProbNotation = String.format("P(X ≤ %d)", hasta);
            }
            case "Como Mínimo" -> {
                probDescriptionText = String.format("que ocurran como mínimo %d eventos", desde);
                fullProbNotation = String.format("P(X ≥ %d)", desde);
            }
            case "Rango" -> {
                probDescriptionText = String.format("que ocurran entre %d y %d eventos", desde, hasta);
                fullProbNotation = String.format("P(%d ≤ X ≤ %d)", desde, hasta);
            }
            default -> {
                probDescriptionText = "";
                fullProbNotation = "";
            }
        }

        return String.format(
                "--- Datos Clave de la Distribución ---\n" +
                        "Una propiedad de Poisson es que la media (esperanza) y la varianza son iguales al promedio ingresado.\n" +
                        "→ Valor Esperado E[X] = %.4f\n" +
                        "→ Varianza Var[X] = %.4f\n\n" +
                        "--- Resultado ---\n" +
                        "La probabilidad de %s (%s) es: %,.10f",
                lambda,
                lambda,
                probDescriptionText,
                fullProbNotation,
                prob
        );
    }
}