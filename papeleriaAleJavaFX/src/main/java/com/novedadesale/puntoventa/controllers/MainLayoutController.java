package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.ui.Animations;
import com.novedadesale.puntoventa.ui.Sesion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    private static final String VIEWS = "/com/novedadesale/puntoventa/views/pages/";

    @FXML private BorderPane root;
    @FXML private VBox sidebar;
    @FXML private StackPane contentPane;
    @FXML private HBox userMenu;
    @FXML private Label userName;
    @FXML private Label userRole;
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
        mostrarUsuario();
        goToInicio();
        Animations.enableMicroInteractions(root);
    }

    private void mostrarUsuario() {
        userName.setText(Sesion.nombre());
        userRole.setText(Sesion.rol() == Rol.ADMIN ? "Administrador" : "Cajero");
    }

    @FXML
    private void goToInicio() {
        loadPage("DashboardPage.fxml");
        setActive(btnInicio);
    }

    @FXML
    private void goToVentas() {
        loadPage("PointOfSalePage.fxml");
        setActive(btnVentas);
    }

    @FXML
    private void goToProductos() {
        loadPage("ProductosPage.fxml");
        setActive(btnProductos);
    }

    @FXML
    private void goToRegalos() {
        loadPage("RegalosPage.fxml");
        setActive(btnRegalos);
    }

    @FXML
    private void goToOfertas() {
        loadPage("OfertasPage.fxml");
        setActive(btnOfertas);
    }

    @FXML
    private void goToInventario() {
        loadPage("InventarioPage.fxml");
        setActive(btnInventario);
    }

    @FXML
    private void goToClientes() {
        loadPage("ClientesPage.fxml");
        setActive(btnClientes);
    }

    @FXML
    private void goToProveedores() {
        loadPage("ProveedoresPage.fxml");
        setActive(btnProveedores);
    }

    @FXML
    private void goToReportes() {
        loadPage("ReportesPage.fxml");
        setActive(btnReportes);
    }

    @FXML
    private void goToConfiguracion() {
        loadPage("ConfiguracionPage.fxml");
        setActive(btnConfiguracion);
    }

    @FXML
    private void toggleUserMenu() {
        Animations.pressPulse(userMenu);
    }

    private void loadPage(String fxmlName) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(VIEWS + fxmlName));
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
