package com.calculos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static MediaPlayer globalMusic; // ✅ ÚNICO EN TODO EL PROGRAMA

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("Calculadora de Matemática Aplicada");

        stage.setWidth(850);
        stage.setHeight(750);

        // ✅ MÚSICA PRIMERO - SOLO AQUÍ
        setupGlobalMusic();

        // Luego menú
        showMainMenuView();

        stage.show();
    }

    // ✅ MÚSICA GLOBAL - UNA SOLA VEZ
    private void setupGlobalMusic() {
        try {
            if (globalMusic == null) { // ✅ PREVIENE DUPLICADOS
                String musicPath = getClass().getResource("/audio/end_of_line_TRON.mp3").toExternalForm();
                Media music = new Media(musicPath);
                globalMusic = new MediaPlayer(music);

                globalMusic.setVolume(0.3);
                globalMusic.setCycleCount(MediaPlayer.INDEFINITE);
                globalMusic.play();

                System.out.println("🎵 MÚSICA INICIADA - UNA SOLA VEZ");
            }
        } catch (Exception e) {
            System.out.println("🎵 Audio no encontrado: " + e.getMessage());
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
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

    // ✅ SIN CONEXIÓN A CONTROLLER
    public static void showMainMenuView() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/views/RootView.fxml"));
        Parent root = loader.load();
        setRoot(root, "Calculadora de Matemática Aplicada - Menú Principal");
    }

    // ✅ CIERRA MÚSICA LIMPIO
    @Override
    public void stop() {
        if (globalMusic != null) {
            globalMusic.stop();
            System.out.println("🎵 MÚSICA DETENIDA");
        }
        try {
            super.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}