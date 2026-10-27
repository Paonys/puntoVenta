package com.novedadesale.puntoventa.ui;

import com.novedadesale.puntoventa.App;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import java.util.Optional;

/** Utilidades para mostrar alertas y aplicar el tema a los diálogos. */
public final class Dialogos {

    /** Diálogos en pantalla; el cierre por inactividad se pausa mientras haya uno (S12). */
    private static int abiertos;

    private Dialogos() {
    }

    /** Hay algún diálogo modal abierto (para pausar el temporizador de inactividad). */
    public static boolean abierto() {
        return abiertos > 0;
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
        return mostrar(alerta).orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /** Muestra un diálogo propio, contabilizándolo para el cierre por inactividad. */
    public static <T> Optional<T> mostrar(Dialog<T> dialogo) {
        abiertos++;
        try {
            aplicarEstilos(dialogo.getDialogPane());
            return dialogo.showAndWait();
        } finally {
            abiertos--;
        }
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        mostrar(alerta);
    }
}
