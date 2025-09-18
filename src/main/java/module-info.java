module com.matrices {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.matrices.controllers to javafx.fxml;
    exports com.matrices;
}
