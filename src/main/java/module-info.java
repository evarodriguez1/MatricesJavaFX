module com.calculos {
    requires javafx.controls;
    requires javafx.fxml;
    requires commons.math3;
    requires javafx.media;


    opens com.calculos.controllers to javafx.fxml;
    exports com.calculos;
}
