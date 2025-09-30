package com.calculos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("Calculadora de Matemática Aplicada");

        stage.setWidth(850);
        stage.setHeight(750);

        // Carga la vista principal del menú
        showMainMenuView();

        stage.show();
    }

    public static void setRoot(Parent root, String title) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(MainApp.class.getResource("/styles/styles.css")).toExternalForm()
        );

        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    // Método para volver al menú
    public static void showMainMenuView() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/views/RootView.fxml"));
        Parent root = loader.load();
        setRoot(root, "Calculadora de Matemática Aplicada - Menú Principal");
    }

    public static void main(String[] args) {
        launch();
    }
}