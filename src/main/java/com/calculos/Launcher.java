package com.calculos;

import com.calculos.navigation.NavigationManager;
import com.calculos.navigation.View;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Clase principal que lanza la aplicación.
 * Su única responsabilidad es iniciar el entorno de JavaFX,
 * instanciar el NavigationManager y delegar el control de la
 * primera vista a este.
 */
public class Launcher extends Application {

    /**
     * El punto de entrada estándar para una aplicación Java.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * El método de inicio del ciclo de vida de JavaFX.
     * Este método se llama después de que el sistema de JavaFX está inicializado.
     *
     * @param primaryStage La ventana principal (Stage) creada automáticamente por el framework.
     */
    @Override
    public void start(Stage primaryStage) {
        // 1. Instanciar nuestro gestor de navegación, entregándole el control de la ventana.
        NavigationManager navigationManager = new NavigationManager(primaryStage);

        // 2. Navegar a la vista inicial de la aplicación.
        navigationManager.navigateTo(View.ROOT);

        // 3. Configurar y mostrar la ventana principal.
        primaryStage.setMinWidth(600); // Evita que la ventana sea demasiado pequeña
        primaryStage.setMinHeight(400);
        primaryStage.show();
    }
}