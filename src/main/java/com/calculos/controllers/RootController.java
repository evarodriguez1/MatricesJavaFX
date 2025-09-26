package com.calculos.controllers;

import com.calculos.navigation.View;
import javafx.fxml.FXML;

/**
 * Controlador para la vista principal (RootView.fxml).
 * Actúa como el menú de navegación principal de la aplicación.
 * Hereda de BaseController para obtener la funcionalidad de navegación.
 */
public class RootController extends BaseController {

    /**
     * Navega a la vista de Estadísticas cuando se presiona el botón correspondiente.
     * Delega toda la lógica de navegación al NavigationManager.
     */
    @FXML
    private void openStatisticsView() {
        navigationManager.navigateTo(View.STATISTICS);
    }

    /**
     * Navega a la vista de Probabilidades cuando se presiona el botón correspondiente.
     */
    @FXML
    private void openProbabilityView() {
        navigationManager.navigateTo(View.PROBABILITY);
    }

    /**
     * Navega a la vista de Matrices cuando se presiona el botón correspondiente.
     */
    @FXML
    private void openMatrixView() {
        navigationManager.navigateTo(View.MATRIX);
    }
}