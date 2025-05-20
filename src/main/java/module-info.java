module org.example.trabajo_recuperacion {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.trabajo_recuperacion to javafx.fxml;
    exports org.example.trabajo_recuperacion;
    exports org.example.trabajo_recuperacion.Modelo;
    opens org.example.trabajo_recuperacion.Modelo to javafx.fxml;
    exports org.example.trabajo_recuperacion.DAO;
    opens org.example.trabajo_recuperacion.DAO to javafx.fxml;

}