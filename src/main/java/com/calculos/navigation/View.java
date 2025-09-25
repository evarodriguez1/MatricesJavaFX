package com.calculos.navigation;

/**
 * Enum para gestionar de forma centralizada todas las vistas (FXML) de la aplicación.
 * Cada entrada define la ruta al archivo FXML y el título de la ventana asociado.
 * Esto evita tener Strings de rutas esparcidos por el código.
 */
public enum View {
    ROOT("/views/RootView.fxml", "Calculadora de Matemática Aplicada"),
    BINOMIAL("/views/BinomialView.fxml", "Distribución Binomial"),
    GAUSSIANA("/views/GaussianaView.fxml", "Distribución Normal (Gaussiana)"),
    HIPERGEOMETRICA("/views/HipergeometricaView.fxml", "Distribución Hipergeométrica"),
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

    public String getFxmlFile() {
        return fxmlFile;
    }

    public String getTitle() {
        return title;
    }
}