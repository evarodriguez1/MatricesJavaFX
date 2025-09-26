/**
 * Define el módulo principal de la aplicación "Calculadora de Matemática Aplicada".
 * Este descriptor de módulo especifica las dependencias del proyecto y los
 * paquetes que expone o abre para su uso por otros módulos, como JavaFX.
 */
module com.calculos {
    // --- DEPENDENCIAS EXTERNAS ---
    // Requiere los módulos fundamentales de JavaFX para la UI.
    requires javafx.controls;
    requires javafx.fxml;

    // Requiere los módulos de las librerías de terceros que hemos añadido.
    requires org.controlsfx.controls; // Para componentes UI avanzados
    requires org.kordamp.ikonli.javafx; // Para el motor de iconos
    requires org.kordamp.ikonli.materialdesign2; // Pack específico de iconos

    // --- PAQUETES ABIERTOS PARA REFLEXIÓN ---
    // Abre el paquete de controladores a JavaFX FXML para que pueda
    // inyectar los componentes de la UI (botones, campos de texto, etc.)
    // en las variables @FXML de los controladores. Esto es fundamental.
    opens com.calculos.controllers to javafx.fxml;

    // --- PAQUETES EXPUESTOS PÚBLICAMENTE ---
    // Expone únicamente el paquete 'com.calculos'. Esto es necesario para
    // que el lanzador de JavaFX (definido en el POM) pueda encontrar
    // y ejecutar la clase 'com.calculos.Launcher'.
    exports com.calculos;
    // Exportamos el paquete de navegación para que pueda ser utilizado
    // de manera externa en un futuro si se necesitase
    exports com.calculos.navigation;
}