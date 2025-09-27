package com.calculos.controllers;

import com.calculos.models.HypergeometricDistribution;
import com.calculos.utils.InputValidator;
import com.calculos.utils.exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public final class HypergeometricController extends BaseController {

    @FXML private TextField nPopField;
    @FXML private TextField KField;
    @FXML private TextField nSampleField;
    @FXML private ComboBox<CalculationType> calculationTypeComboBox;
    @FXML private TextField k1Field;
    @FXML private Label k2Label;
    @FXML private TextField k2Field;
    @FXML private TextArea resultArea;
    @FXML private BarChart<String, Number> distributionChart;

    private enum CalculationType {
        EXACT("P(X = k) - Exacto"),
        MAXIMUM("P(X <= k) - Como Máximo"),
        MINIMUM("P(X >= k) - Como Mínimo"),
        RANGE("P(a <= X <= b) - Rango");

        private final String displayName;
        CalculationType(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    @FXML public void initialize() {
        calculationTypeComboBox.getItems().setAll(CalculationType.values());
        calculationTypeComboBox.getSelectionModel().selectFirst();
        k2Label.visibleProperty().bind(calculationTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(CalculationType.RANGE));
        k2Field.visibleProperty().bind(calculationTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(CalculationType.RANGE));
        distributionChart.setLegendVisible(false);
    }

    @FXML private void onSolve() {
        try {
            UserInput inputs = readAndValidateUserInput();
            HypergeometricDistribution dist = new HypergeometricDistribution(inputs.N(), inputs.K(), inputs.n());
            CalculationResult result = calculateResults(dist, inputs);
            displayResults(result, dist);
        } catch (ValidationException | IllegalArgumentException e) {
            handleValidationException(new ValidationException(e.getMessage()));
        } catch (Exception e) {
            handleGenericException(e);
        }
    }

    @FXML private void onClear() {
        nPopField.clear(); KField.clear(); nSampleField.clear(); k1Field.clear(); k2Field.clear(); resultArea.clear();
        distributionChart.getData().clear();
        calculationTypeComboBox.getSelectionModel().selectFirst();
    }

    private UserInput readAndValidateUserInput() throws ValidationException {
        int N = InputValidator.parsePositiveInt(nPopField.getText(), "N (Población Total)");
        int K = InputValidator.parseNonNegativeInt(KField.getText(), "K (Éxitos en Población)");
        int n = InputValidator.parseNonNegativeInt(nSampleField.getText(), "n (Tamaño Muestra)");

        if (K > N) throw new ValidationException("K (éxitos) no puede ser mayor que N (población).");
        if (n > N) throw new ValidationException("n (muestra) no puede ser mayor que N (población).");

        CalculationType type = Objects.requireNonNull(calculationTypeComboBox.getValue());

        int k1 = InputValidator.parseNonNegativeInt(k1Field.getText(), "Éxitos en Muestra (k/a)");
        int k2 = 0;
        if (type == CalculationType.RANGE) {
            k2 = InputValidator.parseNonNegativeInt(k2Field.getText(), "Valor de b");
        }
        return new UserInput(N, K, n, type, k1, k2);
    }

    private CalculationResult calculateResults(HypergeometricDistribution dist, UserInput inputs) throws ValidationException {
        int desde, hasta;
        switch (inputs.type()) {
            case EXACT -> desde = hasta = inputs.k1();
            case MAXIMUM -> { desde = 0; hasta = inputs.k1(); }
            case MINIMUM -> { desde = inputs.k1(); hasta = Math.min(inputs.n(), inputs.K()); }
            case RANGE -> { desde = inputs.k1(); hasta = inputs.k2(); }
            default -> throw new IllegalStateException("Tipo inesperado: " + inputs.type());
        }
        if (desde > hasta) throw new ValidationException("'Desde' no puede ser mayor que 'hasta'.");

        return new CalculationResult(dist.getProbabilityRange(desde, hasta), desde, hasta);
    }

    private void displayResults(CalculationResult res, HypergeometricDistribution dist) {
        resultArea.setText(formatResultText(res, dist));
        updateDistributionChart(dist, res);
    }

    private String formatResultText(CalculationResult res, HypergeometricDistribution dist) {
        return "PROBABILIDAD HIPERGEOMÉTRICA:\nCálculo: " + getProbabilityDescription(res) +
                String.format("\nResultado: %.8f", res.probability()) +
                String.format("\nPorcentaje: %.2f%%", res.probability() * 100) +
                "\n----------------------------------\nDATOS DE LA DISTRIBUCIÓN:\n" +
                String.format("Esperanza E[X]: %.4f\n", dist.getMean()) +
                String.format("Varianza Var[X]: %.4f", dist.getVariance());
    }

    private String getProbabilityDescription(CalculationResult res) {
        return switch (calculationTypeComboBox.getValue()) {
            case EXACT -> String.format("P(X = %d)", res.from());
            case MAXIMUM -> String.format("P(X <= %d)", res.to());
            case MINIMUM -> String.format("P(X >= %d)", res.from());
            case RANGE -> String.format("P(%d <= X <= %d)", res.from(), res.to());
        };
    }

    private void updateDistributionChart(HypergeometricDistribution dist, CalculationResult res) {
        distributionChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<Integer, Double> fullDist = dist.getFullDistribution();
        int limit = fullDist.keySet().stream().mapToInt(v -> v).max().orElse(0);

        IntStream.rangeClosed(0, limit).forEach(k -> series.getData().add(new XYChart.Data<>(String.valueOf(k), fullDist.getOrDefault(k, 0.0))));
        distributionChart.getData().add(series);

        for (XYChart.Data<String, Number> data : series.getData()) {
            int k = Integer.parseInt(data.getXValue());
            if (k >= res.from() && k <= res.to()) data.getNode().setStyle("-fx-bar-fill: #FFD600;");
            else data.getNode().setStyle("-fx-bar-fill: #6A1B9A;");
        }
    }

    private record UserInput(int N, int K, int n, CalculationType type, int k1, int k2) {}
    private record CalculationResult(double probability, int from, int to) {}
}