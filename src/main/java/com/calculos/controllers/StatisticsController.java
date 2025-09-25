package com.calculos.controllers;

import com.calculos.models.EstadisticasSolver;
import com.calculos.MainApp;
import com.calculos.utils.PopupManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsController {

    @FXML private ComboBox<String> dataTypeComboBox;
    @FXML private TextField dataInputField;
    @FXML private ComboBox<String> resultTypeComboBox;
    @FXML private TextArea resultArea;

    private List<Double> currentData = new ArrayList<>();
    private String currentDataType = "Continuos"; // Default

    @FXML
    public void initialize() {
        // Inicializar ComboBox de tipo de datos
        dataTypeComboBox.setItems(FXCollections.observableArrayList("Continuos", "Discretos"));
        dataTypeComboBox.setValue("Continuos");
        dataTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> currentDataType = newVal);

        // Inicializar ComboBox de resultados específicos
        resultTypeComboBox.setItems(FXCollections.observableArrayList(
                "Medidas de Posición (Media, Mediana, Moda, Cuartiles)",
                "Medidas de Dispersión (Varianza, Desviación, Curtosis)",
                "Tabla de Frecuencias (Absolutas y Relativas)"
        ));
    }

    private List<Double> parseAndValidateData() throws IllegalArgumentException {
        String entrada = dataInputField.getText().trim();
        if (entrada.isEmpty()) {
            throw new IllegalArgumentException("La lista de números no puede estar vacía.");
        }

        // Reemplaza comas por espacios y divide
        String[] partes = entrada.replace(",", " ").split("\\s+");
        List<Double> numeros = new ArrayList<>();

        for (String parte : partes) {
            if (parte.isEmpty()) continue;
            try {
                double valor = Double.parseDouble(parte);
                if (currentDataType.equals("Discretos") && valor % 1 != 0) {
                    throw new IllegalArgumentException("Los datos discretos deben ser números enteros.");
                }
                numeros.add(valor);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error de formato: Asegúrate de que todos los valores sean números válidos.");
            }
        }

        if (numeros.isEmpty()) {
            throw new IllegalArgumentException("No se detectaron números válidos en la entrada.");
        }

        // La lista debe estar ordenada para los cálculos de cuartiles y mediana
        Collections.sort(numeros);
        return numeros;
    }

    @FXML
    private void onSolveAll() {
        try {
            currentData = parseAndValidateData();

            // 1. Mostrar datos básicos y ordenados
            StringBuilder sb = new StringBuilder();
            sb.append("--- Análisis Estadístico Descriptivo ---\n");
            sb.append(String.format("Tipo de Datos: %s\n", currentDataType));
            sb.append(String.format("Cantidad de Datos (N): %d\n", currentData.size()));
            sb.append(String.format("Lista Ordenada: %s...\n",
                    currentData.stream()
                            .limit(20) // Muestra solo los primeros 20 para no saturar
                            .map(d -> String.format("%.2f", d))
                            .collect(Collectors.joining(", ")) + (currentData.size() > 20 ? " y más" : "")));
            sb.append("\n======================================\n\n");

            // 2. Calcular y mostrar todas las secciones
            sb.append(calculatePosicion(currentData));
            sb.append("\n======================================\n\n");
            sb.append(calculateDispersion(currentData));
            sb.append("\n======================================\n\n");
            sb.append(calculateFrequencies(currentData));

            resultArea.setText(sb.toString());

        } catch (IllegalArgumentException ex) {
            PopupManager.showError(ex.getMessage());
        } catch (Exception ex) {
            PopupManager.showError("Error inesperado en el cálculo de estadísticas: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void onSolveSpecific() {
        String selected = resultTypeComboBox.getValue();
        if (selected == null) return;

        try {
            // Asegurarse de que los datos estén parseados y validados antes de un cálculo específico
            if (currentData.isEmpty() || !dataInputField.getText().trim().isEmpty()) {
                currentData = parseAndValidateData();
            }

            if (selected.contains("Posición")) {
                resultArea.setText(calculatePosicion(currentData));
            } else if (selected.contains("Dispersión")) {
                resultArea.setText(calculateDispersion(currentData));
            } else if (selected.contains("Frecuencias")) {
                resultArea.setText(calculateFrequencies(currentData));
            }

        } catch (IllegalArgumentException ex) {
            // Si hay un error al parsear los datos, mostramos el error
            PopupManager.showError(ex.getMessage());
        }
    }

    private String calculatePosicion(List<Double> nums) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- MEDIDAS DE POSICIÓN ---\n");

        double media = EstadisticasSolver.media(nums);
        double mediana = EstadisticasSolver.mediana(nums);
        List<Double> modas = EstadisticasSolver.moda(nums);
        double q1 = EstadisticasSolver.cuartil(nums, 1);
        double q3 = EstadisticasSolver.cuartil(nums, 3);

        sb.append(String.format("Media (μ/x̄): %.4f\n", media));
        sb.append(String.format("Mediana (Q2): %.4f\n", mediana));

        String modaStr = modas.isEmpty() ? "No hay moda o es unimodal (f=1)." :
                modas.size() == 1 ? String.format("%.4f", modas.get(0)) :
                        "Múltiple: " + modas.stream().map(d -> String.format("%.4f", d)).collect(Collectors.joining(", "));
        sb.append(String.format("Moda: %s\n", modaStr));

        sb.append(String.format("Primer Cuartil (Q1): %.4f\n", q1));
        sb.append(String.format("Tercer Cuartil (Q3): %.4f\n", q3));

        return sb.toString();
    }

    private String calculateDispersion(List<Double> nums) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- MEDIDAS DE DISPERSIÓN ---\n");

        double rango = EstadisticasSolver.rango(nums);
        double varianza = EstadisticasSolver.varianza(nums);
        double desvEstandar = EstadisticasSolver.desviacionEstandar(nums);
        double rangoIQ = EstadisticasSolver.rangoIntercuartilico(nums);
        double cv = EstadisticasSolver.coeficienteVariacion(nums);
        double curtosis = EstadisticasSolver.coeficienteCurtosis(nums);

        sb.append(String.format("Rango: %.4f\n", rango));
        sb.append(String.format("Varianza (σ²): %.4f\n", varianza));
        sb.append(String.format("Desviación Estándar (σ): %.4f\n", desvEstandar));
        sb.append(String.format("Rango Intercuartílico (RIQ): %.4f\n", rangoIQ));
        sb.append(String.format("Coeficiente de Variación (CV): %.2f%%\n", cv));

        String interpretacion = curtosis < -0.263 ? "Platicúrtica (aplanada)" :
                curtosis > 0.263 ? "Leptocúrtica (apuntada)" : "Mesocúrtica (Normal)";
        sb.append(String.format("Coeficiente de Curtosis (g₂): %.4f → %s\n", curtosis, interpretacion));

        return sb.toString();
    }

    private String calculateFrequencies(List<Double> nums) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- TABLA DE FRECUENCIAS ---\n");

        Map<Double, Long> frecuenciaAbs = EstadisticasSolver.getFrecuenciasAbsolutas(nums);
        int total = nums.size();
        long acumuladaAbs = 0;
        double acumuladaRel = 0;

        // Título de la tabla
        sb.append(String.format("%-10s %-8s %-8s %-12s %-12s\n", "Valor (xi)", "f (Abs)", "F (Abs)", "fr (Rel %)", "Fr (Rel %)"));
        sb.append("-----------------------------------------------------------------------\n");

        for (Map.Entry<Double, Long> entry : frecuenciaAbs.entrySet()) {
            double valor = entry.getKey();
            long f = entry.getValue();

            acumuladaAbs += f;
            double fr = (f * 100.0) / total;
            acumuladaRel += fr;

            sb.append(String.format("%-10.2f %-8d %-8d %-12.2f %-12.2f\n", valor, f, acumuladaAbs, fr, acumuladaRel));
        }

        return sb.toString();
    }

    @FXML
    private void onClear() {
        dataInputField.clear();
        resultArea.clear();
        currentData.clear();
        dataTypeComboBox.setValue("Continuos");
        resultTypeComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void backToMenu() {
        try {
            MainApp.showMainMenuView();
        } catch (Exception e) {
            PopupManager.showError("Error al volver al menú principal: " + e.getMessage());
        }
    }
}