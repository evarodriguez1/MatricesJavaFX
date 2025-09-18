package com.matrices;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
        Parent root = loader.load();  // guardamos el root
        Scene scene = new Scene(root); // creamos la escena

        // ⚡ Agregar CSS
        scene.getStylesheets().add(
                getClass().getResource("/styles/styles.css").toExternalForm()
        );

        stage.setTitle("Matrices - Resolución de Sistemas");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

