package com.calculos.controllers;

import com.calculos.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class RootController {

    @FXML
    private void openStatisticsView() {
        try {
            // Carga la vista de Estadísticas (necesitas crear el FXML)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/StatisticsView.fxml"));
            Parent root = loader.load();
            MainApp.setRoot(root, "Módulo - Estadísticas");
        } catch (Exception e) {
            e.printStackTrace();
            // Implementa un PopupManager.showError aquí si lo necesitas
        }
    }

    @FXML
    private void openProbabilityView() {
        try {
            // Carga la vista de Probabilidad (necesitas crear el FXML)
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
            // Carga la vista de Matrices (es tu MatrixView.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MatrixView.fxml"));
            Parent root = loader.load();
            MainApp.setRoot(root, "Módulo - Matrices");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Nuevo método para abrir la vista de Cálculo de Área Cuadrática
     */
    @FXML
    private void openQuadraticAreaView() {
        try {
            // Carga la nueva vista del cálculo de área
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/QuadraticAreaView.fxml"));
            Parent root = loader.load();
            MainApp.setRoot(root, "Módulo - Área Bajo la Curva Cuadrática");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}