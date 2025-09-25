package com.matrices.controllers;

import com.matrices.models.MatrixSolver;
import com.matrices.utils.InputValidator;
import com.matrices.utils.PopupManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MainController {

    @FXML private TextField a11Field, a12Field, a13Field, b1Field;
    @FXML private TextField a21Field, a22Field, a23Field, b2Field;
    @FXML private TextField a31Field, a32Field, a33Field, b3Field;
    @FXML private TextArea resultArea;

    @FXML
    private void onSolve() {
        try {
            // Tomamos los valores de los TextFields y los convertimos a double
            double[][] A = {
                    {
                            InputValidator.parseDouble(a11Field.getText(), "a11"),
                            InputValidator.parseDouble(a12Field.getText(), "a12"),
                            InputValidator.parseDouble(a13Field.getText(), "a13")
                    },
                    {
                            InputValidator.parseDouble(a21Field.getText(), "a21"),
                            InputValidator.parseDouble(a22Field.getText(), "a22"),
                            InputValidator.parseDouble(a23Field.getText(), "a23")
                    },
                    {
                            InputValidator.parseDouble(a31Field.getText(), "a31"),
                            InputValidator.parseDouble(a32Field.getText(), "a32"),
                            InputValidator.parseDouble(a33Field.getText(), "a33")
                    }
            };

            double[] B = {
                    InputValidator.parseDouble(b1Field.getText(), "b1"),
                    InputValidator.parseDouble(b2Field.getText(), "b2"),
                    InputValidator.parseDouble(b3Field.getText(), "b3")
            };

            // Calculamos la solución usando Gauss-Jordan
            String result = MatrixSolver.solveGaussJordan(A, B);

            // Mostramos el resultado en el TextArea
            resultArea.setText(result);


        } catch (IllegalArgumentException ex) {
            // Si hubo un error en la entrada, usamos un popup
            PopupManager.showError(ex.getMessage());
        }
    }

    @FXML
    private void onClear() {
        clearFields();
    }

    private void clearFields() {
        a11Field.clear(); a12Field.clear(); a13Field.clear(); b1Field.clear();
        a21Field.clear(); a22Field.clear(); a23Field.clear(); b2Field.clear();
        a31Field.clear(); a32Field.clear(); a33Field.clear(); b3Field.clear();

        // AÑADIDO: También limpiamos el área de resultados.
        resultArea.clear();
    }

}
