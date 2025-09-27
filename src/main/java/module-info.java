/**
 * Define el módulo principal de la aplicación "Calculadora de Matemática Aplicada".
 * Este descriptor de módulo especifica las dependencias del proyecto y los
 * paquetes que expone o abre para su uso por otros módulos, como JavaFX.
 *
 * Versión 2.0 Definitiva.
 * @author Tu Nombre (Equipo de Desarrollo)
 */
module com.calculos {
    // --- DEPENDENCIAS EXTERNAS (REQUIRES) ---
    // Dependencias fundamentales para la interfaz gráfica de JavaFX.
    requires javafx.controls;
    requires javafx.fxml;

    // Dependencias para la mejora de la UI y UX.
    requires org.controlsfx.controls; // Para componentes UI avanzados.
    requires org.kordamp.ikonli.javafx; // Motor principal para la renderización de iconos.

    // Packs de iconos específicos que estamos utilizando en los FXML.
    requires org.kordamp.ikonli.materialdesign2;

    // --- PAQUETES ABIERTOS (OPENS) ---
    // Abre el paquete de controladores al módulo FXML de JavaFX.
    // Esto es ESENCIAL para permitir que el FXMLLoader use "reflexión" para
    // inyectar los componentes de la vista (@FXML) en las variables del controlador.
    opens com.calculos.controllers to javafx.fxml;

    // --- PAQUETES EXPUESTOS (EXPORTS) ---
    // Expone el paquete principal 'com.calculos'.
    // Es necesario para que el lanzador de JavaFX (definido en el POM) pueda
    // encontrar y ejecutar la clase 'com.calculos.Launcher'.
    exports com.calculos;

    // Expone el paquete de navegación.
    // Esto permite una arquitectura más limpia, donde el lanzador y los
    // controladores pueden acceder a las clases de navegación como View y Navigable.
    exports com.calculos.navigation;
}