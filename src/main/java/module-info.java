module com.calculos {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.calculos.controllers to javafx.fxml;
    exports com.calculos;
}
