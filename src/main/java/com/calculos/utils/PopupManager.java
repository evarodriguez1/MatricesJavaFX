package com.calculos.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import java.util.Optional;

public class PopupManager {

    private static final String CSS_PATH = "/styles/styles.css";

    /**
     * Aplica la hoja de estilos Tron a cualquier DialogPane (Alerts).
     * Esto carga el CSS y asigna la clase CSS ".dialog-pane" para activar el estilo.
     * @param dialogPane El panel del diálogo a estilizar.
     */
    private static void applyTronStyle(DialogPane dialogPane) {
        // Obtenemos la URL del recurso CSS usando el ClassLoader
        Optional<String> cssUrl = Optional.ofNullable(PopupManager.class.getResource(CSS_PATH))
                .map(url -> url.toExternalForm());

        if (cssUrl.isPresent()) {
            String cssPathString = cssUrl.get();

            // 1. Cargar el CSS si aún no está cargado
            if (!dialogPane.getStylesheets().contains(cssPathString)) {
                dialogPane.getStylesheets().add(cssPathString);
            }
        } else {
            System.err.println("Error: No se pudo encontrar el archivo CSS en la ruta: " + CSS_PATH);
        }

        // 2. Aplicar la clase CSS que define el estilo Tron
        if (!dialogPane.getStyleClass().contains("dialog-pane")) {
            dialogPane.getStyleClass().add("dialog-pane");
        }

        // Opcional: Eliminar el icono por defecto para mantener la estética limpia
        dialogPane.setGraphic(null);
    }


    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error del Sistema"); // Título ajustado para ser más general
        alert.setHeaderText(null);
        alert.setContentText(message);

        // 💥 Aplicar el estilo Tron antes de mostrar
        applyTronStyle(alert.getDialogPane());

        alert.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // 💥 Aplicar el estilo Tron antes de mostrar
        applyTronStyle(alert.getDialogPane());

        alert.showAndWait();
    }
}