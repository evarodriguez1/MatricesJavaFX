module com.calculos {
    // Requerimientos de JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    // Requerimientos de librerías externas
    requires commons.math3;
    requires org.controlsfx.controls;

    // Abres los paquetes a JavaFX para que pueda usar reflexión
    opens com.calculos to javafx.fxml;
    opens com.calculos.controllers to javafx.fxml;

    // Exportas el paquete principal para el lanzador
    exports com.calculos;
}