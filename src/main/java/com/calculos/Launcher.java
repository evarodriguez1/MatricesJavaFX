package com.calculos;

import com.calculos.navigation.NavigationManager;
import com.calculos.navigation.View;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Clase principal que lanza la aplicación.
 * Establece las propiedades de la ventana principal y delega
 * el control de la navegación al NavigationManager.
 */
public class Launcher extends Application {

    // Definimos las dimensiones estándar de la aplicación como constantes.
    private static final double INITIAL_APP_WIDTH = 950;
    private static final double INITIAL_APP_HEIGHT = 750;
    private static final double MIN_APP_WIDTH = 800;
    private static final double MIN_APP_HEIGHT = 650;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 1. Instanciar nuestro gestor de navegación.
        NavigationManager navigationManager = new NavigationManager(primaryStage);

        // 2. Configurar las propiedades de la ventana (Stage).
        primaryStage.setTitle("Calculadora de Matemática Aplicada");
        primaryStage.setWidth(INITIAL_APP_WIDTH);
        primaryStage.setHeight(INITIAL_APP_HEIGHT);
        primaryStage.setMinWidth(MIN_APP_WIDTH); // Fija el tamaño
        primaryStage.setMinHeight(MIN_APP_HEIGHT);
        primaryStage.setResizable(true);

        // 3. Navegar a la vista inicial.
        navigationManager.navigateTo(View.ROOT);

        // 4. Mostrar la ventana ya configurada.
        primaryStage.show();
        primaryStage.centerOnScreen(); // Centrar después de mostrar para mejor precisión
    }
}