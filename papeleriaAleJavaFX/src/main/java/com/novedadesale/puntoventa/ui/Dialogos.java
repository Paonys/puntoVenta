package com.novedadesale.puntoventa.ui;

import com.novedadesale.puntoventa.App;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

/** Utilidades para mostrar alertas y aplicar el tema a los diálogos. */
public final class Dialogos {

    private Dialogos() {
    }

    public static void aplicarEstilos(DialogPane panel) {
        Stage owner = App.getPrimaryStage();
        if (owner != null && owner.getScene() != null) {
            panel.getStylesheets().addAll(owner.getScene().getStylesheets());
        }
    }

    public static void error(String mensaje) {
        mostrar(Alert.AlertType.ERROR, "Error", mensaje);
    }

    public static void aviso(String mensaje) {
        mostrar(Alert.AlertType.WARNING, "Aviso", mensaje);
    }

    public static void info(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Información", mensaje);
    }

    public static boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.OK, ButtonType.CANCEL);
        alerta.setTitle("Confirmar");
        alerta.setHeaderText(null);
        aplicarEstilos(alerta.getDialogPane());
        return alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        aplicarEstilos(alerta.getDialogPane());
        alerta.showAndWait();
    }
}
