package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.ui.Animations;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {

    @FXML private BorderPane root;
    @FXML private VBox sidebar;
    @FXML private StackPane contentPane;
    @FXML private HBox userMenu;
    @FXML private Button btnInicio;
    @FXML private Button btnVentas;
    @FXML private Button btnProductos;
    @FXML private Button btnRegalos;
    @FXML private Button btnOfertas;
    @FXML private Button btnInventario;
    @FXML private Button btnClientes;
    @FXML private Button btnProveedores;
    @FXML private Button btnReportes;
    @FXML private Button btnConfiguracion;
    @FXML private TextField searchField;

    private final List<Button> navButtons = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (Node child : sidebar.getChildren()) {
            if (child instanceof Button button && button.getStyleClass().contains("nav-item")) {
                navButtons.add(button);
            }
        }
        goToInicio();
        Animations.enableMicroInteractions(root);
    }

    @FXML
    private void goToInicio() {
        loadPage("/com/novedadesale/puntoventa/views/pages/PointOfSalePage.fxml");
        setActive(btnInicio);
    }

    @FXML
    private void goToVentas() {
        loadPage("/com/novedadesale/puntoventa/views/pages/PointOfSalePage.fxml");
        setActive(btnVentas);
    }

    @FXML
    private void goToProductos() {
        loadPage("/com/novedadesale/puntoventa/views/pages/ProductosPage.fxml");
        setActive(btnProductos);
    }

    @FXML
    private void goToRegalos() {
        loadPage("/com/novedadesale/puntoventa/views/pages/PointOfSalePage.fxml");
        setActive(btnRegalos);
    }

    @FXML
    private void goToOfertas() {
        loadPage("/com/novedadesale/puntoventa/views/pages/PointOfSalePage.fxml");
        setActive(btnOfertas);
    }

    @FXML
    private void goToInventario() {
        loadPage("/com/novedadesale/puntoventa/views/pages/InventarioPage.fxml");
        setActive(btnInventario);
    }

    @FXML
    private void goToClientes() {
        loadPage("/com/novedadesale/puntoventa/views/pages/ClientesPage.fxml");
        setActive(btnClientes);
    }

    @FXML
    private void goToProveedores() {
        loadPage("/com/novedadesale/puntoventa/views/pages/PointOfSalePage.fxml");
        setActive(btnProveedores);
    }

    @FXML
    private void goToReportes() {
        loadPage("/com/novedadesale/puntoventa/views/pages/ReportesPage.fxml");
        setActive(btnReportes);
    }

    @FXML
    private void goToConfiguracion() {
        loadPage("/com/novedadesale/puntoventa/views/pages/PointOfSalePage.fxml");
        setActive(btnConfiguracion);
    }

    @FXML
    private void toggleUserMenu() {
        Animations.pressPulse(userMenu);
    }

    private void loadPage(String fxmlPath) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().clear();
            contentPane.getChildren().add(page);
            Animations.viewEntrance(page);
            Animations.enableMicroInteractions(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActive(Button activeBtn) {
        for (Button button : navButtons) {
            boolean isActive = button == activeBtn;
            if (isActive && !button.getStyleClass().contains("active")) {
                button.getStyleClass().add("active");
            } else if (!isActive) {
                button.getStyleClass().remove("active");
            }
        }
    }
}