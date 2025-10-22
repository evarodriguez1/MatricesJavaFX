package com.calculos.controllers;

import com.calculos.MainApp;
import com.calculos.utils.PopupManager;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class QuadraticAreaController {

    @FXML private TextField aField;
    @FXML private TextField bField;
    @FXML private TextField cField;
    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private Slider rectanglesSlider;
    @FXML private Label rectanglesLabel;
    @FXML private ComboBox<String> approximationComboBox;
    @FXML private Button showResultsButton;
    @FXML private VBox chartContainer;
    @FXML private StackPane canvasWrapper;
    @FXML private VBox controlsPanel;

    private Canvas chartCanvas;
    private GraphicsContext gc;
    private String lastResultsOutput = "Aún no se ha realizado un cálculo.";
    private double currentA = 0, currentB = 0, currentC = 0;
    private double currentStart = 0, currentEnd = 0;
    private int currentN = 10;
    private String currentApproximation = "Suma Inferior";

    @FXML
    public void initialize() {
        rectanglesLabel.setText(String.format("Cantidad N: %d", (int) rectanglesSlider.getValue()));
        rectanglesSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            rectanglesLabel.setText(String.format("Cantidad N: %d", newVal.intValue()));
            currentN = newVal.intValue();
            drawChart();
        });

        approximationComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentApproximation = newVal;
                drawChart();
            }
        });

        chartCanvas = new Canvas(100, 100);
        gc = chartCanvas.getGraphicsContext2D();
        canvasWrapper.getChildren().add(chartCanvas);
        chartCanvas.widthProperty().bind(canvasWrapper.widthProperty());
        chartCanvas.heightProperty().bind(canvasWrapper.heightProperty());
        chartCanvas.widthProperty().addListener(obs -> drawChart());
        chartCanvas.heightProperty().addListener(obs -> drawChart());

        drawChart();
    }

    @FXML
    private void backToMenu() {
        try {
            MainApp.showMainMenuView();
        } catch (Exception e) {
            e.printStackTrace();
            PopupManager.showError("Error al cargar el menú principal: " + e.getMessage());
        }
    }

    @FXML
    private void clearFields() {
        aField.clear(); bField.clear(); cField.clear();
        startField.clear(); endField.clear();
        rectanglesSlider.setValue(10);
        approximationComboBox.setValue("Suma Inferior");
        lastResultsOutput = "Aún no se ha realizado un cálculo.";
        showResultsButton.setDisable(true);
        currentA = 0; currentB = 0; currentC = 0;
        currentStart = 0; currentEnd = 0;
        currentN = 10;
        currentApproximation = "Suma Inferior";
        drawChart();
    }

    @FXML
    private void calculateArea() {
        try {
            double a = parseAndValidate(aField.getText(), "'a'");
            double b = parseAndValidate(bField.getText(), "'b'");
            double c = parseAndValidate(cField.getText(), "'c'");
            double start = parseAndValidate(startField.getText(), "Límite A");
            double end = parseAndValidate(endField.getText(), "Límite B");
            int n = (int) rectanglesSlider.getValue();

            if (a == 0) {
                PopupManager.showError("El coeficiente 'a' debe ser diferente de 0 para una función cuadrática.");
                return;
            }
            if (start >= end) {
                PopupManager.showError("El límite de inicio (A) debe ser menor que el límite final (B).");
                return;
            }

            double deltaX = (end - start) / n;
            double lowerSum = calculateRiemannSum(a, b, c, start, end, n, deltaX, true);
            double upperSum = calculateRiemannSum(a, b, c, start, end, n, deltaX, false);
            double areaB = (a / 3.0) * Math.pow(end, 3) + (b / 2.0) * Math.pow(end, 2) + c * end;
            double areaA = (a / 3.0) * Math.pow(start, 3) + (b / 2.0) * Math.pow(start, 2) + c * start;
            double realArea = areaB - areaA;

            String functionStr = String.format("f(x) = %.2fx² + %.2fx + %.2f", a, b, c);
            lastResultsOutput = String.format(
                    "Función: %s\n" +
                            "Intervalo: [%.2f, %.2f]\n" +
                            "Rectángulos (N): %d\n" +
                            "------------------------------------------\n" +
                            "Suma Inferior (Aprox. por abajo): %.6f\n" +
                            "Suma Superior (Aprox. por arriba): %.6f\n" +
                            "------------------------------------------\n" +
                            "Área Real (Integral Definida):     %.6f",
                    functionStr, start, end, n, lowerSum, upperSum, realArea
            );

            currentA = a; currentB = b; currentC = c;
            currentStart = start; currentEnd = end;
            drawChart();
            showResultsButton.setDisable(false);

            showCustomPositionedPopup();

        } catch (NumberFormatException e) {
            PopupManager.showError("Error de entrada: " + e.getMessage());
        } catch (Exception e) {
            PopupManager.showError("Error desconocido: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showResultsPopup() {
        showCustomPositionedPopup();
    }

    private void showCustomPositionedPopup() {
        Bounds controlBounds = controlsPanel.localToScreen(controlsPanel.getBoundsInLocal());
        if (controlBounds == null) {
            PopupManager.showResultsPopup("Resultados", "Cálculo completado:", lastResultsOutput);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Resultados del Cálculo de Área");
        alert.setHeaderText("Cálculos completados:");
        TextArea textArea = new TextArea(lastResultsOutput);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setFont(Font.font("Monospaced", 14));
        alert.getDialogPane().setContent(textArea);
        try {
            alert.getDialogPane().getStylesheets().add(MainApp.class.getResource("/styles/styles.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane");
            alert.getDialogPane().setGraphic(null);
        } catch (Exception e) { System.err.println("Error aplicando estilo a popup."); }

        alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Aceptar");

        alert.getDialogPane().setPrefSize(420, 380);
        alert.setX(controlBounds.getMinX());
        alert.setY(controlBounds.getMinY() + 40);

        alert.showAndWait();
    }

    private double evaluateFunction(double a, double b, double c, double x) { return a * x * x + b * x + c; }

    private double calculateRiemannSum(double a, double b, double c, double start, double end, int n, double deltaX, boolean isLowerSum) {
        double sum = 0;
        for (int i = 0; i < n; i++) {
            double x_i = start + i * deltaX, x_i_plus_1 = start + (i + 1) * deltaX;
            double f_start = evaluateFunction(a, b, c, x_i), f_end = evaluateFunction(a, b, c, x_i_plus_1);
            double vertex_x = (a != 0) ? -b / (2.0 * a) : Double.NaN;
            double extrema;
            boolean vertexInInterval = !Double.isNaN(vertex_x) && vertex_x > x_i && vertex_x < x_i_plus_1;
            if (vertexInInterval) {
                double f_vertex = evaluateFunction(a, b, c, vertex_x);
                extrema = isLowerSum ? Math.min(f_vertex, Math.min(f_start, f_end)) : Math.max(f_vertex, Math.max(f_start, f_end));
            } else extrema = isLowerSum ? Math.min(f_start, f_end) : Math.max(f_start, f_end);
            sum += extrema * deltaX;
        }
        return sum;
    }

    private double parseAndValidate(String text, String fieldName) throws NumberFormatException {
        if (text == null || text.trim().isEmpty()) throw new NumberFormatException("El campo " + fieldName + " no puede estar vacío.");
        try { return Double.parseDouble(text.replace(',', '.')); } catch (NumberFormatException e) { throw new NumberFormatException("El valor de " + fieldName + " debe ser un número válido."); }
    }

    private void drawChart() {
        if (gc == null || chartCanvas == null) return;
        double width = chartCanvas.getWidth(), height = chartCanvas.getHeight();
        gc.clearRect(0, 0, width, height);

        if (currentStart >= currentEnd || width < 100 || height < 100) {
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(new Font("Segoe UI", 18));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Ingrese parámetros y un intervalo válido", width / 2, height / 2 - 15);
            gc.fillText("para visualizar el gráfico.", width / 2, height / 2 + 15);
            return;
        }

        double minY = evaluateFunction(currentA, currentB, currentC, currentStart), maxY = minY;
        double vertexX = (currentA != 0) ? -currentB / (2.0 * currentA) : Double.NaN;
        if (!Double.isNaN(vertexX) && vertexX >= currentStart && vertexX <= currentEnd) {
            double vertexY = evaluateFunction(currentA, currentB, currentC, vertexX);
            if (vertexY < minY) minY = vertexY;
            if (vertexY > maxY) maxY = vertexY;
        }
        double yEnd = evaluateFunction(currentA, currentB, currentC, currentEnd);
        if (yEnd < minY) minY = yEnd;
        if (yEnd > maxY) maxY = yEnd;

        double yRange = maxY - minY;
        if (yRange == 0) { minY -= 1; maxY += 1; } else { double margin = yRange * 0.15; minY -= margin; maxY += margin; }
        if (minY > 0) minY = -0.1 * maxY; if (maxY < 0) maxY = -0.1 * minY;

        double marginX = 40, marginY = 30;
        double plotWidth = width - 2 * marginX, plotHeight = height - 2 * marginY;
        if (plotWidth <= 0 || plotHeight <= 0) return;

        double scaleX = plotWidth / (currentEnd - currentStart), scaleY = plotHeight / (maxY - minY);
        double zeroYCanvas = marginY + plotHeight - (0 - minY) * scaleY;
        double zeroXCanvas = marginX - (currentStart * scaleX);

        gc.setStroke(Color.GRAY); gc.setLineWidth(1);
        gc.strokeLine(marginX, zeroYCanvas, marginX + plotWidth, zeroYCanvas);
        gc.strokeLine(zeroXCanvas, marginY, zeroXCanvas, marginY + plotHeight);

        gc.setStroke(Color.YELLOW); gc.setLineWidth(2);
        gc.beginPath();
        for (double x = currentStart; x <= currentEnd; x += (currentEnd - currentStart) / plotWidth) {
            double y = evaluateFunction(currentA, currentB, currentC, x);
            double canvasX = marginX + (x - currentStart) * scaleX;
            double canvasY = marginY + plotHeight - (y - minY) * scaleY;
            if (x == currentStart) gc.moveTo(canvasX, canvasY); else gc.lineTo(canvasX, canvasY);
        }
        gc.stroke();

        if (currentN > 0) {
            gc.setFill(Color.rgb(100, 149, 237, 0.4)); gc.setStroke(Color.ROYALBLUE); gc.setLineWidth(1);
            double deltaX = (currentEnd - currentStart) / currentN;
            boolean isLowerSum = currentApproximation.equals("Suma Inferior");
            for (int i = 0; i < currentN; i++) {
                double rectStart = currentStart + i * deltaX;
                double rectHeight = calculateRiemannSum(currentA, currentB, currentC, rectStart, rectStart + deltaX, 1, deltaX, isLowerSum) / deltaX;
                double rectXCanvas = marginX + (rectStart - currentStart) * scaleX;
                double rectWidthCanvas = deltaX * scaleX;
                double rectHeightCanvas = rectHeight * scaleY;
                if (Math.abs(rectHeightCanvas) > 1e-9) {
                    double drawYCanvas = rectHeight >= 0 ? zeroYCanvas - rectHeightCanvas : zeroYCanvas;
                    rectHeightCanvas = Math.abs(rectHeightCanvas);
                    if (rectHeightCanvas > 0.1) {
                        gc.fillRect(rectXCanvas, drawYCanvas, rectWidthCanvas, rectHeightCanvas);
                        gc.strokeRect(rectXCanvas, drawYCanvas, rectWidthCanvas, rectHeightCanvas);
                    }
                }
            }
        }
    }
}