package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.models.Proveedor;
import com.novedadesale.puntoventa.services.Servicios;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/** Administración de proveedores (CRUD). */
public class ProveedoresController implements Initializable {

    @FXML private TableView<Proveedor> tblProveedores;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colContacto;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colEmail;
    @FXML private TableColumn<Proveedor, Proveedor> colAcciones;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;
    @FXML private Label lblContacto;
    @FXML private Label lblCorreo;
    @FXML private Button btnNuevo;

    private final ObservableList<Proveedor> proveedores = FXCollections.observableArrayList();
    private FilteredList<Proveedor> filtrados;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        filtrados = new FilteredList<>(proveedores, p -> true);
        tblProveedores.setItems(filtrados);
        tblProveedores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblProveedores.setPlaceholder(Componentes.placeholder("No se encontraron proveedores"));

        configurarColumnas();
        Componentes.soloAdmin(btnNuevo);
        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltro());
        recargar();
    }

    private void recargar() {
        proveedores.setAll(Servicios.proveedores().listar());
        aplicarFiltro();
        actualizarMetricas();
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nombre()));
        colContacto.setCellValueFactory(cd -> new ReadOnlyStringWrapper(Componentes.nulo(cd.getValue().contacto())));
        colTelefono.setCellValueFactory(cd -> new ReadOnlyStringWrapper(Componentes.nulo(cd.getValue().telefono())));
        colEmail.setCellValueFactory(cd -> new ReadOnlyStringWrapper(Componentes.nulo(cd.getValue().email())));

        colAcciones.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        colAcciones.setCellFactory(c -> new TableCell<>() {
            private final Button btnEditar = Componentes.iconButton("mdi2p-pencil-outline", "Editar");
            private final Button btnEliminar = Componentes.iconButton("mdi2t-trash-can-outline", "Eliminar");
            private final HBox box = new HBox(8, btnEditar, btnEliminar);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                btnEliminar.getStyleClass().add("danger");
                btnEditar.setOnAction(e -> editar(getItem()));
                btnEliminar.setOnAction(e -> eliminar(getItem()));
            }
            @Override protected void updateItem(Proveedor p, boolean empty) {
                super.updateItem(p, empty);
                setGraphic(empty || p == null ? null : box);
            }
        });
        colAcciones.setVisible(Sesion.esAdmin());
    }

    private void aplicarFiltro() {
        String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
        filtrados.setPredicate(p -> q.isEmpty()
                || p.nombre().toLowerCase(Locale.ROOT).contains(q)
                || Componentes.nulo(p.contacto()).toLowerCase(Locale.ROOT).contains(q)
                || Componentes.nulo(p.email()).toLowerCase(Locale.ROOT).contains(q));
    }

    private void actualizarMetricas() {
        lblTotal.setText(String.valueOf(proveedores.size()));
        lblContacto.setText(String.valueOf(proveedores.stream()
                .filter(p -> p.contacto() != null && !p.contacto().isBlank()).count()));
        lblCorreo.setText(String.valueOf(proveedores.stream()
                .filter(p -> p.email() != null && !p.email().isBlank()).count()));
    }

    @FXML
    private void onNuevoProveedor() {
        editar(null);
    }

    private void editar(Proveedor existente) {
        boolean esNuevo = existente == null;
        Dialog<Proveedor> dialogo = new Dialog<>();
        dialogo.setTitle(esNuevo ? "Nuevo proveedor" : "Editar proveedor");
        dialogo.setResizable(false);
        DialogPane panel = dialogo.getDialogPane();
        panel.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Dialogos.aplicarEstilos(panel);

        TextField nombre = new TextField(esNuevo ? "" : existente.nombre());
        TextField contacto = new TextField(esNuevo ? "" : Componentes.nulo(existente.contacto()));
        TextField telefono = new TextField(esNuevo ? "" : Componentes.nulo(existente.telefono()));
        TextField email = new TextField(esNuevo ? "" : Componentes.nulo(existente.email()));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8, 4, 4, 4));
        grid.add(Componentes.etiqueta("Nombre"), 0, 0);
        grid.add(nombre, 1, 0);
        grid.add(Componentes.etiqueta("Contacto"), 0, 1);
        grid.add(contacto, 1, 1);
        grid.add(Componentes.etiqueta("Teléfono"), 0, 2);
        grid.add(telefono, 1, 2);
        grid.add(Componentes.etiqueta("Correo"), 0, 3);
        grid.add(email, 1, 3);
        panel.setContent(grid);

        Button btnOk = (Button) panel.lookupButton(ButtonType.OK);
        btnOk.setText("Guardar");
        btnOk.getStyleClass().add("btn-primary");
        Button btnCancel = (Button) panel.lookupButton(ButtonType.CANCEL);
        btnCancel.setText("Cancelar");
        btnCancel.getStyleClass().add("btn-secondary");

        dialogo.setResultConverter(boton -> boton == ButtonType.OK
                ? new Proveedor(esNuevo ? 0 : existente.id(),
                        Componentes.texto(nombre), Componentes.texto(contacto),
                        Componentes.texto(telefono), Componentes.texto(email))
                : null);

        dialogo.showAndWait().ifPresent(proveedor -> {
            try {
                Servicios.proveedores().guardar(proveedor);
                recargar();
            } catch (RuntimeException e) {
                Dialogos.error(e.getMessage());
            }
        });
    }

    private void eliminar(Proveedor proveedor) {
        if (proveedor == null) {
            return;
        }
        if (Dialogos.confirmar("¿Eliminar al proveedor \"" + proveedor.nombre() + "\"?")) {
            try {
                Servicios.proveedores().eliminar(proveedor.id());
                recargar();
            } catch (RuntimeException e) {
                Dialogos.error(e.getMessage());
            }
        }
    }
}
