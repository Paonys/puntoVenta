package com.novedadesale.puntoventa;

import com.novedadesale.puntoventa.ui.Sesion;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

public class App extends Application {

    private static final String BASE = "/com/novedadesale/puntoventa/";
    private static final String LOGIN = BASE + "views/pages/LoginPage.fxml";
    private static final String MAIN = BASE + "views/templates/MainLayout.fxml";
    private static final String[] HOJAS = {
            "theme", "animations", "atoms", "molecules", "organisms", "templates", "pages", "utilities"
    };

    private static Stage primaryStage;
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        scene = new Scene(cargar(LOGIN));
        aplicarEstilos(scene);

        stage.setTitle("Punto de Venta - Papelería Novedades Ale");
        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream(BASE + "images/logo.png")));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            // Icono opcional si no existe
        }
        stage.setScene(scene);
        stage.setMinWidth(1200);
        stage.setMinHeight(800);
        stage.show();
    }

    /** Abre la pantalla principal tras un inicio de sesión válido. */
    public static void mostrarApp() {
        if (scene != null) {
            scene.setRoot(cargar(MAIN));
        }
    }

    /** Cierra la sesión y regresa al inicio de sesión. */
    public static void mostrarLogin() {
        Sesion.cerrar();
        if (scene != null) {
            scene.setRoot(cargar(LOGIN));
        }
    }

    private static Parent cargar(String rutaFxml) {
        try {
            return new FXMLLoader(App.class.getResource(rutaFxml)).load();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar " + rutaFxml, e);
        }
    }

    private static void aplicarEstilos(Scene scene) {
        for (String hoja : HOJAS) {
            scene.getStylesheets().add(Objects.requireNonNull(
                    App.class.getResource(BASE + "styles/" + hoja + ".css")).toExternalForm());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
