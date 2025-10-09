package com.calculos.controllers;

import com.calculos.MainApp;
import com.calculos.utils.PopupManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    private Canvas chartCanvas;
    private GraphicsContext gc;

    private String lastResultsOutput = "Aún no se ha realizado un cálculo.";

    // CAMBIO: Valores por defecto actualizados para reflejar la nueva lógica
    private double currentA = 0, currentB = 0, currentC = 0;
    private double currentStart = 0, currentEnd = 0;
    private int currentN = 10;
    private String currentApproximation = "Suma Inferior"; // CAMBIO

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
    public void initialize() {
        showResultsButton.setDisable(true);

        rectanglesLabel.setText(String.format("Cantidad N: %d", (int) rectanglesSlider.getValue()));
        rectanglesSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            rectanglesLabel.setText(String.format("Cantidad N: %d", newVal.intValue()));
            currentN = newVal.intValue();
            drawChart();
        });

        // CAMBIO: Se establece el valor inicial del ComboBox
        if (approximationComboBox.getValue() == null) {
            approximationComboBox.setValue("Suma Inferior");
        }
        approximationComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentApproximation = newVal;
                drawChart();
            }
        });

        chartCanvas = new Canvas(100, 100);
        gc = chartCanvas.getGraphicsContext2D();

        canvasWrapper.getChildren().add(chartCanvas);

        canvasWrapper.widthProperty().addListener((obs, oldVal, newVal) -> {
            chartCanvas.setWidth(newVal.doubleValue());
            drawChart();
        });
        canvasWrapper.heightProperty().addListener((obs, oldVal, newVal) -> {
            chartCanvas.setHeight(newVal.doubleValue());
            drawChart();
        });

        drawChart();
    }

    @FXML
    private void clearFields() {
        aField.clear();
        bField.clear();
        cField.clear();
        startField.clear();
        endField.clear();
        rectanglesSlider.setValue(10);
        // CAMBIO: Se reinicia al nuevo valor por defecto
        approximationComboBox.setValue("Suma Inferior");

        lastResultsOutput = "Aún no se ha realizado un cálculo.";
        showResultsButton.setDisable(true);

        currentA = 0; currentB = 0; currentC = 0;
        currentStart = 0; currentEnd = 0;
        currentN = 10;
        // CAMBIO: Se reinicia al nuevo valor por defecto
        currentApproximation = "Suma Inferior";
        drawChart();
    }

    @FXML
    private void calculateArea() {
        lastResultsOutput = "";
        try {
            double a = parseAndValidate(aField.getText(), "a");
            double b = parseAndValidate(bField.getText(), "b");
            double c = parseAndValidate(cField.getText(), "c");
            double start = parseAndValidate(startField.getText(), "Límite A");
            double end = parseAndValidate(endField.getText(), "Límite B");
            int n = (int) rectanglesSlider.getValue();
            String approximation = approximationComboBox.getValue();

            if (a == 0) {
                lastResultsOutput = "Error: El coeficiente 'a' debe ser diferente de 0 para una función cuadrática.";
                showResultsPopupAndEnableButton();
                return;
            }
            if (start >= end) {
                lastResultsOutput = "Error: El límite de inicio (A) debe ser menor que el límite final (B).";
                showResultsPopupAndEnableButton();
                return;
            }

            double deltaX = (end - start) / n;

            // 1. Suma Inferior y Superior (tu método ya lo hace bien)
            double lowerSum = calculateRiemannSum(a, b, c, start, end, n, deltaX, true);
            double upperSum = calculateRiemannSum(a, b, c, start, end, n, deltaX, false);

            // 2. Área Real (Integral)
            double areaB = (a / 3.0) * Math.pow(end, 3) + (b / 2.0) * Math.pow(end, 2) + c * end;
            double areaA = (a / 3.0) * Math.pow(start, 3) + (b / 2.0) * Math.pow(start, 2) + c * start;
            double realArea = areaB - areaA;

            // 3. Formatear salida (simplificada)
            String functionStr = String.format("f(x) = %.2fx² + %.2fx + %.2f", a, b, c);
            // CAMBIO: Se simplifica la salida para ser más clara y no mostrar cálculos redundantes.
            String output = String.format(
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

            lastResultsOutput = output;

            // 4. Actualizar gráfico y mostrar popup
            currentA = a;
            currentB = b;
            currentC = c;
            currentStart = start;
            currentEnd = end;
            drawChart();
            showResultsPopupAndEnableButton();

        } catch (NumberFormatException e) {
            lastResultsOutput = "Error de entrada: " + e.getMessage();
            showResultsPopupAndEnableButton();
        } catch (Exception e) {
            lastResultsOutput = "Error desconocido: " + e.getMessage();
            showResultsPopupAndEnableButton();
            e.printStackTrace();
        }
    }

    private void showResultsPopupAndEnableButton() {
        showResultsButton.setDisable(false);
        showResultsPopup();
    }

    @FXML
    private void showResultsPopup() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(MainApp.getPrimaryStage());
        dialog.setTitle("Resultados del Cálculo de Área");

        TextArea resultsDisplay = new TextArea(lastResultsOutput);
        resultsDisplay.setEditable(false);
        resultsDisplay.setPrefSize(450, 250); // Ajuste de tamaño
        resultsDisplay.setWrapText(true);
        resultsDisplay.getStyleClass().add("text-area");

        Button closeButton = new Button("Cerrar");
        closeButton.getStyleClass().addAll("button", "secondary-action-button");
        closeButton.setOnAction(e -> dialog.close());

        VBox dialogVBox = new VBox(20);
        dialogVBox.getChildren().addAll(resultsDisplay, closeButton);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setPadding(new Insets(20));
        dialogVBox.getStyleClass().add("content-pane");

        Scene dialogScene = new Scene(dialogVBox, 500, 350); // Ajuste de tamaño

        try {
            dialogScene.getStylesheets().add(
                    MainApp.class.getResource("/styles/styles.css").toExternalForm()
            );
        } catch (Exception e) {
            System.err.println("No se pudo cargar la hoja de estilos para el popup.");
        }

        dialog.setScene(dialogScene);
        dialog.show();
    }


    private double evaluateFunction(double a, double b, double c, double x) {
        return a * x * x + b * x + c;
    }

    /**
     * Este método ya calcula correctamente la Suma de Darboux (Inferior o Superior).
     * No necesita cambios. ¡Bien hecho aquí!
     */
    private double calculateRiemannSum(double a, double b, double c, double start, double end, int n, double deltaX, boolean isLowerSum) {
        double sum = 0;
        for (int i = 0; i < n; i++) {
            double x_i = start + i * deltaX;
            double x_i_plus_1 = start + (i + 1) * deltaX;

            double f_start = evaluateFunction(a, b, c, x_i);
            double f_end = evaluateFunction(a, b, c, x_i_plus_1);
            double vertex_x = (a != 0) ? -b / (2.0 * a) : Double.NaN;
            double extrema;

            boolean vertexInInterval = !Double.isNaN(vertex_x) && vertex_x > x_i && vertex_x < x_i_plus_1;

            if (vertexInInterval) {
                double f_vertex = evaluateFunction(a, b, c, vertex_x);
                if (isLowerSum) {
                    extrema = Math.min(f_vertex, Math.min(f_start, f_end));
                } else {
                    extrema = Math.max(f_vertex, Math.max(f_start, f_end));
                }
            } else {
                if (isLowerSum) {
                    extrema = Math.min(f_start, f_end);
                } else {
                    extrema = Math.max(f_start, f_end);
                }
            }
            sum += extrema * deltaX;
        }
        return sum;
    }

    /**
     * ELIMINADO: Este método ya no es necesario, ya que calculateRiemannSum maneja toda la lógica.
     * private double calculateCustomRiemann(...) { ... }
     */

    private double parseAndValidate(String text, String fieldName) throws NumberFormatException {
        if (text == null || text.trim().isEmpty()) {
            throw new NumberFormatException("El campo '" + fieldName + "' no puede estar vacío.");
        }
        try {
            return Double.parseDouble(text.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("El valor de '" + fieldName + "' debe ser un número válido.");
        }
    }

    private void drawChart() {
        // ... (el código de inicialización del gráfico, ejes y dibujo de la función no cambia)
        if (gc == null || chartCanvas == null) return;

        double width = chartCanvas.getWidth();
        double height = chartCanvas.getHeight();

        gc.clearRect(0, 0, width, height);

        // ------------------ 1. Validación y Escala ------------------
        if (currentStart >= currentEnd || width < 100 || height < 100 || currentEnd - currentStart == 0) {
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(new javafx.scene.text.Font("Segoe UI", 18));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText("Ingrese un intervalo válido [A, B]", width / 2, height / 2 - 20);
            gc.fillText("y presione 'Calcular Área' para ver el gráfico.", width / 2, height / 2 + 10);
            return;
        }

        // Determinar rango de Y
        double minY = evaluateFunction(currentA, currentB, currentC, currentStart);
        double maxY = minY;

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
        double yMargin = yRange * 0.1;

        if (yRange == 0) {
            minY -= 1;
            maxY += 1;
        } else {
            minY -= yMargin;
            maxY += yMargin;
        }

        if (minY > 0) minY = 0;
        if (maxY < 0) maxY = 0;

        // ------------------ 2. Coordenadas y Escala ------------------
        double marginX = 40;
        double marginY = 30;
        double plotWidth = width - 2 * marginX;
        double plotHeight = height - 2 * marginY;

        double scaleX = plotWidth / (currentEnd - currentStart);
        double scaleY = plotHeight / (maxY - minY);

        double zeroYCanvas = marginY + plotHeight - (0 - minY) * scaleY;

        // ... (El código para dibujar ejes y la curva de la función sigue igual)
        // Ejes
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        if (zeroYCanvas >= marginY && zeroYCanvas <= marginY + plotHeight) {
            gc.strokeLine(marginX, zeroYCanvas, marginX + plotWidth, zeroYCanvas);
        } else {
            gc.strokeLine(marginX, marginY + plotHeight, marginX + plotWidth, marginY + plotHeight);
            zeroYCanvas = marginY + plotHeight;
        }
        double zeroXCanvas = marginX + (0 - currentStart) * scaleX;
        if (zeroXCanvas < marginX || zeroXCanvas > marginX + plotWidth) zeroXCanvas = marginX;
        gc.strokeLine(zeroXCanvas, marginY, zeroXCanvas, marginY + plotHeight);

        // Curva
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(2);
        gc.beginPath();
        boolean firstPoint = true;
        for (double x = currentStart; x <= currentEnd; x += (currentEnd - currentStart) / plotWidth) {
            double y = evaluateFunction(currentA, currentB, currentC, x);
            double canvasX = marginX + (x - currentStart) * scaleX;
            double canvasY = marginY + plotHeight - (y - minY) * scaleY;
            if (firstPoint) {
                gc.moveTo(canvasX, canvasY);
                firstPoint = false;
            } else {
                gc.lineTo(canvasX, canvasY);
            }
        }
        gc.stroke();


        // ------------------ 5. DIBUJAR RECTÁNGULOS (LÓGICA ACTUALIZADA) ------------------
        if (currentN > 0) {
            gc.setFill(Color.rgb(100, 149, 237, 0.4));
            gc.setStroke(Color.ROYALBLUE);
            gc.setLineWidth(1);

            double deltaX = (currentEnd - currentStart) / currentN;
            boolean isLowerSum = currentApproximation.equals("Suma Inferior");

            for (int i = 0; i < currentN; i++) {
                double rectStart = currentStart + i * deltaX;
                double rectEnd = rectStart + deltaX;

                // ✅ LÓGICA CLAVE: Encontrar la altura correcta (mínima o máxima) en el subintervalo
                double rectHeight;
                double f_start = evaluateFunction(currentA, currentB, currentC, rectStart);
                double f_end = evaluateFunction(currentA, currentB, currentC, rectEnd);

                double vertex_x = (currentA != 0) ? -currentB / (2.0 * currentA) : Double.NaN;

                boolean vertexInInterval = !Double.isNaN(vertex_x) && vertex_x > rectStart && vertex_x < rectEnd;

                if (vertexInInterval) {
                    double f_vertex = evaluateFunction(currentA, currentB, currentC, vertex_x);
                    if (isLowerSum) {
                        rectHeight = Math.min(f_vertex, Math.min(f_start, f_end));
                    } else {
                        rectHeight = Math.max(f_vertex, Math.max(f_start, f_end));
                    }
                } else {
                    if (isLowerSum) {
                        rectHeight = Math.min(f_start, f_end);
                    } else {
                        rectHeight = Math.max(f_start, f_end);
                    }
                }

                // Dibujar el rectángulo con la altura calculada
                double rectXCanvas = marginX + (rectStart - currentStart) * scaleX;
                double rectWidthCanvas = deltaX * scaleX;
                double rectHeightCanvas = rectHeight * scaleY;

                if (Math.abs(rectHeightCanvas) > 1e-9) {
                    double drawYCanvas;
                    if (rectHeight >= 0) {
                        drawYCanvas = zeroYCanvas - rectHeightCanvas;
                    } else {
                        drawYCanvas = zeroYCanvas;
                        rectHeightCanvas = -rectHeightCanvas;
                    }

                    // Clamp para evitar que los rectángulos se salgan del área de dibujo
                    if (drawYCanvas < marginY) {
                        rectHeightCanvas -= (marginY - drawYCanvas);
                        drawYCanvas = marginY;
                    }
                    if (drawYCanvas + rectHeightCanvas > marginY + plotHeight) {
                        rectHeightCanvas = (marginY + plotHeight) - drawYCanvas;
                    }

                    if(rectHeightCanvas > 0) {
                        gc.fillRect(rectXCanvas, drawYCanvas, rectWidthCanvas, rectHeightCanvas);
                        gc.strokeRect(rectXCanvas, drawYCanvas, rectWidthCanvas, rectHeightCanvas);
                    }
                }
            }
        }
    }
}