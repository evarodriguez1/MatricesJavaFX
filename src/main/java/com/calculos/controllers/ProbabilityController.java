package com.calculos.controllers;

import com.calculos.Launcher;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox; // O el layout principal

public class ProbabilityController {

    // Necesitas los elementos FXML que contendrán las vistas de cada distribución
    @FXML private VBox mainContainer;
    @FXML private TabPane probabilityTabPane; // Es una buena forma de organizar 4 vistas

    @FXML
    public void initialize() {
        // Inicializar o cargar las vistas FXML de cada distribución (BinomialView.fxml, etc.)
        // Opcional: podrías cargar solo la Binomial al inicio.
    }

    @FXML
    private void backToMenu() throws Exception {
        Launcher.showMainMenuView();
    }

    // Aquí irían los métodos para la Binomial, Gaussiana, etc.
    // Lo más sencillo es que cada pestaña (Tab) de TabPane tenga su propio controlador
    // para manejar los cálculos y resultados de esa distribución.

}