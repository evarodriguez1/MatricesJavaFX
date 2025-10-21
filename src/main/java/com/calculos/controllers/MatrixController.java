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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class MatrixController {

    @FXML private TextField a11Field, a12Field, a13Field, b1Field;
    @FXML private TextField a21Field, a22Field, a23Field, b2Field;
    @FXML private TextField a31Field, a32Field, a33Field, b3Field;
    @FXML private TextArea resultArea;
    @FXML private Button showStepsButton;
    @FXML private CheckBox useFractionsCheck;
    @FXML private Button checkButton;

    // ✅ CORRECCIÓN: La declaración @FXML que faltaba ha sido añadida.
    @FXML private Button solveButton;

    private List<TextField> allMatrixFields;
    private TextField[][] gridFields;
    private String detailedSteps;
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

        showStepsButton.setDisable(true);
        checkButton.setDisable(true);

        setupFieldNavigation();
    }

    @FXML
    private void onSolve() {
        clearErrorStyles();
        showStepsButton.setDisable(true);
        checkButton.setDisable(true);
        detailedSteps = null;
        lastSolutionSummary = null;

        try {
            String[][] A_temp = {
                    { getStringValue(a11Field), getStringValue(a12Field), getStringValue(a13Field) },
                    { getStringValue(a21Field), getStringValue(a22Field), getStringValue(a23Field) },
                    { getStringValue(a31Field), getStringValue(a32Field), getStringValue(a33Field) }
            };

            String[] B_temp = { getStringValue(b1Field), getStringValue(b2Field), getStringValue(b3Field) };

            this.originalA_str = A_temp;
            this.originalB_str = B_temp;

            MatrixSolver.SolveResult result = MatrixSolver.solveGaussJordan(originalA_str, originalB_str, useFractionsCheck.isSelected());

            resultArea.setText(result.summary);
            detailedSteps = result.steps;
            lastSolutionSummary = result.summary;

            showStepsButton.setDisable(false);

            if (result.summary.startsWith("Sistema Compatible Determinado")) {
                checkButton.setDisable(false);
            }
        } catch (IllegalArgumentException ex) {
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

        PopupManager.showResultsPopup(
                "Resolución Paso a Paso",
                "Detalle del método de Gauss-Jordan aplicado:",
                detailedSteps
        );
    }

    @FXML
    private void onFillWithZeros() {
        allMatrixFields.forEach(field -> {
            if (field.getText().trim().isEmpty()) {
                field.setText("0");
            }
        });
    }

    @FXML
    private void onCheckSolution() {
        if (lastSolutionSummary == null || !lastSolutionSummary.startsWith("Sistema Compatible Determinado")) {
            PopupManager.showInfo("Debe resolver un Sistema Compatible Determinado para poder corroborar.");
            return;
        }
        try {
            MatrixSolver.VerificationResult verification = MatrixSolver.checkSolution(
                    originalA_str,
                    originalB_str,
                    lastSolutionSummary,
                    useFractionsCheck.isSelected()
            );

            String title = verification.isCorrect ? "Solución Verificada" : "Error en la Solución";
            String header = verification.isCorrect ? "¡La solución es correcta!" : "La solución no coincide.";

            PopupManager.showResultsPopup(title, header, verification.equationResults);

        } catch (Exception ex) {
            PopupManager.showError("Ocurrió un error al corroborar la solución: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void onClear() {
        allMatrixFields.forEach(TextField::clear);
        resultArea.clear();
        clearErrorStyles();
        showStepsButton.setDisable(true);
        checkButton.setDisable(true);
        detailedSteps = null;
        lastSolutionSummary = null;
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
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (gridFields[row][col] == sourceField) {
                    currentRow = row;
                    currentCol = col;
                    break;
                }
            }
        }

        if (currentRow == -1) return;

        KeyCode code = event.getCode();
        int nextRow = currentRow;
        int nextCol = currentCol;

        switch (code) {
            case UP:    nextRow = (currentRow > 0) ? currentRow - 1 : 2; break;
            case DOWN:  nextRow = (currentRow < 2) ? currentRow + 1 : 0; break;
            case LEFT:  nextCol = (currentCol > 0) ? currentCol - 1 : 3; break;
            case RIGHT: nextCol = (currentCol < 3) ? currentCol + 1 : 0; break;
            default: return;
        }

        gridFields[nextRow][nextCol].requestFocus();
        event.consume();
    }

    // He notado que te faltaba un import estático en la versión que me enviaste. Lo he eliminado para evitar errores.
    private static final double EPSILON_LOCAL = 1e-9;
    private String getStringValue(TextField field) {
        String text = field.getText().trim();
        String fieldName = field.getPromptText();
        if (text.isEmpty()) {
            field.getStyleClass().add("error-field");
            throw new IllegalArgumentException("El campo " + fieldName + " no puede estar vacío.");
        }

        String parsedText = text.replace(',', '.');
        try {
            if (parsedText.contains("/")) {
                String[] parts = parsedText.split("/");
                if (parts.length != 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                    throw new NumberFormatException();
                }
                Double.parseDouble(parts[0].trim());
                double den = Double.parseDouble(parts[1].trim());
                if (Math.abs(den) < EPSILON_LOCAL) {
                    field.getStyleClass().add("error-field");
                    throw new IllegalArgumentException("El denominador no puede ser cero.");
                }
            } else {
                Double.parseDouble(parsedText);
            }
            return text;
        } catch (NumberFormatException e) {
            field.getStyleClass().add("error-field");
            throw new IllegalArgumentException("Valor inválido en " + fieldName + ": '" + text + "' no es un número o fracción válida.");
        }
    }

    private void clearErrorStyles() {
        allMatrixFields.forEach(field -> field.getStyleClass().remove("error-field"));
    }
}