package org.example.trabajo_recuperacion;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public class Controler {
    @FXML
    private Button buttonModificar;
    private final ArrayList<Trabajador> ListaTrabajadores = new ArrayList<>();
    @FXML
    private Label welcomeText;
    @FXML
    private TextField txtFieldNombre;
    @FXML
    private TextField FieldtextSalario;
    @FXML
    private Button añadirEmpleado;
    @FXML
    private Label ConsultarID;
    @FXML
    private Label ConsultarNombre;
    @FXML
    private Label ConsultarPuesto;
    @FXML
    private Label ConsultarSueldo;
    @FXML
    private Label ConsultarFecha;
    @FXML
    private Button ButtonRefrescar;
    @FXML
    private Button buttonEliminar;
    @FXML
    private ListView<String> listViewTrabajadores;
    @FXML
    private ComboBox Puesto;

    private LocalDate fechaAlta = LocalDate.now();

    @FXML
    public Trabajador crearTrabajador() {
        String nombre = txtFieldNombre.getText();
        int salario;
        try {
            salario = Integer.parseInt(FieldtextSalario.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("El salario debe ser un número válido");
            return null;
        }
        String puesto = (String) Puesto.getValue();
        System.out.println(">>> Datos del nuevo trabajador:");
        System.out.println(" - Nombre ingresado: " + nombre);
        System.out.println(" - Cargo seleccionado: " + puesto);
        System.out.println(" - Sueldo asignado: " + salario);
        System.out.println(" - Fecha de ingreso: " + fechaAlta);
        return new Trabajador(nombre, puesto, salario, fechaAlta);
    }

    public void anyadirEmpleados(Trabajador t) {
        ListaTrabajadores.add(t);
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Información");
        alerta.setHeaderText("Operación completada");
        alerta.setContentText(mensaje);
        ButtonType botonAceptar = new ButtonType("Entendido");
        alerta.getButtonTypes().setAll(botonAceptar);
        alerta.setOnCloseRequest(event -> {
            txtFieldNombre.clear();
            FieldtextSalario.clear();
            Puesto.setValue(null);
        });
        alerta.showAndWait();
    }

    @FXML
    public void añadirEmpleado() {
        if (txtFieldNombre.getText().isEmpty() || FieldtextSalario.getText().isEmpty() || Puesto.getValue() == null) {
            System.out.println("⚠️ Por favor, completa todos los campos antes de continuar.");
            return;
        }
        Trabajador nuevoTrabajador = crearTrabajador();
        nuevoTrabajador.setFechaAlta(LocalDate.now());
        anyadirEmpleados(nuevoTrabajador);

        MYSQL.insertar(nuevoTrabajador);
        mostrarAlerta("El trabajador ha sido añadido exitosamente.");
    }

    public void LeerTrabajadores() {
        InputStream inputStream = getClass().getResourceAsStream("/trabajadores.txt");
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String lineaTrabajador;
                while ((lineaTrabajador = reader.readLine()) != null) {
                    Trabajador trabajador = parsearLinea(lineaTrabajador);
                    if (trabajador != null) {
                        ListaTrabajadores.add(trabajador);
                        MYSQL.insertar(trabajador);
                        System.out.println("✔ Trabajador importado: " + trabajador.getNombre());
                    } else {
                        System.out.println("❌ No se pudo interpretar esta línea: " + lineaTrabajador);
                    }
                }
            } catch (Exception e) {
                System.err.println("‼ Error al intentar cargar los trabajadores: " + e.getMessage());
            }
        } else {
            System.err.println("📂 Archivo de trabajadores no encontrado.");
        }
    }

    public Trabajador parsearLinea(String trabajadorLinea) {
        System.out.println(">>> Procesando línea del archivo: " + trabajadorLinea);
        String[] tokens = trabajadorLinea.split(";");
        System.out.println("Número de datos encontrados: " + tokens.length);
        try {
            if (tokens.length >= 3) {
                String nombre = tokens[0];
                String cargo = tokens[1];
                int salario = Integer.parseInt(tokens[2]);
                return new Trabajador(nombre, cargo, salario, fechaAlta);
            } else {
                System.err.println("⚠ Línea incompleta. No se puede procesar.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.err.println("⚠ Error: El salario no es un número válido.");
            return null;
        }
    }

    @FXML
    public void clickrefrescar() {
        listViewTrabajadores.getItems().clear();

        if (ListaTrabajadores.isEmpty()) {
            LeerTrabajadores();
        }

        for (Trabajador t : ListaTrabajadores) {
            listViewTrabajadores.getItems().add(t.getNombre());
            System.out.println("📋 Añadido al listado: " + t.getNombre());
        }
    }

    @FXML
    public void initialize() {
        clickrefrescar();
        eliminarTrabajador();
        listViewTrabajadores.setOnMouseClicked(event -> {
            eliminarTrabajador();
            int indiceSeleccionado = listViewTrabajadores.getSelectionModel().getSelectedIndex();

            if (indiceSeleccionado >= 0) {
                Trabajador trabajadorSeleccionado = ListaTrabajadores.get(indiceSeleccionado);
                ConsultarID.setText(String.valueOf(trabajadorSeleccionado.getId()));
                ConsultarNombre.setText(trabajadorSeleccionado.getNombre());
                ConsultarPuesto.setText(trabajadorSeleccionado.getPuesto());
                ConsultarSueldo.setText(String.valueOf(trabajadorSeleccionado.getSalario()));
                LocalDate fechaAlta = trabajadorSeleccionado.getFechaAlta();
                if (fechaAlta != null) {
                    ConsultarFecha.setText(fechaAlta.toString());
                } else {
                    ConsultarFecha.setText("No disponible");
                }
            }
        });
    }

    public void eliminarTrabajador() {
        buttonEliminar.setOnAction(event -> {
            int indiceSeleccionado = listViewTrabajadores.getSelectionModel().getSelectedIndex();

            if (indiceSeleccionado >= 0) {
                Trabajador trabajadorSeleccionado = ListaTrabajadores.get(indiceSeleccionado);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmar eliminación");
                alert.setHeaderText("¿Estás seguro de que quieres eliminar a " + trabajadorSeleccionado.getNombre() + "? Esta acción es irreversible.");

                ButtonType buttonTypeSi = new ButtonType("Eliminar");
                ButtonType buttonTypeNo = new ButtonType("Cancelar");
                alert.getButtonTypes().setAll(buttonTypeSi, buttonTypeNo);

                Optional<ButtonType> resultado = alert.showAndWait();

                if (resultado.isPresent() && resultado.get() == buttonTypeSi) {
                    MYSQL.eliminar(trabajadorSeleccionado);
                    ConfirmarDelete();
                }
            }
        });
    }

    private void ConfirmarDelete() {
        int indiceSeleccionado = listViewTrabajadores.getSelectionModel().getSelectedIndex();

        if (indiceSeleccionado >= 0) {
            Trabajador trabajadorSeleccionado = ListaTrabajadores.get(indiceSeleccionado);
            ListaTrabajadores.remove(trabajadorSeleccionado);

            ConsultarID.setText("");
            ConsultarNombre.setText("");
            ConsultarPuesto.setText("");
            ConsultarSueldo.setText("");
            ConsultarFecha.setText("");

            listViewTrabajadores.getItems().remove(indiceSeleccionado);

            System.out.println("🗑 Trabajador eliminado: " + trabajadorSeleccionado.getNombre());
        } else {
            System.out.println("⚠ No se ha seleccionado ningún trabajador para eliminar.");
        }
    }

    @FXML
    private void modificarButton() {
        String nombreSeleccionado = listViewTrabajadores.getSelectionModel().getSelectedItem();
        Trabajador trabajadorSeleccionado = null;

        for (Trabajador trabajador : ListaTrabajadores) {
            if (trabajador.getNombre().equals(nombreSeleccionado)) {
                trabajadorSeleccionado = trabajador;
                break;
            }
        }

        if (trabajadorSeleccionado != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("modificar.fxml"));
                Parent root = loader.load();

                Modificar modificarController = loader.getController();
                
                Stage stage = new Stage();
                stage.setTitle("Modificar Trabajador");
                stage.setScene(new Scene(root, 1000, 800));
                stage.setResizable(false);
                
                modificarController.init(trabajadorSeleccionado, stage);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠ No se ha seleccionado ningún trabajador para modificar.");
        }
    }
}
