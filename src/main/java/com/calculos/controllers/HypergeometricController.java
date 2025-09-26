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

/**
 * Controlador para la vista de la Distribución Hipergeométrica.
 * Orquesta la validación, cálculo y presentación de resultados, delegando la lógica
 * de negocio al modelo HypergeometricDistribution y siguiendo el patrón de BaseController.
 */
public class HypergeometricController extends BaseController {

    // --- Componentes FXML de la Vista ---
    @FXML private TextField nField; // Población
    @FXML private TextField KField; // Éxitos en Población
    @FXML private TextField nSampleField; // Muestra
    @FXML private ComboBox<CalculationType> calculationTypeComboBox;
    @FXML private TextField k1Field;
    @FXML private Label k2Label;
    @FXML private TextField k2Field;
    @FXML private TextArea resultArea;
    @FXML private BarChart<String, Number> distributionChart;

    /**
     * Enum interno para representar los tipos de cálculo.
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
     * Configura el estado inicial de la vista y los bindings.
     */
    @FXML
    public void initialize() {
        calculationTypeComboBox.getItems().setAll(CalculationType.values());
        calculationTypeComboBox.getSelectionModel().selectFirst();

        // UI Reactiva: el segundo campo solo es visible para rangos.
        k2Label.visibleProperty().bind(calculationTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(CalculationType.RANGE));
        k2Field.visibleProperty().bind(calculationTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(CalculationType.RANGE));
    }

    /**
     * Maneja el evento de clic en el botón "Calcular".
     */
    @FXML
    private void onSolve() {
        try {
            UserInput inputs = readAndValidateUserInput();
            HypergeometricDistribution dist = new HypergeometricDistribution(inputs.N(), inputs.K(), inputs.n());
            CalculationResult result = calculateResults(dist, inputs);
            displayResults(result, dist);

        } catch (ValidationException e) {
            handleValidationException(e);
        } catch (Exception e) {
            handleGenericException(e);
        }
    }

    /**
     * Limpia todos los campos a su estado por defecto.
     */
    @FXML
    private void onClear() {
        nField.clear();
        KField.clear();
        nSampleField.clear();
        k1Field.clear();
        k2Field.clear();
        resultArea.clear();
        distributionChart.getData().clear();
        calculationTypeComboBox.getSelectionModel().selectFirst();
    }

    // --- Métodos Auxiliares Descompuestos ---

    private UserInput readAndValidateUserInput() throws ValidationException {
        int N = InputValidator.parsePositiveInt(nField.getText(), "N (Población Total)");
        int K = InputValidator.parseNonNegativeInt(KField.getText(), "K (Éxitos Totales)");
        int n = InputValidator.parseNonNegativeInt(nSampleField.getText(), "n (Tamaño Muestra)");

        if (K > N) throw new ValidationException("K (éxitos) no puede ser mayor que N (población).");
        if (n > N) throw new ValidationException("n (muestra) no puede ser mayor que N (población).");

        CalculationType type = Objects.requireNonNull(calculationTypeComboBox.getValue());

        int k1 = InputValidator.parseNonNegativeInt(k1Field.getText(), "Valor de k/a");
        int k2 = 0; // Valor por defecto
        if (type == CalculationType.RANGE) {
            k2 = InputValidator.parseNonNegativeInt(k2Field.getText(), "Valor de b");
        }

        return new UserInput(N, K, n, type, k1, k2);
    }

    private CalculationResult calculateResults(HypergeometricDistribution dist, UserInput inputs) throws ValidationException {
        int desde, hasta;
        switch (inputs.type()) {
            case EXACT -> desde = hasta = inputs.k1();
            case MAXIMUM -> {
                desde = 0;
                hasta = inputs.k1();
            }
            case MINIMUM -> {
                desde = inputs.k1();
                hasta = Math.min(inputs.n(), inputs.K());
            }
            case RANGE -> {
                desde = inputs.k1();
                hasta = inputs.k2();
            }
            default -> throw new IllegalStateException("Tipo de cálculo inesperado.");
        }

        double probability = dist.getProbabilityRange(desde, hasta);
        return new CalculationResult(probability, desde, hasta);
    }

    private void displayResults(CalculationResult result, HypergeometricDistribution dist) {
        resultArea.setText(formatResultText(result, dist));
        updateDistributionChart(dist, result);
    }

    private String formatResultText(CalculationResult result, HypergeometricDistribution dist) {
        StringBuilder sb = new StringBuilder();
        String probDescription = getProbabilityDescription(result);

        sb.append("PROBABILIDAD HIPERGEOMÉTRICA:\n");
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

    private void updateDistributionChart(HypergeometricDistribution dist, CalculationResult result) {
        distributionChart.getData().clear();
        distributionChart.setAnimated(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("P(X=k)");

        Map<Integer, Double> fullDist = dist.getFullDistribution();

        // El dominio es de 0 al máximo de éxitos posibles
        int maxSuccesses = (int) (dist.getMean() * 2 > 10 ? (int)(dist.getMean() * 2) : 10);
        IntStream.rangeClosed(0, maxSuccesses)
                .forEach(k -> {
                    XYChart.Data<String, Number> data = new XYChart.Data<>(String.valueOf(k), fullDist.getOrDefault(k, 0.0));
                    series.getData().add(data);
                });

        distributionChart.getData().add(series);

        // Resaltar barras del resultado
        for(XYChart.Data<String, Number> data : series.getData()) {
            int k = Integer.parseInt(data.getXValue());
            if (k >= result.from() && k <= result.to()) {
                data.getNode().setStyle("-fx-bar-fill: #FFD600;");
            } else {
                data.getNode().setStyle("-fx-bar-fill: #4A148C;");
            }
        }
    }

    // --- Records Internos ---
    private record UserInput(int N, int K, int n, CalculationType type, int k1, int k2) {}
    private record CalculationResult(double probability, int from, int to) {}
}