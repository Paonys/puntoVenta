package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.App;
import com.novedadesale.puntoventa.models.Usuario;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.ui.Sesion;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
            App.mostrarApp();
        } catch (RuntimeException e) {
            mostrarError(e.getMessage() == null ? "No se pudo iniciar sesión." : e.getMessage());
        }
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
