package com.calculos.controllers;

import com.calculos.models.MatrixSolution;
import com.calculos.models.MatrixSolver;
import com.calculos.utils.InputValidator;
import com.calculos.utils.exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.util.Arrays;
import java.util.List;

/**
 * Controlador para la vista de resolución de matrices.
 * Agrupa los TextFields inyectados en listas para un manejo de datos limpio
 * y delega la lógica de negocio al MatrixSolver.
 */
public class MatrixController extends BaseController {

    // --- Inyección Individual de Componentes FXML ---
    @FXML private TextField a11Field, a12Field, a13Field;
    @FXML private TextField a21Field, a22Field, a23Field;
    @FXML private TextField a31Field, a32Field, a33Field;
    @FXML private TextField b1Field, b2Field, b3Field;
    @FXML private TextArea resultArea;

    // --- Listas para Agrupación Lógica (construidas en código) ---
    private List<TextField> matrixFields;
    private List<TextField> vectorFields;

    /**
     * Este método se ejecuta DESPUÉS de que todos los @FXML han sido inyectados.
     * Es el lugar perfecto para agrupar los componentes en listas.
     */
    @FXML
    public void initialize() {
        // Construir las listas aquí garantiza que los campos no sean nulos.
        matrixFields = Arrays.asList(
                a11Field, a12Field, a13Field,
                a21Field, a22Field, a23Field,
                a31Field, a32Field, a33Field
        );
        vectorFields = Arrays.asList(b1Field, b2Field, b3Field);
    }

    /**
     * Maneja el evento de clic en el botón "Resolver".
     */
    @FXML
    private void onSolve() {
        try {
            double[][] matrixA = readMatrixFromUI();
            double[] vectorB = readVectorFromUI();
            MatrixSolution solution = MatrixSolver.solve(matrixA, vectorB);
            displaySolution(solution);
        } catch (ValidationException e) {
            handleValidationException(e);
        } catch (Exception e) {
            handleGenericException(e);
        }
    }

    /**
     * Limpia todos los campos de texto en la interfaz.
     */
    @FXML
    private void onClear() {
        matrixFields.forEach(TextField::clear);
        vectorFields.forEach(TextField::clear);
        resultArea.clear();
    }

    // --- El resto del código es idéntico al anterior, ya era correcto ---

    private double[][] readMatrixFromUI() throws ValidationException {
        double[][] matrix = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int index = i * 3 + j;
                String fieldName = String.format("A[%d,%d]", i + 1, j + 1);
                matrix[i][j] = InputValidator.parseDouble(matrixFields.get(index).getText(), fieldName);
            }
        }
        return matrix;
    }

    private double[] readVectorFromUI() throws ValidationException {
        double[] vector = new double[3];
        for (int i = 0; i < 3; i++) {
            String fieldName = String.format("B[%d]", i + 1);
            vector[i] = InputValidator.parseDouble(vectorFields.get(i).getText(), fieldName);
        }
        return vector;
    }

    private void displaySolution(MatrixSolution solution) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("--- %s ---\n", solution.type().getTitle()));
        sb.append(solution.type().getDescription()).append("\n\n");

        if (solution.hasUniqueSolution()) {
            sb.append("VALORES DE LAS INCÓGNITAS:\n");
            double[] values = solution.solutions().get();
            for (int i = 0; i < values.length; i++) {
                sb.append(String.format("x%d = %.4f\n", i + 1, values[i]));
            }
        }

        resultArea.setText(sb.toString());
    }
}