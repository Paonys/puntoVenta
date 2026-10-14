package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.shared.Formato;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/** Sección de Regalos: catálogo de solo lectura con las existencias del ramo. */
public class RegalosController implements Initializable {

    private static final String CATEGORIA = "Regalos";

    @FXML private FlowPane productGrid;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;
    @FXML private Label lblValor;
    @FXML private Label lblStockBajo;

    private List<Producto> regalos = List.of();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        txtBuscar.textProperty().addListener((o, a, b) -> render(b));
        recargar();
    }

    private void recargar() {
        regalos = Servicios.productos().listar().stream()
                .filter(p -> p.categoria() != null && p.categoria().equalsIgnoreCase(CATEGORIA))
                .toList();
        render(txtBuscar.getText());
        actualizarMetricas();
    }

    private void render(String texto) {
        productGrid.getChildren().clear();
        String q = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        List<Producto> visibles = regalos.stream()
                .filter(p -> q.isEmpty()
                        || p.nombre().toLowerCase(Locale.ROOT).contains(q)
                        || p.codigo().toLowerCase(Locale.ROOT).contains(q))
                .toList();
        if (visibles.isEmpty()) {
            Label vacio = new Label("No se encontraron regalos");
            vacio.getStyleClass().add("text-tertiary");
            productGrid.getChildren().add(vacio);
            return;
        }
        visibles.forEach(p -> productGrid.getChildren().add(tarjeta(p)));
    }

    private VBox tarjeta(Producto producto) {
        VBox card = new VBox(8);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(180);

        VBox imagen = new VBox();
        imagen.getStyleClass().add("product-image");
        imagen.setAlignment(Pos.CENTER);
        FontIcon icono = new FontIcon("mdi2g-gift-outline");
        icono.setIconSize(30);
        imagen.getChildren().add(icono);

        Label precio = new Label(Formato.money(producto.precio()));
        precio.getStyleClass().add("product-name");

        Label nombre = new Label(producto.nombre());
        nombre.getStyleClass().add("product-name");
        nombre.setWrapText(true);

        Label stock = new Label("Stock: " + producto.stock());
        stock.getStyleClass().add("product-stock");

        Label badge = new Label();
        badge.getStyleClass().add("badge");
        if (producto.agotado()) {
            badge.setText("Agotado");
            badge.getStyleClass().add("danger");
        } else if (producto.stockBajo()) {
            badge.setText("Stock bajo");
            badge.getStyleClass().add("warning");
        } else {
            badge.setText("Disponible");
            badge.getStyleClass().add("success");
        }

        card.getChildren().addAll(imagen, precio, nombre, stock, badge);
        return card;
    }

    private void actualizarMetricas() {
        BigDecimal valor = regalos.stream()
                .map(p -> p.precio().multiply(BigDecimal.valueOf(p.stock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotal.setText(String.valueOf(regalos.size()));
        lblValor.setText(Formato.money(valor));
        lblStockBajo.setText(String.valueOf(regalos.stream().filter(Producto::stockBajo).count()));
    }
}
