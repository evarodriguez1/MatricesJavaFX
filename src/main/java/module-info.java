module com.calculos {
    requires javafx.controls;
    requires javafx.fxml;
    requires commons.math3;


    opens com.calculos.controllers to javafx.fxml;
    exports com.calculos;
}
