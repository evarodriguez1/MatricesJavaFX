package com.calculos.controllers;

import com.calculos.models.NormalDistribution;
import com.calculos.utils.InputValidator;
import com.calculos.utils.exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.util.Objects;

/**
 * Controlador para la vista de la Distribución Normal (Gaussiana).
 * Gestiona la UI, valida las entradas del usuario y presenta los resultados
 * numéricos y gráficos, delegando los cálculos al modelo NormalDistribution.
 */
public class NormalController extends BaseController {

    // --- Componentes FXML de la Vista ---
    @FXML private TextField meanField;
    @FXML private TextField varianceField;
    @FXML private ComboBox<CalculationType> calculationTypeComboBox;
    @FXML private TextField aField;
    @FXML private Label bLabel;
    @FXML private TextField bField;
    @FXML private TextArea resultArea;
    @FXML private AreaChart<Number, Number> distributionChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    /**
     * Enum interno para representar los tipos de cálculo de forma segura.
     */
    private enum CalculationType {
        LESS_THAN_OR_EQUAL("P(X <= a) - Menor o Igual"),
        GREATER_THAN_OR_EQUAL("P(X >= a) - Mayor o Igual"),
        RANGE("P(a <= X <= b) - Rango");

        private final String displayName;
        CalculationType(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    /**
     * Configura el estado inicial de la vista y los listeners.
     */
    @FXML
    public void initialize() {
        calculationTypeComboBox.getItems().setAll(CalculationType.values());
        calculationTypeComboBox.getSelectionModel().selectFirst();

        // UI Reactiva: el segundo campo solo es visible para cálculos de rango.
        bLabel.visibleProperty().bind(calculationTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(CalculationType.RANGE));
        bField.visibleProperty().bind(calculationTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(CalculationType.RANGE));

        // Estilo inicial del gráfico
        distributionChart.setLegendVisible(false);
    }

    /**
     * Orquesta el flujo de cálculo al presionar el botón.
     */
    @FXML
    private void onSolve() {
        try {
            UserInput inputs = readAndValidateUserInput();
            NormalDistribution dist = new NormalDistribution(inputs.mean(), inputs.stdDev());
            CalculationResult result = calculateResults(dist, inputs);
            displayResults(result, dist);

        } catch (ValidationException e) {
            handleValidationException(e);
        } catch (Exception e) {
            handleGenericException(e);
        }
    }

    /**
     * Limpia todos los campos y resultados.
     */
    @FXML
    private void onClear() {
        meanField.clear();
        varianceField.clear();
        aField.clear();
        bField.clear();
        resultArea.clear();
        distributionChart.getData().clear();
        calculationTypeComboBox.getSelectionModel().selectFirst();
    }

    // --- Métodos Auxiliares Descompuestos ---

    private UserInput readAndValidateUserInput() throws ValidationException {
        double mean = InputValidator.parseDouble(meanField.getText(), "Media (μ)");
        double variance = InputValidator.parseDouble(varianceField.getText(), "Varianza (σ²)");
        if (variance < 0) {
            throw new ValidationException("La Varianza no puede ser negativa.");
        }

        CalculationType type = Objects.requireNonNull(calculationTypeComboBox.getValue(), "Tipo de cálculo no seleccionado.");

        double a = InputValidator.parseDouble(aField.getText(), "Límite 'a'");
        double b = Double.NaN; // Valor por defecto
        if (type == CalculationType.RANGE) {
            b = InputValidator.parseDouble(bField.getText(), "Límite 'b'");
        }

        return new UserInput(mean, Math.sqrt(variance), type, a, b);
    }

    private CalculationResult calculateResults(NormalDistribution dist, UserInput inputs) {
        double probability;
        double a = inputs.a();
        double b = inputs.b();

        // Asegurar que a <= b para cálculos de rango
        if (inputs.type() == CalculationType.RANGE && a > b) {
            double temp = a; a = b; b = temp;
        }

        probability = switch (inputs.type()) {
            case LESS_THAN_OR_EQUAL -> dist.getCumulativeProbability(a);
            case GREATER_THAN_OR_EQUAL -> dist.getComplementaryProbability(a);
            case RANGE -> dist.getRangeProbability(a, b);
        };

        return new CalculationResult(probability, a, b);
    }

    private void displayResults(CalculationResult result, NormalDistribution dist) {
        resultArea.setText(formatResultText(result, dist));
        updateDistributionChart(result, dist);
    }

    private String formatResultText(CalculationResult result, NormalDistribution dist) {
        StringBuilder sb = new StringBuilder();
        String probDescription;
        CalculationType type = calculationTypeComboBox.getValue();

        switch(type) {
            case LESS_THAN_OR_EQUAL -> probDescription = String.format("P(X <= %.4f)", result.a());
            case GREATER_THAN_OR_EQUAL -> probDescription = String.format("P(X >= %.4f)", result.a());
            case RANGE -> probDescription = String.format("P(%.4f <= X <= %.4f)", result.a(), result.b());
            default -> probDescription = "";
        }

        sb.append(String.format("PROBABILIDAD %s:\n", probDescription));
        sb.append(String.format("Resultado: %.8f\n", result.probability()));
        sb.append(String.format("Porcentaje: %.2f%%\n", result.probability() * 100));
        sb.append("\n----------------------------------\n");
        sb.append("VALORES ESTANDARIZADOS (Z-SCORES):\n");
        sb.append(String.format("Z-score para 'a' (%.4f): %.4f\n", result.a(), dist.calculateZScore(result.a())));
        if (type == CalculationType.RANGE) {
            sb.append(String.format("Z-score para 'b' (%.4f): %.4f\n", result.b(), dist.calculateZScore(result.b())));
        }
        return sb.toString();
    }

    /**
     * Dibuja la curva de la Campana de Gauss y sombrea el área de probabilidad.
     */
    private void updateDistributionChart(CalculationResult result, NormalDistribution dist) {
        distributionChart.getData().clear();
        distributionChart.setAnimated(false);

        double mean = dist.getMean();
        double stdDev = dist.getStandardDeviation();

        // Define los límites de la gráfica
        double lowerBound = mean - 4 * stdDev;
        double upperBound = mean + 4 * stdDev;
        xAxis.setLowerBound(lowerBound);
        xAxis.setUpperBound(upperBound);
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(true);

        // Serie para la curva principal
        XYChart.Series<Number, Number> curveSeries = new XYChart.Series<>();
        for (double x = lowerBound; x <= upperBound; x += (upperBound - lowerBound) / 500.0) {
            curveSeries.getData().add(new XYChart.Data<>(x, dist.getDensity(x)));
        }

        // Serie para el área sombreada
        XYChart.Series<Number, Number> areaSeries = new XYChart.Series<>();
        double start = switch(calculationTypeComboBox.getValue()) {
            case LESS_THAN_OR_EQUAL, RANGE, GREATER_THAN_OR_EQUAL -> result.a();
        };
        double end = switch(calculationTypeComboBox.getValue()) {
            case LESS_THAN_OR_EQUAL -> result.a();
            case GREATER_THAN_OR_EQUAL -> upperBound;
            case RANGE -> result.b();
        };

        if (calculationTypeComboBox.getValue() == CalculationType.LESS_THAN_OR_EQUAL) start = lowerBound;

        for (double x = start; x <= end; x += (upperBound - lowerBound) / 500.0) {
            areaSeries.getData().add(new XYChart.Data<>(x, dist.getDensity(x)));
        }

        distributionChart.getData().addAll(curveSeries, areaSeries);

        // Aplicar estilos
        areaSeries.getNode().lookup(".chart-series-area-fill").setStyle("-fx-fill: #FFD60088;"); // Amarillo traslúcido
        curveSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: #4A148C; -fx-stroke-width: 2px;");
    }

    // --- Records Internos para una estructura de datos limpia ---

    private record UserInput(double mean, double stdDev, CalculationType type, double a, double b) {}
    private record CalculationResult(double probability, double a, double b) {}
}