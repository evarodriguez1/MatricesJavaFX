package com.calculos;

import javafx.application.Application;
import javafx.stage.Stage;
import com.calculos.navigation.NavigationManager;
import com.calculos.navigation.View;

/**
 * Clase principal que lanza la aplicación. Su única responsabilidad es iniciar
 * la infraestructura de JavaFX y delegar el control de la navegación
 * al NavigationManager.
 */
public class Launcher extends Application {

    public static void main(String[] args) {
        // Esta es la entrada original a toda la aplicación
        launch(args);
    }

    /**
     * El método de inicio de JavaFX.
     * @param primaryStage La ventana principal creada por el framework.
     */
    @Override
    public void start(Stage primaryStage) {
        // 1. Crea el gestor de navegación, pasándole la ventana principal
        NavigationManager navigationManager = new NavigationManager(primaryStage);

        // 2. Le dice al gestor que navegue a la primera vista (el menú principal)
        navigationManager.navigateTo(View.ROOT);

        // 3. Muestra la ventana
        primaryStage.show();
    }
}