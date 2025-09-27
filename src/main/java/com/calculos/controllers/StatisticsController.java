package com.calculos.controllers;

import com.calculos.models.DataSet;
import com.calculos.utils.InputValidator;
import com.calculos.utils.exceptions.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para la vista de Estadística Descriptiva.
 * Implementa una UI reactiva que guía al usuario y utiliza el modelo
 * de dominio {@link DataSet} para realizar y presentar análisis estadísticos complejos.
 *
 * @author Tu Nombre (Equipo de Desarrollo)
 */
public final class StatisticsController extends BaseController {

    // --- Componentes FXML de la Vista ---
    @FXML private ComboBox<DataType> dataTypeComboBox;
    @FXML private TextField dataInputField;
    @FXML private ComboBox<ResultType> resultTypeComboBox;
    @FXML private TextArea resultArea;

    private enum DataType {
        CONTINUOUS("Continuos"),
        DISCRETE("Discretos");

        private final String displayName;
        DataType(String name) { this.displayName = name; }
        @Override public String toString() { return displayName; }
    }

    private enum ResultType {
        POSITION("Medidas de Posición"),
        DISPERSION("Medidas de Dispersión"),
        FREQUENCIES("Tabla de Frecuencias");

        private final String displayName;
        ResultType(String name) { this.displayName = name; }
        @Override public String toString() { return displayName; }
    }

    /**
     * Configura el estado inicial de la vista y, crucialmente, los listeners
     * para crear una experiencia de usuario reactiva.
     */
    @FXML
    public void initialize() {
        // --- Configuración Inicial ---
        dataTypeComboBox.getItems().setAll(DataType.values());
        resultTypeComboBox.getItems().setAll(ResultType.values());
        resultTypeComboBox.setPromptText("Calcular sección específica...");

        // --- Lógica de UI Reactiva ---
        // Se crea un listener que se dispara CADA VEZ que el valor del ComboBox cambia.
        dataTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Habilita el campo de texto cuando se selecciona un tipo.
                dataInputField.setDisable(false);
                // Cambia el promptText dinámicamente según la selección.
                if (newVal == DataType.DISCRETE) {
                    dataInputField.setPromptText("Ej: 1 5 12 9 5 ...");
                } else { // CONTINUOUS
                    dataInputField.setPromptText("Ej: 1.52 5.3 12.01 9.8 ...");
                }
            }
        });

        // CORRECCIÓN DEL BUG: Establecer el valor inicial DESPUÉS de añadir el listener.
        dataTypeComboBox.getSelectionModel().select(DataType.CONTINUOUS);
    }

    /**
     * Orquesta el cálculo y presentación de TODAS las medidas estadísticas.
     */
    @FXML
    private void onSolveAll() {
        try {
            DataSet dataSet = createDataSetFromInput();

            StringBuilder sb = new StringBuilder();
            sb.append(formatDataSetInfo(dataSet));
            sb.append("\n======================================\n\n");
            sb.append(formatPositionMeasures(dataSet));
            sb.append("\n======================================\n\n");
            sb.append(formatDispersionMeasures(dataSet));
            sb.append("\n======================================\n\n");
            sb.append(formatFrequenciesTable(dataSet));

            resultArea.setText(sb.toString());
        } catch (ValidationException e) {
            handleValidationException(e);
        } catch (Exception e) {
            handleGenericException(e);
        }
    }

    /**
     * Orquesta el cálculo y presentación de una medida específica seleccionada.
     */
    @FXML
    private void onSolveSpecific() {
        ResultType selectedType = resultTypeComboBox.getValue();
        if (selectedType == null) {
            handleValidationException(new ValidationException("Por favor, selecciona una sección para calcular."));
            return;
        }
        try {
            DataSet dataSet = createDataSetFromInput();

            String resultText = switch(selectedType) {
                case POSITION -> formatPositionMeasures(dataSet);
                case DISPERSION -> formatDispersionMeasures(dataSet);
                case FREQUENCIES -> formatFrequenciesTable(dataSet);
            };

            resultArea.setText(resultText);
        } catch (ValidationException e) {
            handleValidationException(e);
        } catch (Exception e) {
            handleGenericException(e);
        }
    }

    /**
     * Limpia la interfaz de usuario a su estado inicial.
     */
    @FXML
    private void onClear() {
        dataInputField.clear();
        resultArea.clear();
        // Al resetear, también deshabilitamos el campo de texto.
        dataInputField.setDisable(true);
        dataInputField.setPromptText("Selecciona un Tipo de Datos para empezar...");
        dataTypeComboBox.getSelectionModel().select(DataType.CONTINUOUS);
        resultTypeComboBox.getSelectionModel().clearSelection();
    }

    // --- Lógica de Parseo y Creación del Modelo ---

    private DataSet createDataSetFromInput() throws ValidationException {
        String input = dataInputField.getText();
        if (input == null || input.trim().isEmpty()) {
            throw new ValidationException("El campo de entrada de datos no puede estar vacío.");
        }

        DataType dataType = dataTypeComboBox.getValue();
        List<Double> numbers = new ArrayList<>();
        // Expresión regular robusta para dividir por espacios, comas, punto y coma o saltos de línea.
        String[] parts = input.trim().split("[\\s,;\\n]+");

        for (String part : parts) {
            if (part.isEmpty()) continue;
            // Usamos parseDouble que ya maneja comas/puntos
            double value = InputValidator.parseDouble(part, "Dato de entrada");
            if (dataType == DataType.DISCRETE && value % 1 != 0) {
                throw new ValidationException("Tipo de dato 'Discreto' seleccionado, pero se encontró el valor no entero: " + value);
            }
            numbers.add(value);
        }

        if (numbers.isEmpty()) {
            throw new ValidationException("No se detectaron números válidos en la entrada.");
        }

        // El constructor de DataSet se encarga de la ordenación y validación final.
        return new DataSet(numbers);
    }

    // --- Métodos de Formateo de Resultados (Lógica de Presentación) ---

    private String formatDataSetInfo(DataSet dataSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- ANÁLISIS ESTADÍSTICO DESCRIPTIVO ---\n");
        sb.append(String.format("Tipo de Datos: %s\n", dataTypeComboBox.getValue()));
        sb.append(String.format("Cantidad de Datos (N): %d\n", dataSet.getSize()));
        String sortedDataPreview = dataSet.getSortedData().stream()
                .limit(20)
                .map(d -> String.format("%.2f", d))
                .collect(Collectors.joining(", "));
        if (dataSet.getSize() > 20) {
            sortedDataPreview += ", ... y más";
        }
        sb.append(String.format("Primeros Datos Ordenados: %s", sortedDataPreview));
        return sb.toString();
    }

    private String formatPositionMeasures(DataSet dataSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- MEDIDAS DE POSICIÓN ---\n");
        sb.append(String.format("Media (μ/x̄): %.4f\n", dataSet.getMean()));
        sb.append(String.format("Mediana (Q2): %.4f\n", dataSet.getMedian()));

        List<Double> modes = dataSet.getMode();
        String modeStr = modes.isEmpty() ? "No hay moda (amodal)" :
                modes.size() == 1 ? String.format("%.4f", modes.get(0)) :
                        "Múltiple: " + modes.stream().map(d -> String.format("%.4f", d)).collect(Collectors.joining(", "));
        sb.append(String.format("Moda: %s\n", modeStr));

        sb.append(String.format("Primer Cuartil (Q1): %.4f\n", dataSet.getQuartile(1)));
        sb.append(String.format("Tercer Cuartil (Q3): %.4f\n", dataSet.getQuartile(3)));
        return sb.toString();
    }

    private String formatDispersionMeasures(DataSet dataSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- MEDIDAS DE DISPERSIÓN ---\n");
        sb.append(String.format("Rango: %.4f\n", dataSet.getRange()));
        sb.append(String.format("Rango Intercuartílico (RIQ): %.4f\n", dataSet.getInterquartileRange()));
        sb.append(String.format("Varianza (σ²): %.4f\n", dataSet.getVariance()));
        sb.append(String.format("Desviación Estándar (σ): %.4f\n", dataSet.getStandardDeviation()));
        sb.append(String.format("Coeficiente de Variación (CV): %.2f%%\n", dataSet.getCoefficientOfVariation()));

        double kurtosis = dataSet.getKurtosis();
        String interpretation = kurtosis < -0.25 ? "Platicúrtica (aplanada)" :
                kurtosis > 0.25 ? "Leptocúrtica (apuntada)" : "Mesocúrtica (Normal)";
        sb.append(String.format("Coeficiente de Curtosis (g₂): %.4f → %s\n", kurtosis, interpretation));
        return sb.toString();
    }

    private String formatFrequenciesTable(DataSet dataSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- TABLA DE FRECUENCIAS ---\n");
        Map<Double, Long> frequencies = dataSet.getAbsoluteFrequencies();
        long total = dataSet.getSize();

        sb.append(String.format("%-12s %-10s %-10s %-15s %-15s\n", "Valor (xi)", "f (Abs)", "F (Abs)", "fr (Rel %)", "Fr (Rel %)"));
        sb.append("-".repeat(65)).append("\n");

        long cumulativeAbs = 0;
        double cumulativeRel = 0.0;
        for (Map.Entry<Double, Long> entry : frequencies.entrySet()) {
            double value = entry.getKey();
            long absFreq = entry.getValue();
            cumulativeAbs += absFreq;
            double relFreq = (absFreq * 100.0) / total;
            cumulativeRel += relFreq;
            sb.append(String.format("%-12.2f %-10d %-10d %-15.2f %-15.2f\n", value, absFreq, cumulativeAbs, relFreq, cumulativeRel));
        }
        return sb.toString();
    }
}