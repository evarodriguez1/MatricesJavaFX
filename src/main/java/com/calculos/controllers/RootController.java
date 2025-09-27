package com.calculos.controllers;

import com.calculos.navigation.View;
import javafx.fxml.FXML;

/**
 * Controlador para la vista principal de la aplicación (RootView.fxml).
 * Actúa como el menú de navegación central, proporcionando puntos de entrada
 * a los diferentes módulos de cálculo (Estadísticas, Probabilidades, Matrices).
 * Hereda de {@link BaseController} para obtener la funcionalidad de navegación
 * inyectada por {@link com.calculos.navigation.NavigationManager}.
 *
 * @author Tu Nombre (Equipo de Desarrollo)
 */
public final class RootController extends BaseController {

    /**
     * Navega al Módulo de Estadística Descriptiva.
     * Este método se invoca al hacer clic en el botón correspondiente en la UI.
     * Delega toda la lógica de carga y presentación de la vista al NavigationManager.
     */
    @FXML
    private void openStatisticsView() {
        navigationManager.navigateTo(View.STATISTICS);
    }

    /**
     * Navega al Módulo de Probabilidades.
     * Este método se invoca al hacer clic en el botón correspondiente en la UI.
     */
    @FXML
    private void openProbabilityView() {
        navigationManager.navigateTo(View.PROBABILITY);
    }

    /**
     * Navega al Módulo de Resolución de Matrices.
     * Este método se invoca al hacer clic en el botón correspondiente en la UI.
     */
    @FXML
    private void openMatrixView() {
        navigationManager.navigateTo(View.MATRIX);
    }
}