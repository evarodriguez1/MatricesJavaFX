package com.calculos.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Gestor de navegación centralizado para la aplicación.
 * Es responsable de cargar y cambiar las escenas en la ventana principal (Stage).
 * Utiliza el enum View para mantener un control limpio de las rutas de FXML.
 */
public class NavigationManager {

    private final Stage primaryStage;

    public NavigationManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Carga y muestra una nueva vista en la ventana principal.
     *
     * @param view El enum de la vista a la que se desea navegar.
     */
    public void navigateTo(View view) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(view.getFxmlFile()));
            Parent root = loader.load();

            Scene scene = primaryStage.getScene();
            if (scene == null) {
                // Si es la primera escena, la crea
                scene = new Scene(root);
                // Carga las hojas de estilo una sola vez
                scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                // Si ya existe una escena, solo reemplaza el contenido
                scene.setRoot(root);
            }

            primaryStage.setTitle(view.getTitle());
            primaryStage.sizeToScene(); // Ajusta el tamaño de la ventana al contenido
            primaryStage.centerOnScreen(); // Centra la ventana

        } catch (IOException e) {
            // Manejo de error robusto
            e.printStackTrace(); // Log del error para el desarrollador
            // Aquí se llamaría a nuestro PopupManager refactorizado
            // PopupManager.showErrorWithDetails("No se pudo cargar la vista: " + view.getTitle(), e);
        }
    }
}