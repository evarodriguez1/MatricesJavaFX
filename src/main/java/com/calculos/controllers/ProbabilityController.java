package com.calculos.controllers;

import com.calculos.models.BinomialDistribution;
import com.calculos.utils.InputValidator;
import com.calculos.utils.exceptions.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Controlador para la vista del Módulo de Probabilidades.
 * Orquesta una UI dinámica que, de momento, se enfoca exclusivamente en la
 * Distribución Binomial, implementando un flujo de cálculo en cascada.
 */
public final class ProbabilityController extends BaseController {

    // --- Componentes FXML de la Vista ---
    @FXML private ComboBox<DistributionType> distributionTypeComboBox;
    // Paneles Contenedores
    @FXML private VBox binomialInputPane;
    @FXML private VBox calculationPane;
    @FXML private VBox chartContainer;
    // Campos de entrada Binomial
    @FXML private TextField nField;
    @FXML private TextField pField;
    // Campos de cálculo específico
    @FXML private ComboBox<CalculationType> calculationTypeComboBox;
    @FXML private Label k1Label;
    @FXML private Label k2Label;
    @FXML private TextField k1Field;
    @FXML private TextField k2Field;
    // Botones y Salidas
    @FXML private Button solveButton;
    @FXML private TextArea resultArea;

    /** Enum para gestionar las distribuciones (ampliable en el futuro). */
    private enum DistributionType {
        BINOMIAL("Binomial");
        private final String displayName;
        DistributionType(String name) { this.displayName = name; }
        @Override public String toString() { return displayName; }
    }

    /** Enum para los tipos de cálculo de probabilidad. */
    private enum CalculationType {
        EXACT("P(X = k) - Exacto"),
        MAXIMUM("P(X <= k) - Como Máximo"),
        MINIMUM("P(X >= k) - Como Mínimo"),
        RANGE("P(a <= X <= b) - Rango");
        private final String displayName;
        CalculationType(String name) { this.displayName = name; }
        @Override public String toString() { return displayName; }
    }

    /**
     * Configura el estado inicial y los listeners para la UI dinámica en cascada.
     */
    @FXML
    public void initialize() {
        // 1. Configurar ComboBox principal
        distributionTypeComboBox.getItems().setAll(DistributionType.values());

        // 2. Configurar el flujo en cascada
        // El ChangeListener observa los TextFields de los parámetros principales (n y p).
        ChangeListener<String> parameterListener = (obs, oldVal, newVal) -> checkAndShowCalculationPane();
        nField.textProperty().addListener(parameterListener);
        pField.textProperty().addListener(parameterListener);

        // El listener en el ComboBox de tipo de cálculo ajusta los campos k1/k2
        calculationTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateSpecificInputFields(newVal));

        // 3. Establecer estado inicial
        onClear(); // Reutiliza onClear para establecer el estado inicial limpio

        // 4. Seleccionar Binomial por defecto para iniciar el flujo.
        distributionTypeComboBox.getSelectionModel().selectFirst();
    }

    /**
     * Orquesta el flujo de cálculo final al presionar el botón "Resolver".
     */
    @FXML
    private void onSolve() {
        try {
            // Lee los parámetros base y crea la distribución
            int n = InputValidator.parsePositiveInt(nField.getText(), "Ensayos (n)");
            double p = InputValidator.parseDoubleInRange(pField.getText(), "Probabilidad (p)", 0, 1);
            BinomialDistribution dist = new BinomialDistribution(n, p);

            // Lee los parámetros de cálculo específicos
            SpecificInputs specificInputs = readSpecificInputs(n);

            // Realiza el cálculo de probabilidad
            double probability = dist.getProbabilityRange(specificInputs.from(), specificInputs.to());

            // Formatea y muestra los resultados
            String resultText = formatResultText(dist, specificInputs, probability);
            resultArea.setText(resultText);

            // Actualiza la gráfica
            updateDistributionChart(dist, specificInputs);

        } catch (ValidationException | IllegalArgumentException e) {
            handleValidationException(new ValidationException(e.getMessage()));
        } catch (Exception e) {
            handleGenericException(e);
        }
    }

    @FXML
    private void onClear() {
        // Resetea todos los campos a su estado inicial
        resultArea.clear();
        chartContainer.getChildren().clear();
        nField.clear();
        pField.clear();
        k1Field.clear();
        k2Field.clear();

        // Oculta y resetea la sección de cálculo específico
        calculationPane.setVisible(false);
        calculationPane.setManaged(false);
        calculationTypeComboBox.getItems().clear();

        // Deshabilita el botón de calcular
        solveButton.setDisable(true);
    }

    // --- Lógica de UI Reactiva en Cascada ---

    private void checkAndShowCalculationPane() {
        try {
            // Intenta validar los parámetros base de forma silenciosa.
            InputValidator.parsePositiveInt(nField.getText(), "");
            InputValidator.parseDoubleInRange(pField.getText(), "", 0, 1);

            // Si ambos son válidos, muestra la siguiente sección.
            if (!calculationPane.isVisible()) {
                calculationPane.setVisible(true);
                calculationPane.setManaged(true);
                calculationTypeComboBox.getItems().setAll(CalculationType.values());
                calculationTypeComboBox.getSelectionModel().selectFirst();
            }
        } catch (ValidationException e) {
            // Si alguno falla, la sección permanece oculta.
            calculationPane.setVisible(false);
            calculationPane.setManaged(false);
            solveButton.setDisable(true); // Siempre deshabilitado si los params. base no son válidos
        }
    }

    private void updateSpecificInputFields(CalculationType type) {
        if (type == null) return;

        boolean isRange = type == CalculationType.RANGE;

        // Mostrar u ocultar el segundo campo (para b)
        k2Label.setVisible(isRange);
        k2Label.setManaged(isRange);
        k2Field.setVisible(isRange);
        k2Field.setManaged(isRange);

        // Actualizar el texto del Label de k1 para ser más intuitivo
        switch (type) {
            case EXACT -> k1Label.setText("Nº exacto de éxitos (k):");
            case MAXIMUM -> k1Label.setText("Nº máximo de éxitos (k):");
            case MINIMUM -> k1Label.setText("Nº mínimo de éxitos (k):");
            case RANGE -> k1Label.setText("Valor mínimo (a):");
        }

        solveButton.setDisable(false); // Habilitar el botón de cálculo
    }

    private SpecificInputs readSpecificInputs(int n) throws ValidationException {
        CalculationType type = Objects.requireNonNull(calculationTypeComboBox.getValue(), "Tipo de cálculo no seleccionado.");
        int k1 = InputValidator.parseNonNegativeInt(k1Field.getText(), k1Label.getText());
        int k2 = 0;

        if (type == CalculationType.RANGE) {
            k2 = InputValidator.parseNonNegativeInt(k2Field.getText(), k2Label.getText());
        }

        return switch (type) {
            case EXACT -> new SpecificInputs(k1, k1);
            case MAXIMUM -> new SpecificInputs(0, k1);
            case MINIMUM -> new SpecificInputs(k1, n);
            case RANGE -> new SpecificInputs(k1, k2);
        };
    }

    // --- Lógica de Presentación de Resultados ---

    private String formatResultText(BinomialDistribution dist, SpecificInputs inputs, double probability) {
        StringBuilder sb = new StringBuilder("RESULTADOS:\n");
        CalculationType type = calculationTypeComboBox.getValue();

        String probText = switch(type) {
            case EXACT -> String.format("La probabilidad de obtener exactamente %d éxitos es: %.8f", inputs.from(), probability);
            case MAXIMUM -> String.format("La probabilidad de obtener como máximo %d éxitos (o menos) es: %.8f", inputs.to(), probability);
            case MINIMUM -> String.format("La probabilidad de obtener como mínimo %d éxitos (o más) es: %.8f", inputs.from(), probability);
            case RANGE -> String.format("La probabilidad de obtener entre %d y %d éxitos es: %.8f", inputs.from(), inputs.to(), probability);
        };

        sb.append(probText).append("\n");
        sb.append(String.format("Porcentaje: %.2f%%\n", probability * 100));
        sb.append("\n----------------------------------\n");
        sb.append("OTROS DATOS DE INTERÉS:\n");
        sb.append(String.format("Valor Esperado (Media): %.4f\n", dist.getMean()));
        sb.append(String.format("Varianza: %.4f", dist.getVariance()));

        return sb.toString();
    }

    private void updateDistributionChart(BinomialDistribution dist, SpecificInputs inputs) {
        chartContainer.getChildren().clear();
        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<Integer, Double> fullDist = dist.getFullDistribution();
        int upperLimit = Math.min((int) (dist.getMean() * 2.5 + 5), dist.getMean() > 50 ? 50 : 100);

        IntStream.rangeClosed(0, Math.min(dist.getMean() > 50 ? 50 : 100, fullDist.keySet().stream().mapToInt(v->v).max().orElse(0))).forEach(k -> {
            series.getData().add(new XYChart.Data<>(String.valueOf(k), fullDist.getOrDefault(k, 0.0)));
        });

        chart.getData().add(series);

        for (XYChart.Data<String, Number> data : series.getData()) {
            int k = Integer.parseInt(data.getXValue());
            if (k >= inputs.from() && k <= inputs.to()) {
                data.getNode().setStyle("-fx-bar-fill: #FFD600;");
            } else {
                data.getNode().setStyle("-fx-bar-fill: #6A1B9A;");
            }
        }
        chartContainer.getChildren().add(chart);
    }

    // --- Record para agrupar datos de entrada específicos ---
    private record SpecificInputs(int from, int to) {}
}