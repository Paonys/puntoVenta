package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.App;
import com.novedadesale.puntoventa.models.Usuario;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.ui.Dialogos;
import com.novedadesale.puntoventa.ui.Sesion;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/** Inicio de sesión (B4): autentica contra {@link Servicios#usuarios()} y abre la sesión. */
public class LoginController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnIngresar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ocultarError();
        Platform.runLater(txtUsuario::requestFocus);
    }

    @FXML
    private void onIngresar() {
        String usuario = txtUsuario.getText() == null ? "" : txtUsuario.getText().trim();
        String password = txtPassword.getText() == null ? "" : txtPassword.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarError("Ingresa tu usuario y contraseña.");
            return;
        }

        try {
            Optional<Usuario> autenticado = Servicios.usuarios().autenticar(usuario, password);
            if (autenticado.isEmpty()) {
                mostrarError("Usuario o contraseña incorrectos.");
                txtPassword.clear();
                txtPassword.requestFocus();
                return;
            }
            Sesion.iniciar(autenticado.get());
            if (autenticado.get().debeCambiarPassword() && !pedirCambioPassword()) {
                Sesion.cerrar();
                txtPassword.clear();
                txtPassword.requestFocus();
                return;
            }
            App.mostrarApp();
        } catch (RuntimeException e) {
            mostrarError(e.getMessage() == null ? "No se pudo iniciar sesión." : e.getMessage());
        }
    }

    /**
     * S9: diálogo modal obligatorio para cuentas con contraseña inicial.
     * Devuelve {@code false} si se cancela (se regresa al login sin sesión).
     */
    private boolean pedirCambioPassword() {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Cambio obligatorio de contraseña");
        dialogo.setHeaderText("Debes cambiar tu contraseña antes de entrar al sistema.");

        ButtonType cambiar = new ButtonType("Continuar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(cambiar, cancelar);

        PasswordField actual = new PasswordField();
        PasswordField nueva = new PasswordField();
        PasswordField confirmacion = new PasswordField();
        Label error = new Label();
        error.getStyleClass().add("login-error");
        error.setManaged(false);
        error.setVisible(false);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8, 4, 4, 4));
        grid.add(new Label("Contraseña actual"), 0, 0);
        grid.add(actual, 1, 0);
        grid.add(new Label("Contraseña nueva"), 0, 1);
        grid.add(nueva, 1, 1);
        grid.add(new Label("Confirmar contraseña"), 0, 2);
        grid.add(confirmacion, 1, 2);
        grid.add(error, 1, 3);
        dialogo.getDialogPane().setContent(grid);

        Node btnContinuar = dialogo.getDialogPane().lookupButton(cambiar);
        ((Button) btnContinuar).getStyleClass().add("btn-primary");
        ((Button) dialogo.getDialogPane().lookupButton(cancelar)).getStyleClass().add("btn-secondary");
        btnContinuar.addEventFilter(ActionEvent.ACTION, evento -> {
            try {
                if (nueva.getText() == null || !nueva.getText().equals(confirmacion.getText())) {
                    throw new com.novedadesale.puntoventa.exceptions.ValidacionException(
                            "La confirmación no coincide con la contraseña nueva");
                }
                Servicios.usuarios().cambiarPasswordPropia(actual.getText(), nueva.getText());
            } catch (RuntimeException e) {
                error.setText(e.getMessage() == null ? "No se pudo cambiar la contraseña." : e.getMessage());
                error.setManaged(true);
                error.setVisible(true);
                evento.consume();
            }
        });

        Platform.runLater(actual::requestFocus);
        Optional<ButtonType> resultado = Dialogos.mostrar(dialogo);
        return resultado.isPresent() && resultado.get() == cambiar;
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setManaged(true);
        lblError.setVisible(true);
    }

    private void ocultarError() {
        lblError.setManaged(false);
        lblError.setVisible(false);
    }
}
