package com.novedadesale.puntoventa.controllers.molecules;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ProductCardController {
    @FXML private VBox imgContainer;
    @FXML private Label lblPrice;
    @FXML private Label lblName;
    @FXML private Label lblStock;
    @FXML private Button btnAdd;

    private Runnable onAddAction;

    public void setData(String name, String price, int stock) {
        lblName.setText(name);
        lblPrice.setText(price);
        lblStock.setText("Stock: " + stock);
    }

    public void setOnAddAction(Runnable action) {
        this.onAddAction = action;
    }

    @FXML
    private void onAddToCart() {
        if (onAddAction != null) onAddAction.run();
    }
}
