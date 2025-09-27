package com.calculos.controllers;

import com.calculos.models.PoissonDistribution;
import com.calculos.utils.InputValidator;
import com.calculos.utils.exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Controlador para la vista de la Distribución de Poisson.
 * Gestiona la UI, valida la entrada, delega los cálculos al modelo PoissonDistribution
 * y presenta los resultados de forma numérica y gráfica. Sigue el patrón MVC de élite.
 *
 * @author Tu Nombre (Equipo de Desarrollo)
 */
public final class PoissonController extends BaseController {

    // --- Componentes FXML de la Vista ---
    @FXML private TextField lambdaField;
    @FXML private ComboBox<CalculationType> calculationTypeComboBox;
    @FXML private TextField k1Field;
    @FXML private Label k2Label;
    @FXML private TextField k2Field;
    @FXML private TextArea resultArea;
    @FXML private BarChart<String, Number> distributionChart;

    /**
     * Enum interno para representar los tipos de cálculo de forma segura y expresiva.
     */
    private enum CalculationType {
        EXACT("P(X = k) - Exacto"),
        MAXIMUM("P(X <= k) - Como Máximo"),
        MINIMUM("P(X >= k) - Como Mínimo"),
        RANGE("P(a <= X <= b) - Rango");

        private final String displayName;
        CalculationType(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    /**
     * Configura el estado inicial de la vista y los bindings de la UI reactiva.
     */
    @FXML
    public void initialize() {
        calculationTypeComboBox.getItems().setAll(CalculationType.values());
        calculationTypeComboBox.getSelectionModel().selectFirst();

        k2Label.visibleProperty().bind(calculationTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(CalculationType.RANGE));
        k2Field.visibleProperty().bind(calculationTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(CalculationType.RANGE));

        distributionChart.setLegendVisible(false);
    }

    /**
     * Orquesta el flujo de cálculo al presionar el botón de "Resolver".
     */
    @FXML
    private void onSolve() {
        try {
            UserInput inputs = readAndValidateUserInput();
            PoissonDistribution dist = new PoissonDistribution(inputs.lambda());
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
        lambdaField.clear();
        k1Field.clear();
        k2Field.clear();
        resultArea.clear();
        distributionChart.getData().clear();
        calculationTypeComboBox.getSelectionModel().selectFirst();
    }

    // --- Métodos Auxiliares Descompuestos ---

    private UserInput readAndValidateUserInput() throws ValidationException {
        double lambda = InputValidator.parseDouble(lambdaField.getText(), "Lambda (λ)");
        if (lambda <= 0) {
            throw new ValidationException("El promedio de ocurrencias (lambda) debe ser positivo.");
        }

        CalculationType type = Objects.requireNonNull(calculationTypeComboBox.getValue());

        int k1 = InputValidator.parseNonNegativeInt(k1Field.getText(), "Ocurrencias (k/a)");
        int k2 = 0;
        if (type == CalculationType.RANGE) {
            k2 = InputValidator.parseNonNegativeInt(k2Field.getText(), "Valor de b");
        }

        return new UserInput(lambda, type, k1, k2);
    }

    private CalculationResult calculateResults(PoissonDistribution dist, UserInput inputs) throws ValidationException {
        double probability;
        int from = inputs.k1();
        int to = (inputs.type() == CalculationType.RANGE) ? inputs.k2() : inputs.k1(); // 'to' inicial

        switch (inputs.type()) {
            case EXACT:
                probability = dist.getProbabilityAt(from);
                to = from;
                break;
            case MAXIMUM:
                probability = dist.getProbabilityRange(0, from);
                to = from; // 'to' es k1
                from = 0;
                break;
            case MINIMUM:
                probability = dist.getComplementaryProbability(from);
                break;
            case RANGE:
                if (from > to) {
                    throw new ValidationException("'Desde' (" + from + ") no puede ser mayor que 'hasta' (" + to + ").");
                }
                probability = dist.getProbabilityRange(from, to);
                break;
            default:
                throw new IllegalStateException("Tipo de cálculo inesperado: " + inputs.type());
        }
        return new CalculationResult(probability, from, to);
    }

    private void displayResults(CalculationResult result, PoissonDistribution dist) {
        resultArea.setText(formatResultText(result, dist));
        updateDistributionChart(dist, result);
    }

    private String formatResultText(CalculationResult result, PoissonDistribution dist) {
        StringBuilder sb = new StringBuilder();
        String probDescription = getProbabilityDescription(result);

        sb.append("PROBABILIDAD DE POISSON:\n");
        sb.append(String.format("Promedio de Ocurrencias (λ): %.4f\n", dist.getMean()));
        sb.append(String.format("Cálculo: %s\n", probDescription));
        sb.append(String.format("Resultado: %.8f\n", result.probability()));
        sb.append(String.format("Porcentaje: %.2f%%\n", result.probability() * 100));
        sb.append("\n----------------------------------\n");
        sb.append("DATOS DE LA DISTRIBUCIÓN:\n");
        sb.append(String.format("Esperanza E[X]: %.4f\n", dist.getMean()));
        sb.append(String.format("Varianza Var[X]: %.4f\n", dist.getVariance()));

        return sb.toString();
    }

    private String getProbabilityDescription(CalculationResult result) {
        return switch (calculationTypeComboBox.getValue()) {
            case EXACT -> String.format("P(X = %d)", result.from());
            case MAXIMUM -> String.format("P(X <= %d)", result.to());
            case MINIMUM -> String.format("P(X >= %d)", result.from());
            case RANGE -> String.format("P(%d <= X <= %d)", result.from(), result.to());
        };
    }

    private void updateDistributionChart(PoissonDistribution dist, CalculationResult result) {
        distributionChart.getData().clear();
        distributionChart.setAnimated(true);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        Map<Integer, Double> fullDist = dist.getDistributionForGraphing();
        int limit = fullDist.keySet().stream().mapToInt(v -> v).max().orElse(0);

        IntStream.rangeClosed(0, limit)
                .forEach(k -> {
                    XYChart.Data<String, Number> data = new XYChart.Data<>(String.valueOf(k), fullDist.getOrDefault(k, 0.0));
                    series.getData().add(data);
                });

        distributionChart.getData().add(series);

        for(XYChart.Data<String, Number> data : series.getData()) {
            int k = Integer.parseInt(data.getXValue());
            CalculationType type = calculationTypeComboBox.getValue();

            boolean shouldHighlight = switch(type) {
                case EXACT -> k == result.from();
                case MAXIMUM -> k <= result.to();
                case MINIMUM -> k >= result.from();
                case RANGE -> k >= result.from() && k <= result.to();
            };

            if (shouldHighlight) {
                data.getNode().setStyle("-fx-bar-fill: #FFD600;");
            } else {
                data.getNode().setStyle("-fx-bar-fill: #6A1B9A;");
            }
        }
        distributionChart.setAnimated(false);
    }

    private record UserInput(double lambda, CalculationType type, int k1, int k2) {}
    private record CalculationResult(double probability, int from, int to) {}
}