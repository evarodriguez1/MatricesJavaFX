package com.calculos.controllers;

import com.calculos.models.BinomialDistribution;
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
 * Controlador para la vista de la Distribución Binomial.
 * Implementa la arquitectura estándar de Controlador Delgado (Thin Controller),
 * gestiona una UI reactiva y presenta los resultados de forma numérica y gráfica.
 *
 * @author Tu Nombre (Equipo de Desarrollo)
 */
public final class BinomialController extends BaseController {

    // --- Componentes FXML de la Vista ---
    @FXML private TextField nField;
    @FXML private TextField pField;
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
        EXACT("P(X = k) - Exacto", 1),
        MAXIMUM("P(X <= k) - Como Máximo", 1),
        MINIMUM("P(X >= k) - Como Mínimo", 1),
        RANGE("P(a <= X <= b) - Rango", 2);

        private final String displayName;
        private final int requiredFields;

        CalculationType(String displayName, int requiredFields) {
            this.displayName = displayName;
            this.requiredFields = requiredFields;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Configura el estado inicial de la vista y los listeners de UI reactiva.
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
            BinomialDistribution dist = new BinomialDistribution(inputs.n(), inputs.p());
            CalculationResult results = calculateResults(dist, inputs);
            displayResults(results, dist);
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
        nField.clear();
        pField.clear();
        k1Field.clear();
        k2Field.clear();
        resultArea.clear();
        distributionChart.getData().clear();
        calculationTypeComboBox.getSelectionModel().selectFirst();
    }

    // --- Métodos Auxiliares Descompuestos ---

    private UserInput readAndValidateUserInput() throws ValidationException {
        int n = InputValidator.parsePositiveInt(nField.getText(), "N (Total Ensayos)");
        double p = InputValidator.parseDoubleInRange(pField.getText(), "P (Prob. Éxito)", 0.0, 1.0);

        CalculationType selectedType = Objects.requireNonNull(calculationTypeComboBox.getValue(), "Tipo de cálculo no seleccionado.");

        int k1 = InputValidator.parseNonNegativeInt(k1Field.getText(), "Valor de k/a");
        int k2 = 0;
        if (selectedType == CalculationType.RANGE) {
            k2 = InputValidator.parseNonNegativeInt(k2Field.getText(), "Valor de b");
        }

        return new UserInput(n, p, selectedType, k1, k2);
    }

    private CalculationResult calculateResults(BinomialDistribution dist, UserInput inputs) throws ValidationException {
        int desde;
        int hasta;

        switch (inputs.type()) {
            case EXACT -> desde = hasta = inputs.k1();
            case MAXIMUM -> {
                desde = 0;
                hasta = inputs.k1();
            }
            case MINIMUM -> {
                desde = inputs.k1();
                hasta = inputs.n();
            }
            case RANGE -> {
                desde = inputs.k1();
                hasta = inputs.k2();
            }
            default -> throw new IllegalStateException("Tipo de cálculo inesperado: " + inputs.type());
        }

        if (desde > hasta) {
            throw new ValidationException("El valor 'desde' (" + desde + ") no puede ser mayor que 'hasta' (" + hasta + ").");
        }

        double probability = dist.getProbabilityRange(desde, hasta);
        return new CalculationResult(probability, desde, hasta);
    }

    private void displayResults(CalculationResult result, BinomialDistribution dist) {
        resultArea.setText(formatResultText(result, dist));
        updateDistributionChart(dist, result);
    }

    private String formatResultText(CalculationResult result, BinomialDistribution dist) {
        String probDescription = getProbabilityDescription(result);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("PROBABILIDAD %s:\n", probDescription));
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

    private void updateDistributionChart(BinomialDistribution dist, CalculationResult result) {
        distributionChart.getData().clear();
        distributionChart.setAnimated(true);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        Map<Integer, Double> fullDist = dist.getFullDistribution();

        // Determina un límite superior razonable para la gráfica para no sobrecargarla.
        int upperLimit = (int) Math.max(20, dist.getMean() + 4 * Math.sqrt(dist.getVariance()));
        int domainLimit = fullDist.keySet().stream().mapToInt(v->v).max().orElse(0);
        upperLimit = Math.min(upperLimit, domainLimit);

        IntStream.rangeClosed(0, upperLimit)
                .forEach(k -> {
                    XYChart.Data<String, Number> data = new XYChart.Data<>(String.valueOf(k), fullDist.getOrDefault(k, 0.0));
                    series.getData().add(data);
                });

        distributionChart.getData().add(series);

        for (XYChart.Data<String, Number> data : series.getData()) {
            int k = Integer.parseInt(data.getXValue());
            if (k >= result.from() && k <= result.to()) {
                data.getNode().setStyle("-fx-bar-fill: #FFD600;");
            } else {
                data.getNode().setStyle("-fx-bar-fill: #6A1B9A;");
            }
        }
        distributionChart.setAnimated(false);
    }

    private record UserInput(int n, double p, CalculationType type, int k1, int k2) {}
    private record CalculationResult(double probability, int from, int to) {}
}