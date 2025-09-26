package com.calculos.controllers;

import com.calculos.models.DataSet;
import com.calculos.utils.InputValidator;
import com.calculos.utils.exceptions.ValidationException;
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
 * Gestiona la entrada de datos, delega la validación y el parseo a métodos especializados,
 * crea un objeto de dominio {@link DataSet} para los cálculos y formatea los resultados para la UI.
 */
public class StatisticsController extends BaseController {

    // --- Componentes FXML de la Vista ---
    @FXML private ComboBox<DataType> dataTypeComboBox;
    @FXML private TextField dataInputField;
    @FXML private ComboBox<ResultType> resultTypeComboBox;
    @FXML private TextArea resultArea;

    // Estado del controlador: guarda el último conjunto de datos válido.
    private DataSet currentDataSet;

    /**
     * Enum interno para los tipos de datos.
     */
    private enum DataType {
        CONTINUOUS("Continuos"), DISCRETE("Discretos");
        private final String displayName;
        DataType(String name) { this.displayName = name; }
        @Override public String toString() { return displayName; }
    }

    /**
     * Enum interno para los tipos de resultados.
     */
    private enum ResultType {
        POSITION("Medidas de Posición"),
        DISPERSION("Medidas de Dispersión"),
        FREQUENCIES("Tabla de Frecuencias");
        private final String displayName;
        ResultType(String name) { this.displayName = name; }
        @Override public String toString() { return displayName; }
    }

    /**
     * Configura el estado inicial de la vista.
     */
    @FXML
    public void initialize() {
        dataTypeComboBox.getItems().setAll(DataType.values());
        dataTypeComboBox.getSelectionModel().select(DataType.CONTINUOUS);

        resultTypeComboBox.getItems().setAll(ResultType.values());
        resultTypeComboBox.setPromptText("Calcular una medida específica...");
    }

    /**
     * Orquesta el cálculo y presentación de TODAS las medidas estadísticas.
     */
    @FXML
    private void onSolveAll() {
        try {
            currentDataSet = createDataSetFromInput();

            StringBuilder sb = new StringBuilder();
            sb.append(formatDataSetInfo(currentDataSet));
            sb.append("\n======================================\n\n");
            sb.append(formatPositionMeasures(currentDataSet));
            sb.append("\n======================================\n\n");
            sb.append(formatDispersionMeasures(currentDataSet));
            sb.append("\n======================================\n\n");
            sb.append(formatFrequenciesTable(currentDataSet));

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
            // No usamos popup, es una no-acción
            return;
        }

        try {
            // Revalida los datos si el campo de texto ha sido modificado
            if (currentDataSet == null) {
                currentDataSet = createDataSetFromInput();
            }

            String resultText = switch(selectedType) {
                case POSITION -> formatPositionMeasures(currentDataSet);
                case DISPERSION -> formatDispersionMeasures(currentDataSet);
                case FREQUENCIES -> formatFrequenciesTable(currentDataSet);
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
        currentDataSet = null; // Borra los datos en memoria
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
        // Usa una expresión regular para dividir por uno o más espacios, comas o saltos de línea.
        String[] parts = input.trim().split("[\\s,;\\n]+");

        for (String part : parts) {
            if (part.isEmpty()) continue;
            double value = InputValidator.parseDouble(part, "Datos de entrada");
            if (dataType == DataType.DISCRETE && value % 1 != 0) {
                throw new ValidationException("Se seleccionó 'Discretos', pero se encontró el valor no entero: " + value);
            }
            numbers.add(value);
        }

        if (numbers.isEmpty()) {
            throw new ValidationException("No se detectaron números válidos en la entrada.");
        }

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
            sortedDataPreview += ", ...";
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