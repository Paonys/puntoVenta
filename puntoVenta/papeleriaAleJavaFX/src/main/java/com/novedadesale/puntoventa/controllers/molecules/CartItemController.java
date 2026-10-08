package com.novedadesale.puntoventa.controllers.molecules;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

public class CartItemController {
    @FXML private Label lblName;
    @FXML private Label lblPrice;
    @FXML private Label lblQty;
    @FXML private Label lblSubtotal;
    @FXML private Button btnMinus;
    @FXML private Button btnPlus;
    @FXML private Button btnRemove;

    private int quantity = 1;
    private double unitPrice = 0.0;
    private Consumer<Integer> onQuantityChanged;
    private Runnable onRemove;

    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    public void setData(String name, double unitPrice, int qty) {
        this.unitPrice = unitPrice;
        this.quantity = qty;
        lblName.setText(name);
        lblPrice.setText(currency.format(unitPrice));
        update();
    }

    public void setOnQuantityChanged(Consumer<Integer> c) { this.onQuantityChanged = c; }
    public void setOnRemove(Runnable r) { this.onRemove = r; }

    @FXML void decrease() {
        if (quantity > 1) {
            quantity--;
            update();
            if (onQuantityChanged != null) onQuantityChanged.accept(quantity);
        }
    }
    @FXML void increase() {
        quantity++;
        update();
        if (onQuantityChanged != null) onQuantityChanged.accept(quantity);
    }
    @FXML void remove() {
        if (onRemove != null) onRemove.run();
    }

    private void update() {
        lblQty.setText(String.valueOf(quantity));
        lblSubtotal.setText(currency.format(unitPrice * quantity));
    }
}
