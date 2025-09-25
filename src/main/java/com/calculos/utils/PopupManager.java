package com.calculos.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Gestor centralizado para la creación de diálogos de alerta (Popups).
 * Proporciona métodos estandarizados para mostrar errores, información, confirmaciones
 * y errores con detalles técnicos expandibles.
 */
public class PopupManager {

    /**
     * Muestra un diálogo de error simple.
     * @param message El mensaje de error a mostrar al usuario.
     */
    public static void showError(String message) {
        createAlert(Alert.AlertType.ERROR, "Error", message).showAndWait();
    }

    /**
     * Muestra un diálogo de información.
     * @param message El mensaje informativo.
     */
    public static void showInfo(String message) {
        createAlert(Alert.AlertType.INFORMATION, "Información", message).showAndWait();
    }

    /**
     * Muestra un diálogo de confirmación y espera la respuesta del usuario.
     * @param title El título de la ventana.
     * @param message El mensaje de la pregunta (ej: "¿Está seguro?").
     * @return true si el usuario presiona OK, false en cualquier otro caso.
     */
    public static boolean showConfirm(String title, String message) {
        Alert alert = createAlert(Alert.AlertType.CONFIRMATION, title, message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Muestra un diálogo de error con una sección expandible para detalles técnicos (Stack Trace).
     * @param message Mensaje principal del error, amigable para el usuario.
     * @param exception La excepción ocurrida, de la cual se extraerá el Stack Trace.
     */
    public static void showErrorWithDetails(String message, Exception exception) {
        Alert alert = createAlert(Alert.AlertType.ERROR, "Error Crítico", message);

        // --- Crear el contenido expandible ---
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Los detalles técnicos del error son:");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    /**
     * Método fábrica privado para crear y configurar una Alerta base.
     * Centraliza el estilo y la configuración común.
     */
    private static Alert createAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // No usamos el header por simplicidad estética
        alert.setContentText(message);

        // Aplicar estilos y asegurar que el tamaño sea adecuado
        alert.getDialogPane().getStylesheets().add(
                PopupManager.class.getResource("/styles/styles.css").toExternalForm()
        );
        // Aseguramos un ancho mínimo para que no se vea apretado
        alert.getDialogPane().setMinWidth(350);
        // Permite que la ventana del Alert sea redimensionable por el usuario
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.sizeToScene();

        return alert;
    }
}