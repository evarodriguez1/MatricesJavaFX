package com.calculos.controllers;

import com.calculos.navigation.Navigable;
import com.calculos.navigation.View;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;

/**
 * Controlador para la vista del Módulo de Probabilidades (ProbabilityView.fxml).
 * Versión 2.1 - Manejo de errores de carga mejorado.
 */
public final class ProbabilityController extends BaseController {

    @FXML private TabPane probabilityTabPane;
    @FXML private Tab binomialTab;
    @FXML private Tab normalTab;
    @FXML private Tab hypergeometricTab;
    @FXML private Tab poissonTab;

    @FXML
    public void initialize() {
        bindTabToView(binomialTab, View.BINOMIAL);
        bindTabToView(normalTab, View.NORMAL);
        bindTabToView(hypergeometricTab, View.HYPERGEOMETRIC);
        bindTabToView(poissonTab, View.POISSON);

        // Intenta precargar la primera pestaña. Si falla, el error será informativo.
        loadTabContent(binomialTab, View.BINOMIAL);
    }

    private void bindTabToView(Tab tab, View view) {
        tab.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                loadTabContent(tab, view);
            }
        });
    }

    private void loadTabContent(Tab tab, View view) {
        if (tab.getContent() != null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(view.getFxmlFile()));
            Node content = loader.load();

            Object controller = loader.getController();
            if (controller instanceof Navigable) {
                ((Navigable) controller).setNavigationManager(this.navigationManager);
            }

            tab.setContent(content);

        } catch (IOException e) {
            // MEJORA CLAVE: El mensaje de error ahora especifica el archivo fallido.
            String errorMessage = "No se pudo cargar la vista " + view.getTitle();
            tab.setContent(new Label("Error crítico al cargar: " + view.getFxmlFile()));
            // Usamos el handler genérico que muestra el StackTrace.
            handleGenericException(new IOException(errorMessage, e));
        } catch (Exception e) {
            handleGenericException(e);
        }
    }
}