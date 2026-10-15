package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.models.Cliente;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.shared.Formato;
import com.novedadesale.puntoventa.ui.Componentes;
import com.novedadesale.puntoventa.ui.Dialogos;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/** Cartera de clientes: CRUD y descuentos de cliente frecuente. */
public class ClientesController implements Initializable {

    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, BigDecimal> colDescuento;
    @FXML private TableColumn<Cliente, Cliente> colAcciones;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;
    @FXML private Label lblConDescuento;
    @FXML private Label lblPromedio;

    private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private FilteredList<Cliente> filtrados;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        filtrados = new FilteredList<>(clientes, c -> true);
        tblClientes.setItems(filtrados);
        tblClientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        Label placeholder = new Label("No se encontraron clientes");
        placeholder.getStyleClass().add("text-tertiary");
        tblClientes.setPlaceholder(placeholder);

        configurarColumnas();
        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltro());
        recargar();
    }

    private void recargar() {
        clientes.setAll(Servicios.clientes().listar());
        aplicarFiltro();
        actualizarMetricas();
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nombre()));
        colTelefono.setCellValueFactory(cd -> new ReadOnlyStringWrapper(Componentes.nulo(cd.getValue().telefono())));
        colEmail.setCellValueFactory(cd -> new ReadOnlyStringWrapper(Componentes.nulo(cd.getValue().email())));
        colDescuento.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().descuento()));
        colDescuento.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : valor.stripTrailingZeros().toPlainString() + "%");
            }
        });
        colAcciones.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        colAcciones.setCellFactory(c -> new TableCell<>() {
            private final Button btnEditar = Componentes.iconButton("mdi2p-pencil-outline", "Editar");
            private final Button btnEliminar = Componentes.iconButton("mdi2t-trash-can-outline", "Eliminar");
            private final HBox box = new HBox(8, btnEditar, btnEliminar);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                btnEliminar.getStyleClass().add("danger");
                Componentes.soloAdmin(btnEliminar);
                btnEditar.setOnAction(e -> editar(getItem()));
                btnEliminar.setOnAction(e -> eliminar(getItem()));
            }
            @Override protected void updateItem(Cliente c, boolean empty) {
                super.updateItem(c, empty);
                setGraphic(empty || c == null ? null : box);
            }
        });
    }

    private void aplicarFiltro() {
        String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
        filtrados.setPredicate(c -> q.isEmpty()
                || c.nombre().toLowerCase(Locale.ROOT).contains(q)
                || Componentes.nulo(c.telefono()).toLowerCase(Locale.ROOT).contains(q)
                || Componentes.nulo(c.email()).toLowerCase(Locale.ROOT).contains(q));
    }

    private void actualizarMetricas() {
        long conDescuento = clientes.stream()
                .filter(c -> c.descuento() != null && c.descuento().signum() > 0).count();
        BigDecimal promedio = clientes.isEmpty() ? BigDecimal.ZERO
                : clientes.stream().map(c -> c.descuento() == null ? BigDecimal.ZERO : c.descuento())
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(clientes.size()), 1, RoundingMode.HALF_UP);
        lblTotal.setText(String.valueOf(clientes.size()));
        lblConDescuento.setText(String.valueOf(conDescuento));
        lblPromedio.setText(promedio.stripTrailingZeros().toPlainString() + "%");
    }

    @FXML
    private void onNuevoCliente() {
        editar(null);
    }

    private void editar(Cliente existente) {
        boolean esNuevo = existente == null;
        Dialog<Cliente> dialogo = new Dialog<>();
        dialogo.setTitle(esNuevo ? "Nuevo cliente" : "Editar cliente");
        dialogo.setResizable(false);
        DialogPane panel = dialogo.getDialogPane();
        panel.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Dialogos.aplicarEstilos(panel);

        TextField nombre = new TextField(esNuevo ? "" : existente.nombre());
        TextField telefono = new TextField(esNuevo ? "" : Componentes.nulo(existente.telefono()));
        TextField email = new TextField(esNuevo ? "" : Componentes.nulo(existente.email()));
        TextField descuento = new TextField(esNuevo ? "0"
                : existente.descuento().stripTrailingZeros().toPlainString());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8, 4, 4, 4));
        grid.add(Componentes.etiqueta("Nombre"), 0, 0);
        grid.add(nombre, 1, 0);
        grid.add(Componentes.etiqueta("Teléfono"), 0, 1);
        grid.add(telefono, 1, 1);
        grid.add(Componentes.etiqueta("Correo"), 0, 2);
        grid.add(email, 1, 2);
        grid.add(Componentes.etiqueta("Descuento (%)"), 0, 3);
        grid.add(descuento, 1, 3);
        panel.setContent(grid);

        Button btnOk = (Button) panel.lookupButton(ButtonType.OK);
        btnOk.setText("Guardar");
        btnOk.getStyleClass().add("btn-primary");
        Button btnCancel = (Button) panel.lookupButton(ButtonType.CANCEL);
        btnCancel.setText("Cancelar");
        btnCancel.getStyleClass().add("btn-secondary");

        dialogo.setResultConverter(boton -> {
            if (boton != ButtonType.OK) {
                return null;
            }
            return new Cliente(esNuevo ? 0 : existente.id(),
                    Componentes.texto(nombre), Componentes.texto(telefono), Componentes.texto(email),
                    Formato.decimal(descuento.getText()));
        });

        dialogo.showAndWait().ifPresent(cliente -> {
            try {
                Servicios.clientes().guardar(cliente);
                recargar();
            } catch (RuntimeException e) {
                Dialogos.error(e.getMessage());
            }
        });
    }

    private void eliminar(Cliente cliente) {
        if (cliente == null) {
            return;
        }
        if (Dialogos.confirmar("¿Eliminar al cliente \"" + cliente.nombre() + "\"?")) {
            try {
                Servicios.clientes().eliminar(cliente.id());
                recargar();
            } catch (RuntimeException e) {
                Dialogos.error(e.getMessage());
            }
        }
    }
}
