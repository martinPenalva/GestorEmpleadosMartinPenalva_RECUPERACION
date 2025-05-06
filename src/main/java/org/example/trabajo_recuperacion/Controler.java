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
    private Button modificarButton;
    private final ArrayList<Trabajador> listaTrabajadores = new ArrayList<>();
    @FXML
    private Label welcomeLabel;
    @FXML
    private TextField nombreTextField;
    @FXML
    private TextField salarioTextField;
    @FXML
    private Button añadirButton;
    @FXML
    private Label idLabel;
    @FXML
    private Label nombreLabel;
    @FXML
    private Label puestoLabel;
    @FXML
    private Label sueldoLabel;
    @FXML
    private Label fechaLabel;
    @FXML
    private Button refrescarButton;
    @FXML
    private Button eliminarButton;
    @FXML
    private ListView<String> trabajadoresListView;
    @FXML
    private ComboBox<String> puestoComboBox;

    private LocalDate fechaAlta = LocalDate.now();

    @FXML
    public Trabajador crearTrabajador() {
        String nombre = nombreTextField.getText();
        int salario;
        try {
            salario = Integer.parseInt(salarioTextField.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("El salario debe ser un número válido");
            return null;
        }
        String puesto = (String) puestoComboBox.getValue();
        System.out.println(">>> Datos del nuevo trabajador:");
        System.out.println(" - Nombre ingresado: " + nombre);
        System.out.println(" - Cargo seleccionado: " + puesto);
        System.out.println(" - Sueldo asignado: " + salario);
        System.out.println(" - Fecha de ingreso: " + fechaAlta);
        return new Trabajador(nombre, puesto, salario, fechaAlta);
    }

    public void anyadirEmpleados(Trabajador t) {
        listaTrabajadores.add(t);
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Información");
        alerta.setHeaderText("Operación completada");
        alerta.setContentText(mensaje);
        ButtonType botonAceptar = new ButtonType("Entendido");
        alerta.getButtonTypes().setAll(botonAceptar);
        alerta.setOnCloseRequest(event -> {
            nombreTextField.clear();
            salarioTextField.clear();
            puestoComboBox.setValue(null);
        });
        alerta.showAndWait();
    }

    @FXML
    public void añadirEmpleado() {
        if (nombreTextField.getText().isEmpty() || salarioTextField.getText().isEmpty() || puestoComboBox.getValue() == null) {
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
                        listaTrabajadores.add(trabajador);
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
        trabajadoresListView.getItems().clear();

        if (listaTrabajadores.isEmpty()) {
            LeerTrabajadores();
        }

        for (Trabajador t : listaTrabajadores) {
            trabajadoresListView.getItems().add(t.getNombre());
            System.out.println("📋 Añadido al listado: " + t.getNombre());
        }
    }

    @FXML
    public void initialize() {
        clickrefrescar();
        eliminarTrabajador();
        trabajadoresListView.setOnMouseClicked(event -> {
            eliminarTrabajador();
            int indiceSeleccionado = trabajadoresListView.getSelectionModel().getSelectedIndex();

            if (indiceSeleccionado >= 0) {
                Trabajador trabajadorSeleccionado = listaTrabajadores.get(indiceSeleccionado);
                idLabel.setText(String.valueOf(trabajadorSeleccionado.getId()));
                nombreLabel.setText(trabajadorSeleccionado.getNombre());
                puestoLabel.setText(trabajadorSeleccionado.getPuesto());
                sueldoLabel.setText(String.valueOf(trabajadorSeleccionado.getSalario()));
                LocalDate fechaAlta = trabajadorSeleccionado.getFechaAlta();
                if (fechaAlta != null) {
                    fechaLabel.setText(fechaAlta.toString());
                } else {
                    fechaLabel.setText("No disponible");
                }
            }
        });
    }

    public void eliminarTrabajador() {
        eliminarButton.setOnAction(event -> {
            int indiceSeleccionado = trabajadoresListView.getSelectionModel().getSelectedIndex();

            if (indiceSeleccionado >= 0) {
                Trabajador trabajadorSeleccionado = listaTrabajadores.get(indiceSeleccionado);

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
        int indiceSeleccionado = trabajadoresListView.getSelectionModel().getSelectedIndex();

        if (indiceSeleccionado >= 0) {
            Trabajador trabajadorSeleccionado = listaTrabajadores.get(indiceSeleccionado);
            listaTrabajadores.remove(trabajadorSeleccionado);

            idLabel.setText("");
            nombreLabel.setText("");
            puestoLabel.setText("");
            sueldoLabel.setText("");
            fechaLabel.setText("");

            trabajadoresListView.getItems().remove(indiceSeleccionado);

            System.out.println("🗑 Trabajador eliminado: " + trabajadorSeleccionado.getNombre());
        } else {
            System.out.println("⚠ No se ha seleccionado ningún trabajador para eliminar.");
        }
    }

    @FXML
    private void modificarButton() {
        String nombreSeleccionado = trabajadoresListView.getSelectionModel().getSelectedItem();
        Trabajador trabajadorSeleccionado = null;

        for (Trabajador trabajador : listaTrabajadores) {
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
