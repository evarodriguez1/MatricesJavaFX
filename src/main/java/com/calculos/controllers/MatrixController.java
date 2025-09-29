package com.calculos.controllers;

import com.calculos.MainApp;
import com.calculos.models.MatrixSolver;
import com.calculos.utils.InputValidator;
import com.calculos.utils.PopupManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MatrixController {

    // --- Inyección de Componentes FXML ---
    @FXML private TextField a11Field, a12Field, a13Field, b1Field;
    @FXML private TextField a21Field, a22Field, a23Field, b2Field;
    @FXML private TextField a31Field, a32Field, a33Field, b3Field;
    @FXML private TextArea resultArea;

    // --- NUEVO: Lista para manejar todos los campos de forma eficiente ---
    // Esta lista nos permite evitar repetir código para limpiar, rellenar o validar los campos.
    private List<TextField> allMatrixFields;

    /**
     * El método initialize() es un método especial de JavaFX.
     * Se ejecuta automáticamente después de que se cargue el FXML y se inyecten todos los campos @FXML.
     * Es el lugar perfecto para realizar configuraciones iniciales.
     */
    @FXML
    public void initialize() {
        // Poblamos la lista con todos los TextFields de nuestra matriz.
        allMatrixFields = Arrays.asList(
                a11Field, a12Field, a13Field, b1Field,
                a21Field, a22Field, a23Field, b2Field,
                a31Field, a32Field, a33Field, b3Field
        );
    }

    /**
     * Resuelve el sistema de ecuaciones.
     * AHORA INCLUYE FEEDBACK VISUAL PARA ERRORES.
     */
    @FXML
    private void onSolve() {
        // Limpiamos cualquier estilo de error previo en cada intento de resolver.
        clearErrorStyles();

        try {
            double[][] A = {
                    {
                            getValidatedValue(a11Field, "A[1,1]"),
                            getValidatedValue(a12Field, "A[1,2]"),
                            getValidatedValue(a13Field, "A[1,3]")
                    },
                    {
                            getValidatedValue(a21Field, "A[2,1]"),
                            getValidatedValue(a22Field, "A[2,2]"),
                            getValidatedValue(a23Field, "A[2,3]")
                    },
                    {
                            getValidatedValue(a31Field, "A[3,1]"),
                            getValidatedValue(a32Field, "A[3,2]"),
                            getValidatedValue(a33Field, "A[3,3]")
                    }
            };

            double[] B = {
                    getValidatedValue(b1Field, "b[1]"),
                    getValidatedValue(b2Field, "b[2]"),
                    getValidatedValue(b3Field, "b[3]")
            };

            String result = MatrixSolver.solveGaussJordan(A, B);
            resultArea.setText(result);

        } catch (IllegalArgumentException ex) {
            // El helper 'getValidatedValue' ya se encargó de poner el campo en rojo.
            // Aquí solo mostramos el popup con el mensaje de error.
            PopupManager.showError(ex.getMessage());
        }
    }

    /**
     * NUEVO MÉTODO: Rellena todas las celdas vacías con "0".
     * Mejora la experiencia de usuario evitando errores por campos vacíos.
     */
    @FXML
    private void onFillWithZeros() {
        allMatrixFields.forEach(field -> {
            if (field.getText().trim().isEmpty()) {
                field.setText("0");
            }
        });
    }

    /**
     * MÉTODO REFACTORIZADO: Limpia todos los campos de entrada y el resultado.
     * Ahora es más mantenible gracias a nuestra lista 'allMatrixFields'.
     */
    @FXML
    private void onClear() {
        allMatrixFields.forEach(TextField::clear);
        resultArea.clear();
        clearErrorStyles(); // También eliminamos los estilos de error al limpiar.
    }

    /**
     * Navega de vuelta al menú principal. Sin cambios funcionales.
     */
    @FXML
    private void backToMenu() {
        try {
            MainApp.showMainMenuView();
        } catch (Exception e) {
            e.printStackTrace();
            PopupManager.showError("Error al cargar el menú principal: " + e.getMessage());
        }
    }

    // =============================================================
    // MÉTODOS DE AYUDA (Helpers) - Lógica Interna del Controlador
    // =============================================================

    /**
     * NUEVO HELPER: Valida un campo de texto y proporciona feedback visual.
     * Centraliza la lógica de validación y estilo.
     *
     * @param field     El TextField a validar.
     * @param fieldName El nombre del campo para los mensajes de error.
     * @return El valor double parseado.
     * @throws IllegalArgumentException si la validación falla.
     */
    private double getValidatedValue(TextField field, String fieldName) {
        try {
            double value = InputValidator.parseDouble(field.getText(), fieldName);
            // Si la validación es exitosa, nos aseguramos de que el campo no esté rojo.
            field.getStyleClass().remove("error-field");
            return value;
        } catch (IllegalArgumentException e) {
            // Si la validación falla, pintamos el campo de rojo.
            field.getStyleClass().add("error-field");
            // Y relanzamos la excepción para que el 'catch' de onSolve la capture y muestre el popup.
            throw e;
        }
    }

    /**
     * NUEVO HELPER: Elimina la clase de error de todos los campos.
     */
    private void clearErrorStyles() {
        allMatrixFields.forEach(field -> field.getStyleClass().remove("error-field"));
    }
}