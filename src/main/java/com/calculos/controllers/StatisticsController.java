package com.calculos.controllers;

import com.calculos.models.EstadisticasSolver;
import com.calculos.MainApp;
import com.calculos.utils.PopupManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StatisticsController {

    @FXML private ComboBox<String> dataTypeComboBox;
    @FXML private TextField dataInputField;

    private List<Double> currentData = new ArrayList<>();
    private String currentDataType;

    @FXML
    public void initialize() {
        dataTypeComboBox.setItems(FXCollections.observableArrayList("Continuos", "Discretos"));

        dataTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentDataType = newVal;
                dataInputField.setDisable(false);
                if (newVal.equals("Discretos")) {
                    dataInputField.setPromptText("Ej: 1, 2, 3, 4, 5");
                } else {
                    dataInputField.setPromptText("Ej: 1.5, 2.33, 3.1, 4.0");
                }
            }
        });
    }

    private List<Double> parseAndValidateData() throws IllegalArgumentException {
        String entrada = dataInputField.getText().trim();
        if (entrada.isEmpty()) {
            throw new IllegalArgumentException("La lista de números no puede estar vacía.");
        }

        String[] partes = entrada.replace(",", " ").split("\\s+");
        List<Double> numeros = new ArrayList<>();

        for (String parte : partes) {
            if (parte.isEmpty()) continue;
            try {
                double valor = Double.parseDouble(parte);
                if ("Discretos".equals(currentDataType) && valor % 1 != 0) {
                    throw new IllegalArgumentException("Se seleccionó 'Discretos', pero se encontraron valores con decimales.");
                }
                numeros.add(valor);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error de formato: Asegúrate de que todos los valores sean números válidos.");
            }
        }

        if (numeros.isEmpty()) {
            throw new IllegalArgumentException("No se detectaron números válidos en la entrada.");
        }
        return numeros;
    }

    @FXML
    private void onSolveAll() {
        try {
            currentData = parseAndValidateData();

            // Creamos una copia ordenada solo para la visualización inicial de la lista
            List<Double> sortedDataForDisplay = new ArrayList<>(currentData);
            Collections.sort(sortedDataForDisplay);

            StringBuilder sb = new StringBuilder();
            sb.append("--- Análisis Estadístico Descriptivo Completo ---\n\n");
            sb.append(String.format("Tipo de Datos Seleccionado: %s\n", currentDataType));
            sb.append(String.format("Cantidad Total de Datos (N): %d\n\n", sortedDataForDisplay.size()));

            sb.append("Lista de Datos Ordenada:\n");
            String datosOrdenados = sortedDataForDisplay.stream()
                    .map(d -> String.format(currentDataType.equals("Discretos") ? "%.0f" : "%.4f", d))
                    .collect(Collectors.joining(", "));
            sb.append(datosOrdenados);
            sb.append("\n\n======================================================\n\n");

            // Pasamos la lista original (sin ordenar) al Solver
            sb.append(calculatePosicion(currentData));
            sb.append("\n======================================================\n\n");
            sb.append(calculateDispersion(currentData));
            sb.append("\n======================================================\n\n");
            sb.append(calculateFrequencies(currentData));

            PopupManager.showResultsPopup(
                    "Resultados Estadísticos",
                    "Cálculos completados exitosamente.",
                    sb.toString()
            );

        } catch (IllegalArgumentException ex) {
            PopupManager.showError(ex.getMessage());
        } catch (Exception ex) {
            PopupManager.showError("Ocurrió un error inesperado al calcular: " + ex.getMessage());
            ex.printStackTrace();
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

        String modaStr = modas.isEmpty() ? "No hay moda (o todos los valores son únicos)." :
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

        String interpretacion = curtosis < -0.2 ? "Platicúrtica (aplanada)" :
                curtosis > 0.2 ? "Leptocúrtica (apuntada)" : "Mesocúrtica (Normal)";
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

        sb.append(String.format("%-12s %-10s %-10s %-15s %-15s\n", "Valor (xi)", "f (Abs)", "F (Acum)", "fr (Rel %)", "Fr (Acum %)"));
        sb.append("-----------------------------------------------------------------------\n");

        // Creamos una lista de las claves para poder iterar ordenadamente
        List<Double> sortedKeys = new ArrayList<>(frecuenciaAbs.keySet());
        Collections.sort(sortedKeys);

        for (Double valor : sortedKeys) {
            long f = frecuenciaAbs.get(valor);
            acumuladaAbs += f;
            double fr = (f * 100.0) / total;
            acumuladaRel += fr;

            sb.append(String.format("%-12.2f %-10d %-10d %-15.2f %-15.2f\n", valor, f, acumuladaAbs, fr, acumuladaRel));
        }

        return sb.toString();
    }

    @FXML
    private void onClear() {
        dataInputField.clear();
        dataInputField.setDisable(true);
        dataInputField.setPromptText("Primero selecciona un tipo");
        currentData.clear();

        dataTypeComboBox.setValue(null);
        dataTypeComboBox.setPromptText("Selecciona el tipo de dato");

        currentDataType = null;
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