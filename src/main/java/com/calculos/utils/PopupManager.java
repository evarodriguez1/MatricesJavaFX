package com.calculos.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Gestor centralizado para la creación de diálogos de alerta (Popups).
 * Proporciona métodos estandarizados para mostrar errores, información, confirmaciones
 * y errores con detalles técnicos expandibles, garantizando una UI/UX consistente.
 */
public final class PopupManager {

    /**
     * Constructor privado para prevenir instanciación.
     */
    private PopupManager() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada.");
    }

    /**
     * Muestra un diálogo de error simple con un mensaje claro para el usuario.
     * @param message El mensaje de error a mostrar.
     */
    public static void showError(String message) {
        createAlert(Alert.AlertType.ERROR, "Error", message).showAndWait();
    }

    /**
     * Muestra un diálogo de información.
     * @param message El mensaje informativo a mostrar.
     */
    public static void showInfo(String title, String message) {
        createAlert(Alert.AlertType.INFORMATION, title, message).showAndWait();
    }

    /**
     * Muestra un diálogo de confirmación y espera la respuesta del usuario.
     * @param title El título de la ventana de confirmación.
     * @param message El mensaje de la pregunta (ej: "¿Está seguro de que desea limpiar todos los campos?").
     * @return true si el usuario presiona OK, false en cualquier otro caso.
     */
    public static boolean showConfirm(String title, String message) {
        Alert alert = createAlert(Alert.AlertType.CONFIRMATION, title, message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Muestra un diálogo de error con una sección expandible para detalles técnicos (Stack Trace).
     * Fundamental para la depuración y para manejar errores inesperados.
     * @param message Mensaje principal del error, amigable para el usuario.
     * @param exception La excepción ocurrida, de la cual se extraerá el Stack Trace.
     */
    public static void showErrorWithDetails(String message, Exception exception) {
        Alert alert = createAlert(Alert.AlertType.ERROR, "Error Inesperado", message);

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

        // Asignar el contenido expandible al diálogo.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    /**
     * Método fábrica privado para crear y configurar una Alerta base.
     * Centraliza el estilo y la configuración común para mantener la consistencia.
     */
    private static Alert createAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // No usamos el header por simplicidad estética y claridad.
        alert.setContentText(message);

        // Asegura que el popup bloquee la ventana principal.
        alert.initModality(Modality.APPLICATION_MODAL);

        // Intentar aplicar los estilos. Si no se encuentran, la alerta funcionará igualmente.
        try {
            alert.getDialogPane().getStylesheets().add(
                    PopupManager.class.getResource("/styles/styles.css").toExternalForm()
            );
        } catch (NullPointerException e) {
            System.err.println("Advertencia: No se pudo encontrar la hoja de estilos 'styles.css'.");
        }

        // Aseguramos un ancho mínimo para que el contenido no se vea apretado.
        alert.getDialogPane().setMinWidth(400);

        return alert;
    }
}