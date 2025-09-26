package com.calculos.navigation;

import com.calculos.controllers.BaseController; // Importante
import com.calculos.utils.PopupManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class NavigationManager {

    private final Stage primaryStage;
    private Scene scene;

    public NavigationManager(Stage primaryStage) {
        this.primaryStage = Objects.requireNonNull(primaryStage, "Primary stage no puede ser nulo.");
    }

    public void navigateTo(View view) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(view.getFxmlFile()));
            Parent root = loader.load();

            // --- INYECCIÓN DE DEPENDENCIA ---
            // Después de cargar, obtenemos la instancia del controlador
            Object controller = loader.getController();

            // Si el controlador necesita navegar (es decir, hereda de BaseController o implementa Navigable)
            if (controller instanceof Navigable) {
                // Le pasamos la instancia actual de NavigationManager.
                ((Navigable) controller).setNavigationManager(this);
            }
            // --- FIN DE LA INYECCIÓN ---

            if (scene == null) {
                scene = new Scene(root);
                String cssPath = Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm();
                scene.getStylesheets().add(cssPath);
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            primaryStage.setTitle(view.getTitle());
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
            primaryStage.setResizable(false);

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            PopupManager.showErrorWithDetails(
                    "Error crítico: No se pudo cargar la vista '" + view.getTitle() + "'.\n" +
                            "Verifique que el archivo FXML exista en la ruta correcta: " + view.getFxmlFile(), e
            );
        }
    }
}