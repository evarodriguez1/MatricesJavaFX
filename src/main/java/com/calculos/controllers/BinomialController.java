package com.calculos.controllers;

import com.calculos.models.BinomialSolver;
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

public class BinomialController {

    @FXML private TextField nField;
    @FXML private TextField pField;
    @FXML private ComboBox<String> calculationType;
    @FXML private TextField k1Field;
    @FXML private TextField k2Field;
    @FXML private TextArea resultArea;

    private final Map<String, Integer> typeMap = new LinkedHashMap<>();
    private List<TextField> allFields; // Lista para manejo eficiente

    /**
     * Se ejecuta al cargar la vista. Ideal para configuraciones iniciales.
     */
    @FXML
    public void initialize() {
        // Poblamos la lista de campos para una gestión más sencilla.
        allFields = Arrays.asList(nField, pField, k1Field, k2Field);

        // Define las opciones del ComboBox
        typeMap.put("P(X = k) - Exacto", 1);
        typeMap.put("P(X ≤ k) - Como Máximo", 2);
        typeMap.put("P(X ≥ k) - Como Mínimo", 3);
        typeMap.put("P(a ≤ X ≤ b) - Rango", 4);
        calculationType.setItems(FXCollections.observableArrayList(typeMap.keySet()));
        calculationType.getSelectionModel().selectFirst();

        // --- MEJORA DE UX: Listener para habilitar/deshabilitar k2Field ---
        // Esto se activa cada vez que el usuario cambia la selección del ComboBox.
        calculationType.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            boolean isRange = typeMap.getOrDefault(newValue, 0) == 4;
            k2Field.setDisable(!isRange); // Deshabilita el campo si NO es rango.
            if (!isRange) {
                k2Field.clear(); // Limpia el campo si ya no se necesita.
            }
        });
    }

    /**
     * Resuelve la probabilidad binomial y muestra los resultados.
     */
    @FXML
    private void onSolve() {
        clearErrorStyles();
        try {
            // 1. Obtener y validar parámetros principales
            int n = getValidatedInteger(nField, "Total Ensayos (n)");
            double p = getValidatedDouble(pField, "Prob. de Éxito (p)");

            if (n <= 0) throw new IllegalArgumentException("El número de ensayos (n) debe ser mayor que cero.");
            if (p < 0 || p > 1) throw new IllegalArgumentException("La probabilidad (p) debe estar entre 0 y 1.");

            // 2. Determinar rango de cálculo (desde, hasta)
            String selectedType = calculationType.getValue();
            int option = typeMap.getOrDefault(selectedType, 0);

            int desde;
            int hasta;
            int k1 = getValidatedInteger(k1Field, "Valor de k (o a)");

            switch (option) {
                case 1 -> desde = hasta = k1; // Exacto
                case 2 -> { desde = 0; hasta = k1; } // Máximo
                case 3 -> { desde = k1; hasta = n; } // Mínimo
                case 4 -> {
                    desde = k1;
                    hasta = getValidatedInteger(k2Field, "Valor de b");
                }
                default -> throw new IllegalArgumentException("Por favor, seleccione un tipo de cálculo válido.");
            }

            if (hasta < desde) throw new IllegalArgumentException("El valor de 'b' no puede ser menor que 'a' en un rango.");
            if (hasta > n) throw new IllegalArgumentException("Los valores de éxito no pueden ser mayores que el número de ensayos (n).");

            // 3. Calcular resultados con el Solver
            double totalProb = BinomialSolver.solveBinomialRange(n, p, desde, hasta);
            double esperanza = BinomialSolver.getEsperanza(n, p);
            double varianza = BinomialSolver.getVarianza(n, p);

            // 4. Formatear y mostrar resultado amigable
            resultArea.setText(formatResult(selectedType, desde, hasta, totalProb, esperanza, varianza));

        } catch (IllegalArgumentException e) {
            // El 'helper' ya marcó el campo con error, aquí solo mostramos el popup.
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
    private String formatResult(String selectedType, int desde, int hasta, double prob, double esperanza, double varianza) {
        String probDescription;
        String probType = selectedType.split(" - ")[1]; // Extrae "Exacto", "Como Máximo", etc.

        if (probType.equals("Rango")) {
            probDescription = String.format("obtener entre %d y %d éxitos", desde, hasta);
        } else {
            probDescription = String.format("obtener %s %d éxitos", probType.toLowerCase(), hasta);
        }

        return String.format(
                "--- Resultados ---\n" +
                        "La probabilidad de %s es: %,.5f\n\n" +
                        "--- Otros datos de interés ---\n" +
                        "Valor Esperado (Media): %.4f\n" +
                        "↳ En promedio, es el número de éxitos que podrías esperar.\n\n" +
                        "Varianza: %.4f\n" +
                        "↳ Mide qué tan dispersos estarán los resultados respecto al promedio.",
                probDescription, prob, esperanza, varianza
        );
    }
}