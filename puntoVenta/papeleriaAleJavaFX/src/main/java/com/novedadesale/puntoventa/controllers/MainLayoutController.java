package com.novedadesale.puntoventa.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {

    @FXML private StackPane contentPane;
    @FXML private Button btnInicio;
    @FXML private TextField searchField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        goToInicio();
    }

    @FXML
    private void goToInicio() {
        loadPage("/com/novedadesale/puntoventa/views/pages/PointOfSalePage.fxml");
        setActive(btnInicio);
    }

    @FXML
    private void goToVentas() {
        loadPage("/com/novedadesale/puntoventa/views/pages/PointOfSalePage.fxml");
    }

    @FXML
    private void goToProductos() {
        loadPage("/com/novedadesale/puntoventa/views/pages/PointOfSalePage.fxml");
    }

    @FXML
    private void goToRegalos() {
        loadPage("/com/novedadesale/puntoventa/views/pages/PointOfSalePage.fxml");
    }

    @FXML
    private void goToOfertas() {
        loadPage("/com/novedadesale/puntoventa/views/pages/PointOfSalePage.fxml");
    }

    @FXML
    private void goToInventario() {
        loadPage("/com/novedadesale/puntoventa/views/pages/InventarioPage.fxml");
    }

    @FXML
    private void goToClientes() {
        loadPage("/com/novedadesale/puntoventa/views/pages/ClientesPage.fxml");
    }

    @FXML
    private void goToProveedores() {
        loadPage("/com/novedadesale/puntoventa/views/pages/PointOfSalePage.fxml");
    }

    @FXML
    private void goToReportes() {
        loadPage("/com/novedadesale/puntoventa/views/pages/ReportesPage.fxml");
    }

    @FXML
    private void goToConfiguracion() {
        loadPage("/com/novedadesale/puntoventa/views/pages/PointOfSalePage.fxml");
    }

    @FXML
    private void toggleUserMenu() {
        // TODO: Implementar men?? desplegable de usuario
    }

    private void loadPage(String fxmlPath) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().clear();
            contentPane.getChildren().add(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActive(Button activeBtn) {
        // Resetear estilos de navegaci??n (simplificado)
    }
}
