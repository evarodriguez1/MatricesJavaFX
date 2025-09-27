package com.calculos.navigation;

/**
 * Enum para gestionar de forma centralizada todas las vistas principales de la aplicación.
 * Cada entrada define la ruta al archivo FXML y el título de la ventana asociado.
 * Esta arquitectura simplificada evita la necesidad de rutas a sub-vistas,
 * ya que estas son gestionadas por sus controladores contenedores.
 */
public enum View {

    // Vistas principales a las que se puede navegar desde el menú.
    ROOT("/views/RootView.fxml", "Calculadora de Matemática Aplicada"),
    STATISTICS("/views/StatisticsView.fxml", "Módulo de Estadística Descriptiva"),
    PROBABILITY("/views/ProbabilityView.fxml", "Módulo de Cálculos de Probabilidad"),
    MATRIX("/views/MatrixView.fxml", "Módulo de Resolución de Matrices");

    private final String fxmlFile;
    private final String title;

    View(String fxmlFile, String title) {
        this.fxmlFile = fxmlFile;
        this.title = title;
    }

    /**
     * @return La ruta relativa al archivo FXML dentro de la carpeta 'resources'.
     */
    public String getFxmlFile() {
        return fxmlFile;
    }

    /**
     * @return El título a mostrar en la ventana cuando esta vista esté activa.
     */
    public String getTitle() {
        return title;
    }
}