module org.example.trabajo_recuperacion {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.trabajo_recuperacion to javafx.fxml;
    exports org.example.trabajo_recuperacion;
}