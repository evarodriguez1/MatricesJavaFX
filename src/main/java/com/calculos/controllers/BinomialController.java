package com.calculos.controllers;

import com.calculos.models.BinomialSolver;
import com.calculos.utils.InputValidator; // Reutilizamos tu validador
import com.calculos.utils.PopupManager;   // Reutilizamos tu gestor de popups
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.LinkedHashMap;
import java.util.Map;

public class BinomialController {

    @FXML private TextField nField;
    @FXML private TextField pField;
    @FXML private ComboBox<String> calculationType;
    @FXML private TextField k1Field; // Usaremos k1 para k, a, o el límite inferior
    @FXML private TextField k2Field; // Usaremos k2 para el límite superior b (opcional)
    @FXML private TextArea resultArea;

    // Mapa para asociar la descripción del menú con un valor numérico/lógico
    private final Map<String, Integer> typeMap = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        // Define las opciones del menú con el formato que tenías en consola
        typeMap.put("P(X = k) - Exacto", 1);
        typeMap.put("P(X ≤ k) - Como Máximo", 2);
        typeMap.put("P(X ≥ k) - Como Mínimo", 3);
        typeMap.put("P(a ≤ X ≤ b) - Rango", 4);

        calculationType.setItems(FXCollections.observableArrayList(typeMap.keySet()));
        calculationType.getSelectionModel().selectFirst(); // Selecciona el primero por defecto
    }

    @FXML
    private void onSolve() {
        try {
            // 1. Obtener y validar los parámetros N y P
            int n = (int) InputValidator.parseDouble(nField.getText(), "N (Total Ensayos)");
            double p = InputValidator.parseDouble(pField.getText(), "p (Prob. Éxito)");

            if (n <= 0) {
                throw new IllegalArgumentException("El número de ensayos (n) debe ser positivo.");
            }
            if (p < 0 || p > 1) {
                throw new IllegalArgumentException("La probabilidad (p) debe estar entre 0 y 1.");
            }

            // 2. Obtener la opción de cálculo y los valores de k
            String selectedType = calculationType.getValue();
            if (selectedType == null) {
                throw new IllegalArgumentException("Debe seleccionar un tipo de cálculo.");
            }

            int option = typeMap.get(selectedType);
            int desde, hasta;

            // k1 es el valor principal (k, a)
            double k1Double = InputValidator.parseDouble(k1Field.getText(), "Valor de k/a");
            if (k1Double % 1 != 0 || k1Double < 0) {
                throw new IllegalArgumentException("El valor de éxito debe ser un entero no negativo.");
            }
            int k1 = (int) k1Double;

            switch (option) {
                case 1 -> desde = hasta = k1; // Exacto: P(X = k)
                case 2 -> { // Máximo: P(X ≤ k)
                    desde = 0;
                    hasta = k1;
                }
                case 3 -> { // Mínimo: P(X ≥ k)
                    desde = k1;
                    hasta = n; // Máximo de éxitos es el número total de ensayos
                }
                case 4 -> { // Rango: P(a ≤ X ≤ b)
                    desde = k1; // a
                    double k2Double = InputValidator.parseDouble(k2Field.getText(), "Valor de b");
                    if (k2Double % 1 != 0 || k2Double < 0) {
                        throw new IllegalArgumentException("El valor de éxito b debe ser un entero no negativo.");
                    }
                    hasta = (int) k2Double; // b
                }
                default -> throw new IllegalArgumentException("Opción de cálculo no válida.");
            }

            // Validar límites del rango
            if (desde > n || hasta > n || desde < 0 || hasta < 0 || desde > hasta) {
                throw new IllegalArgumentException(String.format("Rango de éxitos inválido. Los valores deben estar entre 0 y n (%d) y 'a' no puede ser mayor que 'b'.", n));
            }


            // 3. Calcular Probabilidad
            double totalProb = BinomialSolver.solveBinomialRange(n, p, desde, hasta);
            double esperanza = BinomialSolver.getEsperanza(n, p);
            double varianza = BinomialSolver.getVarianza(n, p);

            // 4. Formatear y mostrar resultado
            String resultText = formatBinomialResult(option, desde, hasta, totalProb, esperanza, varianza);
            resultArea.setText(resultText);

        } catch (IllegalArgumentException ex) {
            PopupManager.showError(ex.getMessage());
        } catch (Exception ex) {
            PopupManager.showError("Error desconocido al calcular la probabilidad binomial: " + ex.getMessage());
        }
    }

    private String formatBinomialResult(int option, int desde, int hasta, double prob, double esperanza, double varianza) {
        StringBuilder sb = new StringBuilder();
        String probDescription;

        // 1. Descripción de la probabilidad
        if (option == 1) {
            probDescription = String.format("P(X = %d)", desde);
        } else if (option == 2) {
            probDescription = String.format("P(X ≤ %d)", hasta);
        } else if (option == 3) {
            probDescription = String.format("P(X ≥ %d)", desde);
        } else { // option == 4
            probDescription = String.format("P(%d ≤ X ≤ %d)", desde, hasta);
        }

        sb.append(String.format("PROBABILIDAD %s:\n", probDescription));
        sb.append(String.format("Resultado: %.8f\n", prob));
        sb.append(String.format("Porcentaje: %.2f%%\n", prob * 100));
        sb.append("\n----------------------------------\n");
        sb.append("OTROS DATOS DE INTERÉS:\n");
        sb.append(String.format("Esperanza E[X]: %.4f\n", esperanza));
        sb.append(String.format("Varianza Var[X]: %.4f\n", varianza));

        return sb.toString();
    }

    @FXML
    private void onClear() {
        nField.clear();
        pField.clear();
        k1Field.clear();
        k2Field.clear();
        resultArea.clear();
        calculationType.getSelectionModel().selectFirst();
    }
}