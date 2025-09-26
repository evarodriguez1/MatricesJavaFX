package com.calculos.navigation;

/**
 * Enum para gestionar de forma centralizada todas las vistas (FXML) de la aplicación.
 * Cada entrada define la ruta al archivo FXML y el título de la ventana asociado,
 * promoviendo un código más limpio y a prueba de errores de tipeo.
 *
 * @author Tu Nombre (Equipo de Desarrollo)
 */
public enum View {
    ROOT("/views/RootView.fxml", "Calculadora de Matemática Aplicada"),
    BINOMIAL("/views/BinomialView.fxml", "Distribución Binomial"),
    GAUSSIANA("/views/NormalView.fxml", "Distribución Normal (Gaussiana)"),
    HIPERGEOMETRICA("/views/HypergeometricView.fxml", "Distribución Hipergeométrica"),
    POISSON("/views/PoissonView.fxml", "Distribución de Poisson"),
    MATRIX("/views/MatrixView.fxml", "Resolución de Matrices (Gauss-Jordan)"),
    STATISTICS("/views/StatisticsView.fxml", "Cálculos de Estadística Descriptiva"),
    PROBABILITY("/views/ProbabilityView.fxml", "Menú de Probabilidades");

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