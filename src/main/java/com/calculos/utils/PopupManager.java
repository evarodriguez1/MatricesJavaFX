package com.calculos.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.net.URL;
import java.util.Optional;

public class PopupManager {

    private static final String CSS_PATH = "/styles/styles.css";

    /**
     * Aplica la hoja de estilos personalizada a cualquier DialogPane (Alerts).
     * @param dialogPane El panel del diálogo a estilizar.
     */
    private static void applyTronStyle(DialogPane dialogPane) {
        try {
            Optional<String> cssUrl = Optional.ofNullable(PopupManager.class.getResource(CSS_PATH))
                    .map(URL::toExternalForm);

            cssUrl.ifPresent(css -> {
                if (!dialogPane.getStylesheets().contains(css)) {
                    dialogPane.getStylesheets().add(css);
                }
            });

            if (!dialogPane.getStyleClass().contains("dialog-pane")) {
                dialogPane.getStyleClass().add("dialog-pane");
            }

            dialogPane.setGraphic(null); // Eliminar icono por defecto.
        } catch (Exception e) {
            System.err.println("Error al aplicar estilos al Popup: " + e.getMessage());
        }
    }

    /**
     * Muestra un popup de error simple con un mensaje.
     * @param message El mensaje de error a mostrar.
     */
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error del Sistema");
        alert.setHeaderText(null);
        alert.setContentText(message);
        applyTronStyle(alert.getDialogPane());
        alert.showAndWait();
    }

    /**
     * Muestra un popup de información simple con un mensaje.
     * @param message El mensaje informativo a mostrar.
     */
    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        applyTronStyle(alert.getDialogPane());
        alert.showAndWait();
    }

    /**
     * MUEVO MÉTODO: Muestra un popup de información con contenido de texto extenso y scrollable.
     * Ideal para mostrar resultados detallados.
     * @param title El título de la ventana del popup.
     * @param header El texto de la cabecera dentro del popup.
     * @param content El texto extenso que se mostrará en el área de texto scrollable.
     */
    public static void showResultsPopup(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setFont(javafx.scene.text.Font.font("Monospaced", 14));
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expandableContent = new GridPane();
        expandableContent.setMaxWidth(Double.MAX_VALUE);
        expandableContent.add(textArea, 0, 0);

        alert.getDialogPane().setContent(expandableContent);
        alert.getDialogPane().setPrefSize(700, 500); // Tamaño inicial del popup.

        applyTronStyle(alert.getDialogPane());

        alert.showAndWait();
    }
}