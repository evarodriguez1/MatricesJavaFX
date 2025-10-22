package com.calculos.controllers;

import com.calculos.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class RootController implements Initializable {

    @FXML
    private Button muteButton;

    @FXML
    private ImageView muteIconImageView; // Cambiado de FontIcon a ImageView

    // Imágenes para los dos estados del botón
    private Image iconSoundOn;
    private Image iconSoundOff;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cargamos las imágenes una sola vez
        try {
            iconSoundOn = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/audio/speaker_on.png")));
            iconSoundOff = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/audio/speaker_off.png")));
        } catch (Exception e) {
            System.err.println("Error: No se encontraron los iconos de sonido. Verifica que 'speaker_on.png' y 'speaker_off.png' estén en la carpeta resources/audio.");
            muteButton.setDisable(true); // Desactivar el botón si no hay iconos
            return;
        }

        MediaPlayer globalMusic = MainApp.getGlobalMusicPlayer();
        if (globalMusic != null) {
            updateButtonState(globalMusic.isMute());
        } else {
            muteButton.setDisable(true);
        }
    }

    @FXML
    private void toggleMute() {
        MediaPlayer globalMusic = MainApp.getGlobalMusicPlayer();
        if (globalMusic == null) return;

        boolean isNowMuted = !globalMusic.isMute();
        globalMusic.setMute(isNowMuted);
        updateButtonState(isNowMuted);
    }

    private void updateButtonState(boolean isMuted) {
        if (isMuted) {
            muteIconImageView.setImage(iconSoundOff);
            if (!muteButton.getStyleClass().contains("muted")) {
                muteButton.getStyleClass().add("muted");
            }
        } else {
            muteIconImageView.setImage(iconSoundOn);
            muteButton.getStyleClass().remove("muted");
        }
    }

    @FXML
    private void openStatisticsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/StatisticsView.fxml"));
            Parent root = loader.load();
            MainApp.setRoot(root, "Módulo - Estadísticas");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openProbabilityView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ProbabilityView.fxml"));
            Parent root = loader.load();
            MainApp.setRoot(root, "Módulo - Probabilidades");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openMatrixView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MatrixView.fxml"));
            Parent root = loader.load();
            MainApp.setRoot(root, "Módulo - Matrices");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openQuadraticAreaView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/QuadraticAreaView.fxml"));
            Parent root = loader.load();
            MainApp.setRoot(root, "Módulo - Área de regiones curvas");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}