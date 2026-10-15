package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.shared.Formato;
import com.novedadesale.puntoventa.ui.Componentes;
import com.novedadesale.puntoventa.ui.Dialogos;
import com.novedadesale.puntoventa.ui.Sesion;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/** Inventario: consulta de existencias, ajuste de stock y alertas de stock bajo. */
public class InventarioController implements Initializable {

    private enum Filtro { TODOS, BAJO, AGOTADO }

    @FXML private TableView<Producto> tblInventario;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Integer> colMinimo;
    @FXML private TableColumn<Producto, Producto> colEstado;
    @FXML private TableColumn<Producto, Producto> colAcciones;
    @FXML private TextField txtBuscar;
    @FXML private HBox chipsFiltro;
    @FXML private Label lblTotal;
    @FXML private Label lblStockBajo;
    @FXML private Label lblAgotados;
    @FXML private Label lblValor;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private FilteredList<Producto> filtrados;
    private Filtro filtroActivo = Filtro.TODOS;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        filtrados = new FilteredList<>(productos, p -> true);
        tblInventario.setItems(filtrados);
        tblInventario.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblInventario.setPlaceholder(Componentes.placeholder("No se encontraron productos"));

        configurarColumnas();
        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltros());
        crearChips();
        recargar();
    }

    @FXML
    private void recargar() {
        productos.setAll(Servicios.productos().listar());
        aplicarFiltros();
        actualizarMetricas();
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().codigo()));
        colNombre.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nombre()));
        colCategoria.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().categoria()));
        colStock.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().stock()));
        colStock.setCellFactory(c -> Componentes.numberCell());
        colMinimo.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().stockMinimo()));
        colMinimo.setCellFactory(c -> Componentes.numberCell());
        colEstado.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        colEstado.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Producto p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label();
                badge.getStyleClass().add("badge");
                if (p.agotado()) {
                    badge.setText("Agotado");
                    badge.getStyleClass().add("danger");
                } else if (p.stockBajo()) {
                    badge.setText("Stock bajo");
                    badge.getStyleClass().add("warning");
                } else {
                    badge.setText("Disponible");
                    badge.getStyleClass().add("success");
                }
                setGraphic(badge);
            }
        });
        colAcciones.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        colAcciones.setCellFactory(c -> new TableCell<>() {
            private final Button btnReponer = Componentes.iconButton("mdi2p-plus", "Reabastecer (+10)");
            private final Button btnAjustar = Componentes.iconButton("mdi2p-pencil-outline", "Ajustar existencias");
            private final HBox box = new HBox(8, btnReponer, btnAjustar);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                btnReponer.setOnAction(e -> reponer(getItem()));
                btnAjustar.setOnAction(e -> ajustar(getItem()));
            }
            @Override protected void updateItem(Producto p, boolean empty) {
                super.updateItem(p, empty);
                setGraphic(empty || p == null ? null : box);
            }
        });
        colAcciones.setVisible(Sesion.esAdmin());
    }

    private void crearChips() {
        chipsFiltro.getChildren().clear();
        addChip("Todos", Filtro.TODOS);
        addChip("Stock bajo", Filtro.BAJO);
        addChip("Agotados", Filtro.AGOTADO);
    }

    private void addChip(String texto, Filtro filtro) {
        Button chip = new Button(texto);
        chip.getStyleClass().add("chip");
        if (filtro == filtroActivo) {
            chip.getStyleClass().add("active");
        }
        chip.setOnAction(e -> {
            filtroActivo = filtro;
            crearChips();
            aplicarFiltros();
        });
        chipsFiltro.getChildren().add(chip);
    }

    private void aplicarFiltros() {
        String consulta = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
        filtrados.setPredicate(p -> {
            boolean coincideTexto = consulta.isEmpty()
                    || p.nombre().toLowerCase(Locale.ROOT).contains(consulta)
                    || p.codigo().toLowerCase(Locale.ROOT).contains(consulta);
            boolean coincideFiltro = switch (filtroActivo) {
                case TODOS -> true;
                case BAJO -> p.stockBajo();
                case AGOTADO -> p.agotado();
            };
            return coincideTexto && coincideFiltro;
        });
    }

    private void actualizarMetricas() {
        lblTotal.setText(String.valueOf(productos.size()));
        lblStockBajo.setText(String.valueOf(productos.stream().filter(Producto::stockBajo).count()));
        lblAgotados.setText(String.valueOf(productos.stream().filter(Producto::agotado).count()));
        lblValor.setText(Formato.money(productos.stream()
                .map(p -> p.precio().multiply(BigDecimal.valueOf(p.stock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    private void reponer(Producto p) {
        if (p == null) {
            return;
        }
        try {
            Servicios.productos().ajustarStock(p.id(), 10);
            recargar();
        } catch (RuntimeException e) {
            Dialogos.error(e.getMessage());
        }
    }

    private void ajustar(Producto producto) {
        if (producto == null) {
            return;
        }
        Dialog<Integer> dialogo = new Dialog<>();
        dialogo.setTitle("Ajustar existencias");
        dialogo.setResizable(false);
        DialogPane panel = dialogo.getDialogPane();
        panel.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Dialogos.aplicarEstilos(panel);

        Label titulo = new Label(producto.nombre());
        titulo.getStyleClass().add("label-heading");
        TextField cantidad = new TextField(String.valueOf(producto.stock()));

        VBox contenido = new VBox(10,
                titulo,
                Componentes.etiqueta("Stock actual: " + producto.stock()),
                Componentes.etiqueta("Nueva cantidad"),
                cantidad);
        contenido.setPadding(new Insets(8, 4, 4, 4));
        panel.setContent(contenido);

        Button btnOk = (Button) panel.lookupButton(ButtonType.OK);
        btnOk.setText("Guardar");
        btnOk.getStyleClass().add("btn-primary");
        Button btnCancel = (Button) panel.lookupButton(ButtonType.CANCEL);
        btnCancel.setText("Cancelar");
        btnCancel.getStyleClass().add("btn-secondary");

        dialogo.setResultConverter(boton -> boton == ButtonType.OK
                ? (int) Formato.decimal(cantidad.getText()).doubleValue() : null);

        dialogo.showAndWait().ifPresent(nueva -> {
            try {
                int delta = nueva - producto.stock();
                if (delta == 0) {
                    return;
                }
                Servicios.productos().ajustarStock(producto.id(), delta);
                recargar();
            } catch (RuntimeException e) {
                Dialogos.error(e.getMessage());
            }
        });
    }
}
