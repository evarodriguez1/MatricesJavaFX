package com.calculos.controllers;

import com.calculos.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class RootController {

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
            MainApp.setRoot(root, "Módulo - Área Bajo la Curva Cuadrática");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ SIN initialize() - SIN MÚSICA AQUÍ
}