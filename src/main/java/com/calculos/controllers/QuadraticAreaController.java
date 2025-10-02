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

    // NUEVO: Almacena los resultados para mostrarlos en el popup
    private String lastResultsOutput = "Aún no se ha realizado un cálculo.";

    // Variables para almacenar los valores actuales de la función y el intervalo (usados por el gráfico)
    private double currentA = 0, currentB = 0, currentC = 0;
    private double currentStart = 0, currentEnd = 0;
    private int currentN = 10;
    private String currentApproximation = "Izquierda";

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
        // Inicializar el botón de resultados como deshabilitado
        showResultsButton.setDisable(true);

        // Enlazar el valor del Slider con el texto de la etiqueta y dibujar el gráfico
        rectanglesLabel.setText(String.format("Cantidad N: %d", (int) rectanglesSlider.getValue()));
        rectanglesSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            rectanglesLabel.setText(String.format("Cantidad N: %d", newVal.intValue()));
            currentN = newVal.intValue();
            drawChart(); // Actualizar gráfico al mover slider
        });

        // Listener para el ComboBox
        if (approximationComboBox.getValue() == null) {
            approximationComboBox.setValue("Izquierda");
        }
        approximationComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentApproximation = newVal;
                drawChart(); // Actualizar gráfico al cambiar tipo de aproximación
            }
        });

        // Inicializar el Canvas y añadirlo al StackPane (canvasWrapper)
        chartCanvas = new Canvas(100, 100);
        gc = chartCanvas.getGraphicsContext2D();

        canvasWrapper.getChildren().add(chartCanvas);

        // Ajustar el tamaño del canvas cuando el StackPane contenedor cambie de tamaño
        canvasWrapper.widthProperty().addListener((obs, oldVal, newVal) -> {
            chartCanvas.setWidth(newVal.doubleValue());
            drawChart();
        });
        canvasWrapper.heightProperty().addListener((obs, oldVal, newVal) -> {
            chartCanvas.setHeight(newVal.doubleValue());
            drawChart();
        });

        // Dibujar el gráfico inicial (vacío o con valores por defecto)
        drawChart();
    }

    @FXML
    private void goBackToMenu() {
        try {
            MainApp.showMainMenuView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clearFields() {
        aField.clear();
        bField.clear();
        cField.clear();
        startField.clear();
        endField.clear();
        rectanglesSlider.setValue(10);
        approximationComboBox.setValue("Izquierda");

        // CORRECCIÓN: Limpiar el resultado y deshabilitar botón
        lastResultsOutput = "Aún no se ha realizado un cálculo.";
        showResultsButton.setDisable(true);

        // Resetear variables de estado para el gráfico y redibujar
        currentA = 0; currentB = 0; currentC = 0;
        currentStart = 0; currentEnd = 0;
        currentN = 10;
        currentApproximation = "Izquierda";
        drawChart();
    }

    @FXML
    private void calculateArea() {
        lastResultsOutput = ""; // Reiniciar resultado
        try {
            // 1. Lectura y Validación de Coeficientes e Intervalo
            double a = parseAndValidate(aField.getText(), "a");
            double b = parseAndValidate(bField.getText(), "b");
            double c = parseAndValidate(cField.getText(), "c");
            double start = parseAndValidate(startField.getText(), "Límite A");
            double end = parseAndValidate(endField.getText(), "Límite B");
            int n = (int) rectanglesSlider.getValue();
            String approximation = approximationComboBox.getValue();

            if (a == 0) {
                lastResultsOutput = "Error: El coeficiente 'a' debe ser diferente de 0 para una función cuadrática.";
                showResultsButton.setDisable(false);
                showResultsPopup();
                return;
            }
            if (start >= end) {
                lastResultsOutput = "Error: El límite de inicio (A) debe ser menor que el límite final (B).";
                showResultsButton.setDisable(false);
                showResultsPopup();
                return;
            }

            // 2. Cálculos
            double deltaX = (end - start) / n;

            // 2.1. Suma Inferior y Superior (Riemann)
            double lowerSum = calculateRiemannSum(a, b, c, start, end, n, deltaX, true);
            double upperSum = calculateRiemannSum(a, b, c, start, end, n, deltaX, false);

            // 2.2. Área Real (Integral Definida Analítica)
            double areaB = (a / 3.0) * Math.pow(end, 3) + (b / 2.0) * Math.pow(end, 2) + c * end;
            double areaA = (a / 3.0) * Math.pow(start, 3) + (b / 2.0) * Math.pow(start, 2) + c * start;
            double realArea = areaB - areaA;

            // 2.3. Aproximación solicitada (Izquierda/Derecha)
            double customApprox = calculateCustomRiemann(a, b, c, start, end, n, deltaX, approximation.equals("Izquierda"));

            // 3. Almacenar y mostrar Resultados
            String functionStr = String.format("f(x) = %.2fx² + %.2fx + %.2f", a, b, c);
            String output = String.format(
                    "Función: %s\n" +
                            "Intervalo: [%.2f, %.2f]\n" +
                            "Rectángulos (N): %d\n" +
                            "Aproximación por: %s\n" +
                            "------------------------------------------\n" +
                            "Suma Inferior (Aprox. por Riemann): %.6f\n" +
                            "Suma Superior (Aprox. por Riemann): %.6f\n" +
                            "Suma (Método %s):              %.6f\n" +
                            "------------------------------------------\n" +
                            "Área Real (Integral Definida):     %.6f",
                    functionStr, start, end, n, approximation, lowerSum, upperSum, approximation, customApprox, realArea
            );

            lastResultsOutput = output; // 🎯 CORRECCIÓN: Guardar en la variable
            showResultsButton.setDisable(false);

            // 4. ACTUALIZAR GRÁFICO
            currentA = a;
            currentB = b;
            currentC = c;
            currentStart = start;
            currentEnd = end;
            drawChart();
            showResultsPopup(); // 🎯 CORRECCIÓN: Mostrar el popup

        } catch (NumberFormatException e) {
            lastResultsOutput = "Error de entrada: Asegúrate de que todos los campos sean números válidos. " + e.getMessage();
            showResultsButton.setDisable(false);
            showResultsPopup();
        } catch (Exception e) {
            lastResultsOutput = "Error desconocido: " + e.getMessage();
            showResultsButton.setDisable(false);
            showResultsPopup();
            e.printStackTrace();
        }
    }

    /**
     * 🎯 NUEVO MÉTODO: Abre una nueva ventana (popup) para mostrar los resultados.
     */
    @FXML
    private void showResultsPopup() {
        // Crear el Stage (ventana) del popup
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que se cierre
        // Asumiendo que MainApp tiene un método estático para obtener el Stage principal
        dialog.initOwner(MainApp.getPrimaryStage());
        dialog.setTitle("Resultados del Cálculo de Área");

        // Crear el TextArea para mostrar el texto
        TextArea resultsDisplay = new TextArea(lastResultsOutput);
        resultsDisplay.setEditable(false);
        resultsDisplay.setPrefSize(450, 300);
        resultsDisplay.setWrapText(true);
        resultsDisplay.getStyleClass().add("text-area");

        // Botón de cerrar
        Button closeButton = new Button("Cerrar");
        closeButton.getStyleClass().addAll("button", "secondary-action-button");
        closeButton.setOnAction(e -> dialog.close());

        // Contenedor principal del popup
        VBox dialogVBox = new VBox(20);
        dialogVBox.getChildren().addAll(resultsDisplay, closeButton);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setPadding(new Insets(20));
        dialogVBox.getStyleClass().add("content-pane");

        // Escena y mostrar
        Scene dialogScene = new Scene(dialogVBox, 500, 400);

        // Aplicar estilos (es necesario cargar la hoja de estilos de nuevo en el popup)
        try {
            dialogScene.getStylesheets().add(
                    MainApp.class.getResource("/styles/styles.css").toExternalForm()
            );
        } catch (Exception e) {
            System.err.println("No se pudo cargar la hoja de estilos para el popup. Asegúrate de que '/styles/styles.css' existe.");
        }


        dialog.setScene(dialogScene);
        dialog.show();
    }

    // --- MÉTODOS DE CÁLCULO Y DIBUJO ---

    /**
     * Evalúa la función cuadrática f(x) = ax^2 + bx + c en un punto dado.
     */
    private double evaluateFunction(double a, double b, double c, double x) {
        return a * x * x + b * x + c;
    }

    /**
     * Calcula la Suma de Darboux (Inferior o Superior) evaluando el extremo en cada subintervalo.
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

            boolean vertexInInterval = !Double.isNaN(vertex_x) && vertex_x >= x_i && vertex_x <= x_i_plus_1;

            if (vertexInInterval) {
                double f_vertex = evaluateFunction(a, b, c, vertex_x);

                if (isLowerSum) {
                    extrema = Math.min(f_vertex, Math.min(f_start, f_end));
                } else { // Suma Superior
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
     * Calcula la Suma de Riemann usando los puntos finales izquierdos o derechos.
     */
    private double calculateCustomRiemann(double a, double b, double c, double start, double end, int n, double deltaX, boolean useLeftEndpoint) {
        double sum = 0;
        for (int i = 0; i < n; i++) {
            double x;
            if (useLeftEndpoint) {
                x = start + i * deltaX; // Punto final izquierdo
            } else {
                x = start + (i + 1) * deltaX; // Punto final derecho
            }
            sum += evaluateFunction(a, b, c, x) * deltaX;
        }
        return sum;
    }

    /**
     * Helper para parsear y validar la entrada.
     */
    private double parseAndValidate(String text, String fieldName) throws NumberFormatException {
        if (text == null || text.trim().isEmpty()) {
            throw new NumberFormatException("El campo '" + fieldName + "' no puede estar vacío.");
        }
        try {
            return Double.parseDouble(text.replace(',', '.')); // Soporte para coma decimal
        } catch (NumberFormatException e) {
            throw new NumberFormatException("El valor de '" + fieldName + "' debe ser un número válido.");
        }
    }

    /**
     * Método para dibujar la función y los rectángulos en el Canvas.
     */
    private void drawChart() {
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

        // Muestreo para determinar rango Y (1000 puntos para precisión)
        for (int i = 0; i <= 1000; i++) {
            double x = currentStart + (currentEnd - currentStart) * i / 1000.0;
            double y = evaluateFunction(currentA, currentB, currentC, x);
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        // Ajustar el rango Y (margen y visualización del eje 0)
        double yRange = maxY - minY;
        double yMargin = yRange * 0.1;

        if (yRange == 0) { // Si es una función constante
            minY -= 1;
            maxY += 1;
        } else {
            minY -= yMargin;
            maxY += yMargin;
        }

        // Asegurar que el eje 0 (cero) esté visible
        if (minY > 0 && 0 < maxY) minY = 0;
        if (maxY < 0 && 0 > minY) maxY = 0;


        // ------------------ 2. Coordenadas y Escala ------------------
        double marginX = 40;
        double marginY = 30;
        double plotWidth = width - 2 * marginX;
        double plotHeight = height - 2 * marginY;

        double scaleX = plotWidth / (currentEnd - currentStart);
        double scaleY = plotHeight / (maxY - minY);

        // Posición del Eje X (y=0 en coordenadas del Canvas)
        double zeroYCanvas = marginY + plotHeight - (0 - minY) * scaleY;

        // ------------------ 3. Dibujar Ejes y Etiquetas ------------------
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);

        // Eje X
        if (zeroYCanvas >= marginY && zeroYCanvas <= marginY + plotHeight) {
            gc.strokeLine(marginX, zeroYCanvas, marginX + plotWidth, zeroYCanvas);
        } else {
            gc.strokeLine(marginX, marginY + plotHeight, marginX + plotWidth, marginY + plotHeight); // Abajo
            zeroYCanvas = marginY + plotHeight;
        }

        // Eje Y (en x=0 o en el borde izquierdo)
        double zeroXCanvas = marginX + (0 - currentStart) * scaleX;
        if (zeroXCanvas < marginX) zeroXCanvas = marginX;
        if (zeroXCanvas > marginX + plotWidth) zeroXCanvas = marginX; // Si está fuera, lo dibujamos en el borde izquierdo

        gc.strokeLine(zeroXCanvas, marginY, zeroXCanvas, marginY + plotHeight);

        // Etiquetas
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(new javafx.scene.text.Font("Segoe UI", 10));

        // X inicial y final
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText(String.format("%.1f", currentStart), marginX, zeroYCanvas + 15);
        gc.fillText(String.format("%.1f", currentEnd), marginX + plotWidth, zeroYCanvas + 15);

        // Y mínimo y máximo
        gc.setTextAlign(javafx.scene.text.TextAlignment.RIGHT);
        gc.fillText(String.format("%.1f", maxY), zeroXCanvas - 5, marginY + 5);
        if (minY != 0) {
            gc.fillText(String.format("%.1f", minY), zeroXCanvas - 5, marginY + plotHeight + 5);
        }

        // ------------------ 4. Dibujar la Función Cuadrática ------------------
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(2);

        gc.beginPath();
        double step = (currentEnd - currentStart) / plotWidth; // Un punto por píxel de ancho de plot

        // Mover al primer punto
        double firstX = currentStart;
        double firstY = evaluateFunction(currentA, currentB, currentC, firstX);
        double canvasY = marginY + plotHeight - (firstY - minY) * scaleY;

        // Clamp canvasY to plot bounds
        if (canvasY < marginY) canvasY = marginY;
        if (canvasY > marginY + plotHeight) canvasY = marginY + plotHeight;

        gc.moveTo(marginX, canvasY);

        for (double x = currentStart; x <= currentEnd; x += step) {
            double y = evaluateFunction(currentA, currentB, currentC, x);
            double canvasX = marginX + (x - currentStart) * scaleX;
            canvasY = marginY + plotHeight - (y - minY) * scaleY;

            // Clamp canvasY to plot bounds
            if (canvasY < marginY) canvasY = marginY;
            if (canvasY > marginY + plotHeight) canvasY = marginY + plotHeight;

            gc.lineTo(canvasX, canvasY);
        }
        gc.stroke();

        // ------------------ 5. Dibujar Rectángulos de Riemann ------------------
        if (currentN > 0) {
            gc.setFill(Color.rgb(100, 149, 237, 0.4)); // Relleno azul semitransparente
            gc.setStroke(Color.ROYALBLUE);
            gc.setLineWidth(1);

            double deltaX = (currentEnd - currentStart) / currentN;

            for (int i = 0; i < currentN; i++) {
                double rectStart = currentStart + i * deltaX;
                double rectEnd = currentStart + (i + 1) * deltaX;

                double rectXCanvas = marginX + (rectStart - currentStart) * scaleX;
                double rectWidthCanvas = deltaX * scaleX;

                double sampleX;
                if (currentApproximation.equals("Izquierda")) {
                    sampleX = rectStart;
                } else { // Derecha
                    sampleX = rectEnd;
                }

                double rectHeight = evaluateFunction(currentA, currentB, currentC, sampleX);
                double rectHeightCanvas = rectHeight * scaleY;

                if (Math.abs(rectHeight) > 1e-9) {
                    double drawYCanvas;

                    if (rectHeight >= 0) { // Rectángulo por encima del eje X
                        drawYCanvas = zeroYCanvas - rectHeightCanvas;
                    } else { // Rectángulo por debajo del eje X
                        drawYCanvas = zeroYCanvas;
                        rectHeightCanvas = -rectHeightCanvas;
                    }

                    gc.fillRect(rectXCanvas, drawYCanvas, rectWidthCanvas, rectHeightCanvas);
                    gc.strokeRect(rectXCanvas, drawYCanvas, rectWidthCanvas, rectHeightCanvas);
                }
            }
        }
    }
}