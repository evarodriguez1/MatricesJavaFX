package com.calculos.controllers;

import com.calculos.navigation.Navigable;
import com.calculos.navigation.NavigationManager;
import com.calculos.utils.PopupManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import java.io.IOException;

/**
 * Controlador para la vista del Módulo de Probabilidades (ProbabilityView.fxml).
 * Gestiona un TabPane que carga dinámicamente las vistas de cada una de las
 * distribuciones de probabilidad.
 * Implementa un mecanismo de "carga perezosa" (Lazy Loading) para optimizar
 * el rendimiento, cargando el FXML de cada pestaña solo la primera vez que
 * se selecciona.
 */
public class ProbabilityController extends BaseController {

    // --- Componentes FXML de la Vista ---
    @FXML private TabPane probabilityTabPane;
    @FXML private Tab binomialTab;
    @FXML private Tab normalTab;
    @FXML private Tab hypergeometricTab;
    @FXML private Tab poissonTab;

    /**
     * Se ejecuta una vez que los componentes FXML han sido inyectados.
     * Configura los listeners para la carga perezosa de las pestañas.
     */
    @FXML
    public void initialize() {
        // Añadir listeners a las propiedades 'selected' de cada Tab.
        // Esto activará la carga del FXML solo cuando la pestaña se seleccione por primera vez.
        binomialTab.selectedProperty().addListener(
                (obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected) {
                        loadTabContent(binomialTab, "/views/BinomialView.fxml");
                    }
                }
        );
        normalTab.selectedProperty().addListener(
                (obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected) {
                        loadTabContent(normalTab, "/views/NormalView.fxml");
                    }
                }
        );
        hypergeometricTab.selectedProperty().addListener(
                (obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected) {
                        loadTabContent(hypergeometricTab, "/views/HypergeometricView.fxml");
                    }
                }
        );
        poissonTab.selectedProperty().addListener(
                (obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected) {
                        loadTabContent(poissonTab, "/views/PoissonView.fxml");
                    }
                }
        );

        // Precargar la primera pestaña para una experiencia de usuario fluida al entrar.
        loadTabContent(binomialTab, "/views/BinomialView.fxml");
    }

    /**
     * Carga el contenido de un archivo FXML dentro de una pestaña específica.
     * Este método se asegura de que el contenido de cada pestaña se cargue una sola vez.
     *
     * @param tab La pestaña (Tab) que servirá como contenedor.
     * @param fxmlPath La ruta al archivo FXML que se cargará.
     */
    private void loadTabContent(Tab tab, String fxmlPath) {
        // Si la pestaña ya tiene contenido, no hacer nada (previene recargas).
        if (tab.getContent() != null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();

            // --- Inyección de Dependencia Manual ---
            // Le pasamos el NavigationManager al controlador de la pestaña cargada.
            Object controller = loader.getController();
            if (controller instanceof Navigable) {
                ((Navigable) controller).setNavigationManager(this.navigationManager);
            }

            tab.setContent(content);

        } catch (IOException e) {
            // Si una vista hija falla, se muestra un error claro en el área de contenido.
            tab.setContent(new Label("Error crítico: No se pudo cargar la vista desde " + fxmlPath));
            handleGenericException(e);
        }
    }
}