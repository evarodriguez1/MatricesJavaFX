package com.calculos.controllers;

import com.calculos.navigation.Navigable;
import com.calculos.navigation.NavigationManager;
import com.calculos.navigation.View;
import com.calculos.utils.PopupManager;
import com.calculos.utils.exceptions.ValidationException;
import javafx.fxml.FXML;

/**
 * Un controlador base abstracto del que todos los demás controladores de vistas heredarán.
 * Proporciona funcionalidad común como la navegación y el manejo de errores estandarizado,
 * reduciendo así el código duplicado y promoviendo la consistencia.
 *
 * Implementa la interfaz Navigable para recibir el gestor de navegación.
 */
public abstract class BaseController implements Navigable {

    // La instancia del gestor de navegación, protegida para que las clases hijas la usen.
    protected NavigationManager navigationManager;

    /**
     * Implementación del método de la interfaz Navigable.
     * Guarda la instancia del gestor para su uso posterior.
     */
    @Override
    public void setNavigationManager(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
    }

    /**
     * Método de conveniencia para navegar de vuelta al menú principal.
     * Puede ser invocado por un botón "Volver" en cualquier vista.
     */
    @FXML
    protected void backToMenu() {
        if (navigationManager != null) {
            navigationManager.navigateTo(View.ROOT);
        }
    }

    /**
     * Manejador de errores de validación estandarizado.
     * Invoca al PopupManager para mostrar el error al usuario.
     *
     * @param e La ValidationException capturada.
     */
    protected void handleValidationException(ValidationException e) {
        PopupManager.showError(e.getMessage());
    }

    /**
     * Manejador de errores genéricos e inesperados.
     * Invoca al PopupManager para mostrar un error con detalles técnicos.
     *
     * @param e La Exception genérica capturada.
     */
    protected void handleGenericException(Exception e) {
        e.printStackTrace(); // Es crucial para la depuración en consola
        PopupManager.showErrorWithDetails("Ha ocurrido un error inesperado en la aplicación.", e);
    }
}