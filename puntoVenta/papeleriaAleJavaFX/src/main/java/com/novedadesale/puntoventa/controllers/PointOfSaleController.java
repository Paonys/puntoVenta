package com.novedadesale.puntoventa.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class PointOfSaleController implements Initializable {

    @FXML private FlowPane productGrid;
    @FXML private VBox cartItemsContainer;
    @FXML private Label lblVentasDia;
    @FXML private Label lblValorInventario;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIVA;
    @FXML private Label lblDescuento;
    @FXML private Label lblTotal;

    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblVentasDia.setText(currency.format(4245.00));
        lblValorInventario.setText(currency.format(3760.00));
        loadMockProducts();
        updateTotals();
    }

    private void loadMockProducts() {
        String[] products = {
                "Cuaderno espiral",
                "Plumas de gel",
                "Peluches",
                "Taza personalizada",
                "Lápices de colores"
        };
        String[] prices = {"$5.00", "$1.50", "$8.00", "$7.00", "$3.50"};

        for (int i = 0; i < products.length; i++) {
            VBox card = createProductCard(products[i], prices[i]);
            productGrid.getChildren().add(card);
        }
    }

    private VBox createProductCard(String name, String price) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(180);
        card.setSpacing(8);

        VBox imgContainer = new VBox();
        imgContainer.getStyleClass().add("product-image");
        imgContainer.setAlignment(Pos.CENTER);

        Label priceLabel = new Label(price);
        priceLabel.getStyleClass().add("product-price");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);

        Label stockLabel = new Label("Stock: 20");
        stockLabel.getStyleClass().add("product-stock");

        Button addBtn = new Button("Agregar");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.getStyleClass().add("button-hover");

        card.getChildren().addAll(imgContainer, priceLabel, nameLabel, stockLabel, addBtn);
        return card;
    }

    @FXML
    private void verTodosProductos() {
        // TODO
    }

    @FXML
    private void cobrar() {
        // TODO
    }

private void updateTotals() {
        lblSubtotal.setText(currency.format(0.0));
        lblIVA.setText(currency.format(0.0));
        lblDescuento.setText(currency.format(0.0));
        lblTotal.setText(currency.format(0.0));
    }
}
