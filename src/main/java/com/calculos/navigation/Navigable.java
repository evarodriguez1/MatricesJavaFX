package com.calculos.navigation;

/**
 * Interfaz que define un contrato para los controladores que necesitan
 * capacidades de navegación.
 *
 * Implementar esta interfaz asegura que el controlador tendrá un método estandarizado
 * para recibir la instancia del NavigationManager.
 */
public interface Navigable {

    /**
     * Proporciona al controlador la instancia del NavigationManager.
     * Este método se usa para inyectar la dependencia de navegación.
     *
     * @param navigationManager La instancia del gestor de navegación.
     */
    void setNavigationManager(NavigationManager navigationManager);

}