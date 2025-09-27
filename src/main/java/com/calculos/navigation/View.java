package com.calculos.navigation;

/**
 * Enum para gestionar de forma centralizada todas las vistas (FXML) de la aplicación.
 * Cada entrada define la ruta al archivo FXML y el título de la ventana asociado,
 * promoviendo un código más limpio y a prueba de errores de tipeo.
 * ESTA ES LA VERSIÓN CORREGIDA Y ESTANDARIZADA.
 */
public enum View {
    ROOT("/views/RootView.fxml", "Calculadora Aplicada"),
    BINOMIAL("/views/BinomialView.fxml", "Distribución Binomial"),
    NORMAL("/views/NormalView.fxml", "Distribución Normal (Gaussiana)"),
    HYPERGEOMETRIC("/views/HypergeometricView.fxml", "Distribución Hipergeométrica"),
    POISSON("/views/PoissonView.fxml", "Distribución de Poisson"),
    MATRIX("/views/MatrixView.fxml", "Resolución de Matrices"),
    STATISTICS("/views/StatisticsView.fxml", "Estadística Descriptiva"),
    PROBABILITY("/views/ProbabilityView.fxml", "Módulo de Probabilidades");

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