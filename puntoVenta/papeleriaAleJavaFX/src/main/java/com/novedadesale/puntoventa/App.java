package com.novedadesale.puntoventa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novedadesale/puntoventa/views/templates/MainLayout.fxml"));
        BorderPane root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/novedadesale/puntoventa/styles/theme.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/novedadesale/puntoventa/styles/animations.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/novedadesale/puntoventa/styles/atoms.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/novedadesale/puntoventa/styles/molecules.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/novedadesale/puntoventa/styles/organisms.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/novedadesale/puntoventa/styles/templates.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/novedadesale/puntoventa/styles/pages.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/novedadesale/puntoventa/styles/utilities.css")).toExternalForm());

        stage.setTitle("Punto de Venta - Papelería Novedades Ale");
        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/novedadesale/puntoventa/images/logo.png")));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            // Icono opcional si no existe
        }
        stage.setScene(scene);
        stage.setMinWidth(1200);
        stage.setMinHeight(800);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
