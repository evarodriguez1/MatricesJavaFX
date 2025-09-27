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
 *
 * @author Tu Nombre (Equipo de Desarrollo)
 */
public final class NormalController extends BaseController {

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
     * Enum interno para representar los tipos de cálculo de forma segura y expresiva.
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
     * Configura el estado inicial de la vista y los listeners de UI reactiva.
     */
    @FXML
    public void initialize() {
        calculationTypeComboBox.getItems().setAll(CalculationType.values());
        calculationTypeComboBox.getSelectionModel().selectFirst();

        // UI Reactiva: el segundo campo solo es visible para cálculos de rango.
        bLabel.visibleProperty().bind(calculationTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(CalculationType.RANGE));
        bField.visibleProperty().bind(calculationTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(CalculationType.RANGE));

        distributionChart.setLegendVisible(false);
    }

    /**
     * Orquesta el flujo de cálculo al presionar el botón de "Resolver".
     */
    @FXML
    private void onSolve() {
        try {
            UserInput inputs = readAndValidateUserInput();
            NormalDistribution dist = new NormalDistribution(inputs.mean(), inputs.stdDev());
            CalculationResult result = calculateResults(dist, inputs);
            displayResults(result, dist);
        } catch (ValidationException | IllegalArgumentException e) {
            handleValidationException(new ValidationException(e.getMessage()));
        } catch (Exception e) {
            handleGenericException(e);
        }
    }

    /**
     * Limpia todos los campos de la interfaz a su estado inicial.
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

    // --- Métodos Auxiliares Descompuestos (Thin Controller) ---

    private UserInput readAndValidateUserInput() throws ValidationException {
        double mean = InputValidator.parseDouble(meanField.getText(), "Media (μ)");
        double variance = InputValidator.parseDouble(varianceField.getText(), "Varianza (σ²)");
        if (variance <= 0) { // La varianza debe ser estrictamente positiva para la Normal
            throw new ValidationException("La Varianza debe ser un número positivo (mayor que cero).");
        }

        CalculationType type = Objects.requireNonNull(calculationTypeComboBox.getValue(), "Tipo de cálculo no seleccionado.");

        double a = InputValidator.parseDouble(aField.getText(), "Límite 'a'");
        double b = Double.NaN;
        if (type == CalculationType.RANGE) {
            b = InputValidator.parseDouble(bField.getText(), "Límite 'b'");
        }

        return new UserInput(mean, Math.sqrt(variance), type, a, b);
    }

    private CalculationResult calculateResults(NormalDistribution dist, UserInput inputs) throws ValidationException {
        double a = inputs.a();
        double b = inputs.b();

        if (inputs.type() == CalculationType.RANGE && a > b) {
            double temp = a; a = b; b = temp; // Swap para asegurar a <= b
        }

        double probability = switch (inputs.type()) {
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
        String probDescription = getProbabilityDescription(result);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("PROBABILIDAD %s:\n", probDescription));
        sb.append(String.format("Resultado: %.8f\n", result.probability()));
        sb.append(String.format("Porcentaje: %.2f%%\n", result.probability() * 100));
        sb.append("\n----------------------------------\n");
        sb.append("VALORES ESTANDARIZADOS (Z-SCORES):\n");
        sb.append(String.format("Z-score para 'a' (%.4f): %.4f\n", result.a(), dist.calculateZScore(result.a())));
        if (calculationTypeComboBox.getValue() == CalculationType.RANGE) {
            sb.append(String.format("Z-score para 'b' (%.4f): %.4f\n", result.b(), dist.calculateZScore(result.b())));
        }
        return sb.toString();
    }

    private String getProbabilityDescription(CalculationResult result) {
        return switch (calculationTypeComboBox.getValue()) {
            case LESS_THAN_OR_EQUAL -> String.format("P(X <= %.4f)", result.a());
            case GREATER_THAN_OR_EQUAL -> String.format("P(X >= %.4f)", result.a());
            case RANGE -> String.format("P(%.4f <= X <= %.4f)", result.a(), result.b());
        };
    }

    /**
     * Dibuja la curva de la Campana de Gauss y sombrea el área de probabilidad correspondiente.
     * @param result El resultado del cálculo que define qué área sombrear.
     * @param dist El modelo de la distribución para obtener la forma de la curva.
     */
    private void updateDistributionChart(CalculationResult result, NormalDistribution dist) {
        distributionChart.getData().clear();
        distributionChart.setAnimated(true);

        double mean = dist.getMean();
        double stdDev = dist.getStandardDeviation();

        // Define los límites de la gráfica para cubrir ~99.9% de la distribución
        double lowerBound = mean - 4 * stdDev;
        double upperBound = mean + 4 * stdDev;
        xAxis.setLowerBound(lowerBound);
        xAxis.setUpperBound(upperBound);
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(true); // El eje Y se ajusta automáticamente a la altura máxima de la curva

        // Serie para la curva principal
        XYChart.Series<Number, Number> curveSeries = createCurveSeries(dist, lowerBound, upperBound);

        // Serie para el área sombreada
        XYChart.Series<Number, Number> areaSeries = createAreaSeries(dist, result, lowerBound, upperBound);

        distributionChart.getData().addAll(curveSeries, areaSeries);

        // Aplicar estilos directamente a los nodos. Es más robusto que el CSS para áreas dinámicas.
        areaSeries.getNode().lookup(".chart-series-area-fill").setStyle("-fx-fill: #FFD60088;"); // Amarillo traslúcido
        curveSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: #4A148C; -fx-stroke-width: 2px;");

        distributionChart.setAnimated(false);
    }

    private XYChart.Series<Number, Number> createCurveSeries(NormalDistribution dist, double lowerBound, double upperBound) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        final int steps = 500; // Número de puntos para dibujar la curva
        for (int i = 0; i <= steps; i++) {
            double x = lowerBound + (i * (upperBound - lowerBound) / steps);
            series.getData().add(new XYChart.Data<>(x, dist.getDensity(x)));
        }
        return series;
    }

    private XYChart.Series<Number, Number> createAreaSeries(NormalDistribution dist, CalculationResult result, double lowerBound, double upperBound) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        double start, end;

        switch (calculationTypeComboBox.getValue()) {
            case LESS_THAN_OR_EQUAL -> {
                start = lowerBound;
                end = result.a();
            }
            case GREATER_THAN_OR_EQUAL -> {
                start = result.a();
                end = upperBound;
            }
            case RANGE -> {
                start = result.a();
                end = result.b();
            }
            default -> {
                return series; // No sombrear nada si el tipo es desconocido
            }
        }

        // Asegurarse de que el área a sombrear no exceda los límites de la gráfica
        start = Math.max(start, lowerBound);
        end = Math.min(end, upperBound);

        final int steps = 200; // Menos puntos son suficientes para el área
        for (int i = 0; i <= steps; i++) {
            double x = start + (i * (end - start) / steps);
            series.getData().add(new XYChart.Data<>(x, dist.getDensity(x)));
        }
        return series;
    }

    // --- Records Internos para una estructura de datos limpia ---

    private record UserInput(double mean, double stdDev, CalculationType type, double a, double b) {}
    private record CalculationResult(double probability, double a, double b) {}
}