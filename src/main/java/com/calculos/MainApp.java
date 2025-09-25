package com.calculos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class MainApp extends Application {

    // El Stage principal para cambiar escenas
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage; // Guardar la referencia al Stage
        stage.setTitle("Matemática Aplicada - Menú Principal");

        // Carga la vista principal del menú
        showMainMenuView();

        stage.show();
    }


    // Método estático para cambiar de escena (para usar desde los controladores)
    public static void setRoot(Parent root, String title) {
        Scene scene = new Scene(root);
        // Opcional: Agregar el CSS principal a todas las escenas si aplica
        scene.getStylesheets().add(
                MainApp.class.getResource("/styles/styles.css").toExternalForm()
        );
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene(); // Ajustar el tamaño a la nueva escena
        primaryStage.centerOnScreen();
    }

    // Método para volver al menú
    public static void showMainMenuView() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/views/RootView.fxml"));
        Parent root = loader.load();
        setRoot(root, "Matemática Aplicada - Menú Principal");
    }

    public static void main(String[] args) {
        launch();
    }
}
