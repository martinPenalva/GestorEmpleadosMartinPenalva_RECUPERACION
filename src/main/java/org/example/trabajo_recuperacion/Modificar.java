package org.example.trabajo_recuperacion;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

public class Modificar {

    @FXML
    private TextField nombreTextField;
    @FXML
    private TextField puestoTextField;
    @FXML
    private TextField salarioTextField;
    @FXML
    private Button cancelarButton;
    @FXML
    private Button editarButton;

    private Trabajador trabajadorSeleccionado;
    private Stage stage;

    // Inicializa la ventana con los detalles del trabajador a modificar
    public void init(Trabajador trabajador, Stage stage) {
        this.trabajadorSeleccionado = trabajador;
        this.stage = stage;

        // Se muestran los datos actuales del trabajador
        nombreTextField.setText(trabajador.getNombre());
        puestoTextField.setText(trabajador.getPuesto());
        salarioTextField.setText(String.valueOf(trabajador.getSalario()));
    }

    @FXML
    private void cancelar() {
        stage.close();
    }

    @FXML
    private void editar() {
        if (trabajadorSeleccionado != null) {
            try {
                // Validar que los campos no estén vacíos
                if (nombreTextField.getText().isEmpty() || puestoTextField.getText().isEmpty() || salarioTextField.getText().isEmpty()) {
                    mostrarAlerta("Error", "Todos los campos son obligatorios", AlertType.ERROR);
                    return;
                }

                // Obtener los nuevos valores
                String nuevoNombre = nombreTextField.getText().trim();
                String nuevoPuesto = puestoTextField.getText().trim();
                int nuevoSalario;

                try {
                    nuevoSalario = Integer.parseInt(salarioTextField.getText().trim());
                    if (nuevoSalario <= 0) {
                        mostrarAlerta("Error", "El salario debe ser un número positivo", AlertType.ERROR);
                        return;
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "El salario debe ser un número válido", AlertType.ERROR);
                    return;
                }

                // Actualizar el objeto trabajador
                trabajadorSeleccionado.setNombre(nuevoNombre);
                trabajadorSeleccionado.setPuesto(nuevoPuesto);
                trabajadorSeleccionado.setSalario(nuevoSalario);

                // Actualizar en la base de datos
                MYSQL.actualizar(trabajadorSeleccionado);

                // Mostrar mensaje de éxito
                mostrarAlerta("Éxito", "Trabajador actualizado correctamente", AlertType.INFORMATION);

                // Cerrar la ventana
                stage.close();
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al actualizar el trabajador: " + e.getMessage(), AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Error", "No se ha seleccionado ningún trabajador para modificar", AlertType.ERROR);
        }
    }

    @FXML
    private void modificarNombre() {
        String nuevoNombre = obtenerNuevoNombre();
        nombreTextField.setText(nuevoNombre);
    }

    private String obtenerNuevoNombre() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cambiar nombre");
        dialog.setHeaderText("Introduce el nuevo nombre:");
        dialog.setContentText("Nombre:");
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(""); // Devuelve el nombre ingresado o vacío si se cancela
    }

    @FXML
    private void modificarSalario() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Modificar salario");
        dialog.setHeaderText("Introduce el nuevo salario:");
        dialog.setContentText("Salario:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nuevoSalario -> salarioTextField.setText(nuevoSalario));
    }

    @FXML
    private void cambiarPuesto() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Puesto actual", "Puesto 1", "Puesto 2", "Puesto 3");
        dialog.setTitle("Modificar puesto");
        dialog.setHeaderText("Selecciona el nuevo puesto:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nuevoPuesto -> puestoTextField.setText(nuevoPuesto));
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
