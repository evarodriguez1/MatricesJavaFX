package com.calculos.controllers;

import com.calculos.models.MatrixSolution;
import com.calculos.models.MatrixSolver;
import com.calculos.utils.InputValidator;
import com.calculos.utils.MathUtils; // Importamos nuestra nueva herramienta
import com.calculos.utils.exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.util.Arrays;
import java.util.List;

/**
 * Controlador para la vista de resolución de matrices.
 * - Gestiona una UI con notación matemática intuitiva.
 * - Implementa la funcionalidad de rellenar campos vacíos con ceros.
 * - Permite al usuario elegir el formato del resultado (Decimal o Fraccional).
 */
public final class MatrixController extends BaseController {

    // --- Componentes FXML de la Vista ---
    @FXML private TextField a11Field, a12Field, a13Field;
    @FXML private TextField a21Field, a22Field, a23Field;
    @FXML private TextField a31Field, a32Field, a33Field;
    @FXML private TextField b1Field, b2Field, b3Field;
    @FXML private ComboBox<ResultFormat> resultFormatComboBox;
    @FXML private TextArea resultArea;

    // --- Listas para Agrupación Lógica ---
    private List<TextField> allFields; // Una sola lista para todas las operaciones

    /** Enum interno para el formato de resultado. */
    private enum ResultFormat {
        DECIMAL("Decimal"),
        FRACTION("Fraccional");

        private final String displayName;
        ResultFormat(String name) { this.displayName = name; }
        @Override public String toString() { return displayName; }
    }

    /**
     * Se ejecuta DESPUÉS de que todos los @FXML han sido inyectados.
     * Agrupa los componentes en listas y configura el estado inicial.
     */
    @FXML
    public void initialize() {
        allFields = Arrays.asList(
                a11Field, a12Field, a13Field, b1Field,
                a21Field, a22Field, a23Field, b2Field,
                a31Field, a32Field, a33Field, b3Field
        );

        resultFormatComboBox.getItems().setAll(ResultFormat.values());
        resultFormatComboBox.getSelectionModel().select(ResultFormat.DECIMAL);
    }

    /**
     * Orquesta el flujo de cálculo al presionar "Resolver Sistema".
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
        allFields.forEach(TextField::clear);
        resultArea.clear();
    }

    /**
     * Rellena cualquier campo de entrada vacío con el valor "0".
     */
    @FXML
    private void onFillWithZeros() {
        allFields.stream()
                .filter(field -> field.getText() == null || field.getText().trim().isEmpty())
                .forEach(field -> field.setText("0"));
    }

    // --- Métodos Auxiliares Descompuestos ---

    private double[][] readMatrixFromUI() throws ValidationException {
        double[][] matrix = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int index = i * 4 + j; // Ajuste del índice por la nueva lista única
                TextField field = allFields.get(index);
                String fieldName = String.format("A[%d,%d]", i + 1, j + 1);
                matrix[i][j] = InputValidator.parseDouble(field.getText(), fieldName);
            }
        }
        return matrix;
    }

    private double[] readVectorFromUI() throws ValidationException {
        double[] vector = new double[3];
        // Los campos de B están en las posiciones 3, 7, 11 de la lista allFields
        for (int i = 0; i < 3; i++) {
            TextField field = allFields.get(i * 4 + 3);
            String fieldName = String.format("B[%d]", i + 1);
            vector[i] = InputValidator.parseDouble(field.getText(), fieldName);
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
            ResultFormat format = resultFormatComboBox.getValue();

            for (int i = 0; i < values.length; i++) {
                String valueStr = (format == ResultFormat.FRACTION)
                        ? MathUtils.toFraction(values[i])
                        : String.format("%.4f", values[i]);
                sb.append(String.format("x%d = %s\n", i + 1, valueStr));
            }
        }

        resultArea.setText(sb.toString());
    }
}