/**
 * Define el módulo principal de la aplicación "Calculadora de Matemática Aplicada".
 * Este descriptor de módulo es el guardián de la encapsulación, especificando
 * las dependencias, los paquetes expuestos y los permisos de reflexión necesarios
 * para que la aplicación funcione de manera robusta y segura.
 */
module com.calculos {

    // --- DEPENDENCIAS EXTERNAS (REQUIRES) ---
    // Declara los módulos externos que nuestra aplicación necesita para funcionar.

    // Dependencias fundamentales para la interfaz gráfica de JavaFX.
    requires javafx.controls;
    requires javafx.fxml;

    // Dependencias para la mejora de la UI y UX.
    requires org.controlsfx.controls; // Para componentes UI avanzados que podríamos añadir.
    requires org.kordamp.ikonli.javafx; // Motor principal para la renderización de iconos.
    requires org.kordamp.ikonli.materialdesign2; // Pack específico de iconos que estamos utilizando en los FXML.


    // --- PAQUETES ABIERTOS PARA REFLEXIÓN (OPENS) ---
    // Concede permisos especiales a módulos específicos para que accedan a nuestros paquetes internos.

    // Abre el paquete de controladores al módulo FXML de JavaFX.
    // Esto es ESENCIAL para permitir que FXMLLoader use "reflexión" para inyectar
    // los componentes de la vista (@FXML) en las variables del controlador.
    opens com.calculos.controllers to javafx.fxml;

    // Abre el paquete de modelos a JavaFX base. Esto es una buena práctica para
    // componentes de UI como TableView o PropertyValueFactory que podrían necesitar
    // acceso por reflexión a los getters de las clases del modelo.
    opens com.calculos.models to javafx.base;


    // --- PAQUETES EXPUESTOS PÚBLICAMENTE (EXPORTS) ---
    // Hace que las clases públicas de estos paquetes sean visibles para otros módulos.

    // Expone el paquete principal 'com.calculos'.
    // Necesario para que el lanzador de JavaFX (definido en el POM) pueda
    // encontrar y ejecutar la clase 'com.calculos.Launcher'.
    exports com.calculos;

    // Expone el paquete de navegación.
    // Permite una arquitectura limpia, donde el lanzador y los controladores pueden
    // usar las clases de navegación como View y Navigable.
    exports com.calculos.navigation;

    // Expone el paquete de excepciones personalizadas.
    // Permite que otras partes de la aplicación puedan capturar nuestras excepciones
    // específicas, como ValidationException.
    exports com.calculos.utils.exceptions;
}