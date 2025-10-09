package com.calculos.controllers;

import com.calculos.MainApp;
import com.calculos.models.MatrixSolver;
import com.calculos.utils.PopupManager;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority; // Necesario para el VBox.setVgrow
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class MatrixController {

    // --- Inyección de Componentes FXML ---
    @FXML private TextField a11Field, a12Field, a13Field, b1Field;
    @FXML private TextField a21Field, a22Field, a23Field, b2Field;
    @FXML private TextField a31Field, a32Field, a33Field, b3Field;
    @FXML private TextArea resultArea;
    @FXML private Button showStepsButton;
    @FXML private CheckBox useFractionsCheck;
    @FXML private Button checkButton; // Agrega el ID para el nuevo botón

    private List<TextField> allMatrixFields;
    private TextField[][] gridFields; // Para navegación con flechas
    private String detailedSteps; // Para almacenar los pasos

    private String[][] originalA_str;
    private String[] originalB_str;
    private String lastSolutionSummary;

    @FXML
    public void initialize() {
        allMatrixFields = Arrays.asList(
                a11Field, a12Field, a13Field, b1Field,
                a21Field, a22Field, a23Field, b2Field,
                a31Field, a32Field, a33Field, b3Field
        );

        // Deshabilitar el botón de pasos al inicio
        showStepsButton.setDisable(true);

        // Configurar la grilla para navegación
        setupFieldNavigation();
    }

    @FXML
    private void onSolve() {
        clearErrorStyles();
        showStepsButton.setDisable(true); // Deshabilitar mientras se resuelve
        detailedSteps = null;

        try {
            // AHORA LEEMOS COMO STRINGS para preservar la notación de fracción/decimal
            String[][] A_temp = {
                    { getStringValue(a11Field), getStringValue(a12Field), getStringValue(a13Field) },
                    { getStringValue(a21Field), getStringValue(a22Field), getStringValue(a23Field) },
                    { getStringValue(a31Field), getStringValue(a32Field), getStringValue(a33Field) }
            };

            String[] B_temp = {
                    getStringValue(b1Field), getStringValue(b2Field), getStringValue(b3Field)
            };

            // 2. Si llegamos aquí, los datos son válidos. Asignamos a las variables de instancia.
            this.originalA_str = A_temp;
            this.originalB_str = B_temp;


            // Llamada al método corregido que acepta Strings
            MatrixSolver.SolveResult result = MatrixSolver.solveGaussJordan(originalA_str, originalB_str, useFractionsCheck.isSelected());

            resultArea.setText(result.summary);
            detailedSteps = result.steps;
            lastSolutionSummary = result.summary; // Guardar el resumen para la verificación

            showStepsButton.setDisable(false); // Habilitar el botón

            if (result.summary.startsWith("Sistema Compatible Determinado")) {
                checkButton.setDisable(false);
            }

        } catch (IllegalArgumentException ex) {
            // Mostrar error específico de validación
            PopupManager.showError("Error de validación: " + ex.getMessage());
        } catch (Exception ex) {
            PopupManager.showError("Ocurrió un error al resolver la matriz: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void onShowSteps() {
        if (detailedSteps == null || detailedSteps.isEmpty()) {
            PopupManager.showInfo("No hay pasos detallados para mostrar. Resuelve un sistema primero.");
            return;
        }

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Resolución Paso a Paso");

        TextArea stepsArea = new TextArea(detailedSteps);
        stepsArea.setEditable(false);
        stepsArea.setWrapText(true);

        // Aplicamos el estilo .text-area que define la apariencia y la fuente monoespaciada.
        stepsArea.getStyleClass().add("text-area");

        // *** CAMBIO CLAVE: Permite que el TextArea crezca y llene el VBox ***
        stepsArea.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(stepsArea, Priority.ALWAYS);
        // ******************************************************************

        VBox popupLayout = new VBox(10);
        popupLayout.getChildren().add(stepsArea);
        // Aumentar padding para que se vea mejor y no esté pegado al borde
        popupLayout.setPadding(new javafx.geometry.Insets(20));

        // Aplicamos el estilo .root al contenedor para el fondo de gradiente
        popupLayout.getStyleClass().add("root");

        // Creamos la escena y cargamos la hoja de estilos CSS
        Scene popupScene = new Scene(popupLayout, 650, 500);

        // Cargar el CSS globalmente para que aplique los estilos
        try {
            String cssPath = getClass().getResource("/styles/styles.css").toExternalForm();
            popupScene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.err.println("Error al cargar /styles/styles.css. Asegúrate de que el archivo existe.");
        }

        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }

    @FXML
    private void onFillWithZeros() {
        allMatrixFields.forEach(field -> {
            if (field.getText().trim().isEmpty()) {
                field.setText("0");
            }
        });
    }

    // --- NUEVO MÉTODO PARA CORROBORACIÓN ---
    @FXML
    private void onCheckSolution() {
        if (lastSolutionSummary == null || !lastSolutionSummary.startsWith("Sistema Compatible Determinado")) {
            PopupManager.showInfo("Debe resolver un Sistema Compatible Determinado para corroborar.");
            return;
        }

        try {
            MatrixSolver.VerificationResult verification = MatrixSolver.checkSolution(
                    originalA_str,
                    originalB_str,
                    lastSolutionSummary,
                    useFractionsCheck.isSelected()
            );

            // Muestra el resultado de la verificación en un popup
            PopupManager.showInfo(verification.equationResults);

        } catch (Exception ex) {
            PopupManager.showError("Ocurrió un error al corroborar la solución: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    // ---------------------------------------

    @FXML
    private void onClear() {
        allMatrixFields.forEach(TextField::clear);
        resultArea.clear();
        clearErrorStyles();
        showStepsButton.setDisable(true);
        checkButton.setDisable(true); // Limpiar también deshabilita el botón
        detailedSteps = null;
        lastSolutionSummary = null; // Limpiar la solución almacenada
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

    private void setupFieldNavigation() {
        gridFields = new TextField[][]{
                {a11Field, a12Field, a13Field, b1Field},
                {a21Field, a22Field, a23Field, b2Field},
                {a31Field, a32Field, a33Field, b3Field}
        };

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                gridFields[row][col].setOnKeyPressed(this::handleArrowNavigation);
            }
        }
    }

    private void handleArrowNavigation(KeyEvent event) {
        TextField sourceField = (TextField) event.getSource();
        int currentRow = -1, currentCol = -1;

        // Encontrar la posición del campo actual
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (gridFields[row][col] == sourceField) {
                    currentRow = row;
                    currentCol = col;
                    break;
                }
            }
        }

        if (currentRow == -1) return; // No debería pasar

        KeyCode code = event.getCode();
        int nextRow = currentRow;
        int nextCol = currentCol;

        switch (code) {
            case UP:    nextRow = (currentRow > 0) ? currentRow - 1 : 2; break;
            case DOWN:  nextRow = (currentRow < 2) ? currentRow + 1 : 0; break;
            case LEFT:  nextCol = (currentCol > 0) ? currentCol - 1 : 3; break;
            case RIGHT: nextCol = (currentCol < 3) ? currentCol + 1 : 0; break;
            default: return; // No es una tecla de flecha
        }

        gridFields[nextRow][nextCol].requestFocus();
        event.consume(); // Evitar que la tecla haga otra cosa
    }

    /**
     * Obtiene el valor del campo como String y valida el formato.
     */
    private String getStringValue(TextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            field.getStyleClass().add("error-field");
            throw new IllegalArgumentException("El campo [" + field.getPromptText() + "] no puede estar vacío.");
        }

        // Permite comas como separador decimal antes de la validación.
        String parsedText = text.replace(',', '.');

        try {
            if (parsedText.contains("/")) {
                String[] parts = parsedText.split("/");
                if (parts.length != 2) throw new NumberFormatException();

                // Intenta parsear como double para verificar si son números válidos
                Double.parseDouble(parts[0].trim());
                double den = Double.parseDouble(parts[1].trim());

                if (Math.abs(den) < 1e-9) {
                    field.getStyleClass().add("error-field");
                    throw new IllegalArgumentException("El denominador no puede ser cero.");
                }
            } else {
                // Si no tiene '/', solo verifica que sea un número
                Double.parseDouble(parsedText);
            }
            // Retornamos el texto original (con comas si las tenía)
            // Ya que parseToDouble y parseToBigFraction lo manejarán.
            return text;

        } catch (NumberFormatException e) {
            field.getStyleClass().add("error-field");
            throw new IllegalArgumentException("Valor inválido en [" + field.getPromptText() + "]: " + text);
        }
    }



    private void clearErrorStyles() {
        allMatrixFields.forEach(field -> field.getStyleClass().remove("error-field"));
    }


}
