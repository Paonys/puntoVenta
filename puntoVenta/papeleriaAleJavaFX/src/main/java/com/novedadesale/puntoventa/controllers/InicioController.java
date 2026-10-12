package com.novedadesale.puntoventa.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.net.URL;
import java.util.ResourceBundle;

public class InicioController implements Initializable {

    @FXML private FlowPane productGrid;
    @FXML private TextField txtBuscar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarProductosMock();
    }

    private void cargarProductosMock() {
        // Mock basado en la vista original Inicio (Cuaderno, Plumas, Peluches...)
        String[] nombres = {"Cuaderno espiral", "Plumas de gel", "Peluches", "Lápices de colores", "Taza personalizada"};
        String[] precios = {"$5.00", "$1.50", "$8.00", "$3.50", "$7.00"};
        int[] stocks = {50, 120, 18, 80, 15};

        for (int i = 0; i < nombres.length; i++) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(
                        "/com/novedadesale/puntoventa/views/molecules/ProductCard.fxml"));
                javafx.scene.layout.VBox card = loader.load();
                com.novedadesale.puntoventa.controllers.molecules.ProductCardController ctrl =
                        loader.getController();
                ctrl.setData(nombres[i], precios[i], stocks[i]);
                productGrid.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
