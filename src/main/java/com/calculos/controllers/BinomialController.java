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
 * Gestiona la interacción del usuario, la validación de entradas y la
 * presentación de resultados, delegando los cálculos al modelo BinomialDistribution.
 * Hereda de BaseController para funcionalidad común de navegación y manejo de errores.
 */
public class BinomialController extends BaseController {

    // --- Componentes FXML de la Vista ---
    @FXML private TextField nField;
    @FXML private TextField pField;
    @FXML private ComboBox<CalculationType> calculationTypeComboBox;
    @FXML private TextField k1Field;
    @FXML private Label k2Label; // Etiqueta del segundo campo
    @FXML private TextField k2Field;
    @FXML private TextArea resultArea;
    @FXML private BarChart<String, Number> distributionChart; // Gráfica para visualización

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
     * Se ejecuta una vez que los componentes FXML han sido inyectados.
     * Configura el estado inicial de la vista y los listeners.
     */
    @FXML
    public void initialize() {
        // 1. Configurar el ComboBox
        calculationTypeComboBox.getItems().setAll(CalculationType.values());
        calculationTypeComboBox.getSelectionModel().selectFirst();

        // 2. Configurar la UI Reactiva (Data Binding)
        // La visibilidad del segundo campo de k (k2Field) depende de la selección.
        k2Label.visibleProperty().bind(calculationTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(CalculationType.RANGE));
        k2Field.visibleProperty().bind(calculationTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(CalculationType.RANGE));
    }

    /**
     * Maneja el evento de clic en el botón "Calcular". Orquesta el flujo de
     * validación, cálculo y presentación.
     */
    @FXML
    private void onSolve() {
        try {
            // Paso 1: Leer y validar todas las entradas del usuario.
            UserInput inputs = readAndValidateUserInput();

            // Paso 2: Crear el objeto de dominio del modelo.
            BinomialDistribution dist = new BinomialDistribution(inputs.n(), inputs.p());

            // Paso 3: Realizar todos los cálculos necesarios.
            CalculationResult results = calculateResults(dist, inputs);

            // Paso 4: Actualizar la UI con los resultados.
            displayResults(results, dist, inputs);

        } catch (ValidationException e) {
            handleValidationException(e);
        } catch (Exception e) {
            handleGenericException(e);
        }
    }

    /**
     * Limpia todos los campos de entrada y los resultados a su estado inicial.
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

    /**
     * Parsea y valida todas las entradas de la UI y las empaqueta en un objeto.
     * @return un record UserInput con los datos validados.
     * @throws ValidationException si alguna entrada es inválida.
     */
    private UserInput readAndValidateUserInput() throws ValidationException {
        int n = InputValidator.parsePositiveInt(nField.getText(), "N (Total Ensayos)");
        double p = InputValidator.parseDoubleInRange(pField.getText(), "P (Prob. Éxito)", 0.0, 1.0);

        CalculationType selectedType = Objects.requireNonNull(calculationTypeComboBox.getValue(), "Tipo de cálculo no seleccionado.");

        int k1 = InputValidator.parseNonNegativeInt(k1Field.getText(), "Valor de k/a");
        int k2 = 0; // Valor por defecto
        if (selectedType.requiredFields == 2) {
            k2 = InputValidator.parseNonNegativeInt(k2Field.getText(), "Valor de b");
        }

        return new UserInput(n, p, selectedType, k1, k2);
    }

    /**
     * Realiza el cálculo de probabilidad apropiado basado en la selección del usuario.
     * @return un record CalculationResult con la probabilidad calculada.
     */
    private CalculationResult calculateResults(BinomialDistribution dist, UserInput inputs) throws ValidationException {
        int desde, hasta;
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
            default -> throw new IllegalStateException("Tipo de cálculo inesperado.");
        }

        double probability = dist.getProbabilityRange(desde, hasta);
        return new CalculationResult(probability, desde, hasta);
    }

    /**
     * Actualiza la UI (TextArea y Gráfica) con los resultados finales.
     */
    private void displayResults(CalculationResult result, BinomialDistribution dist, UserInput inputs) {
        resultArea.setText(formatResultText(result, dist));
        updateDistributionChart(dist, result);
    }

    /**
     * Formatea el texto de salida para el TextArea.
     */
    private String formatResultText(CalculationResult result, BinomialDistribution dist) {
        StringBuilder sb = new StringBuilder();
        String probDescription;
        CalculationType type = calculationTypeComboBox.getValue();

        if (type == CalculationType.EXACT) {
            probDescription = String.format("P(X = %d)", result.from());
        } else if (type == CalculationType.MAXIMUM) {
            probDescription = String.format("P(X <= %d)", result.to());
        } else if (type == CalculationType.MINIMUM) {
            probDescription = String.format("P(X >= %d)", result.from());
        } else {
            probDescription = String.format("P(%d <= X <= %d)", result.from(), result.to());
        }

        sb.append(String.format("PROBABILIDAD %s:\n", probDescription));
        sb.append(String.format("Resultado: %.8f\n", result.probability()));
        sb.append(String.format("Porcentaje: %.2f%%\n", result.probability() * 100));
        sb.append("\n----------------------------------\n");
        sb.append("DATOS DE LA DISTRIBUCIÓN:\n");
        sb.append(String.format("Esperanza E[X]: %.4f\n", dist.getMean()));
        sb.append(String.format("Varianza Var[X]: %.4f\n", dist.getVariance()));

        return sb.toString();
    }

    /**
     * Actualiza la gráfica de barras para mostrar la distribución de probabilidad completa.
     * Resalta las barras que corresponden al rango calculado.
     */
    private void updateDistributionChart(BinomialDistribution dist, CalculationResult result) {
        distributionChart.getData().clear();
        distributionChart.setAnimated(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("P(X=k)");

        Map<Integer, Double> fullDist = dist.getFullDistribution();

        IntStream.rangeClosed(0, dist.getMean() * 2 > 10 ? (int)(dist.getMean() * 2) : 10)
                .forEach(k -> {
                    XYChart.Data<String, Number> data = new XYChart.Data<>(String.valueOf(k), fullDist.getOrDefault(k, 0.0));
                    series.getData().add(data);
                });

        distributionChart.getData().add(series);

        // Resaltar barras del resultado
        for(XYChart.Data<String, Number> data : series.getData()) {
            int k = Integer.parseInt(data.getXValue());
            if (k >= result.from() && k <= result.to()) {
                data.getNode().setStyle("-fx-bar-fill: #FFD600;"); // Color de resaltado (amarillo)
            } else {
                data.getNode().setStyle("-fx-bar-fill: #4A148C;"); // Color normal (morado oscuro)
            }
        }
    }

    // --- Records Internos para una mejor estructura de datos ---

    /**
     * Un record para empaquetar de forma inmutable la entrada del usuario después de la validación.
     */
    private record UserInput(int n, double p, CalculationType type, int k1, int k2) {}

    /**
     * Un record para empaquetar de forma inmutable los resultados del cálculo.
     */
    private record CalculationResult(double probability, int from, int to) {}
}