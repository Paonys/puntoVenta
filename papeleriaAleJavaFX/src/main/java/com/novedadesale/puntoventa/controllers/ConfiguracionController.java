package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.models.Usuario;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/** Configuración: gestión de usuarios y credenciales. */
public class ConfiguracionController implements Initializable {

    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, Usuario> colEstado;
    @FXML private TableColumn<Usuario, Usuario> colAcciones;
    @FXML private TextField txtBuscar;
    @FXML private Button btnNuevoUsuario;
    @FXML private Label lblTotal;
    @FXML private Label lblAdmins;
    @FXML private Label lblActivos;

    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private FilteredList<Usuario> filtrados;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        filtrados = new FilteredList<>(usuarios, u -> true);
        tblUsuarios.setItems(filtrados);
        tblUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblUsuarios.setPlaceholder(Componentes.placeholder("No se encontraron usuarios"));

        configurarColumnas();
        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltro());
        aplicarPermisos();
        recargar();
    }

    private void aplicarPermisos() {
        btnNuevoUsuario.setDisable(!Sesion.esAdmin());
    }

    private void recargar() {
        usuarios.setAll(Servicios.usuarios().listar());
        aplicarFiltro();
        actualizarMetricas();
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nombre()));
        colUsuario.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().usuario()));
        colRol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(rol(cd.getValue().rol())));
        colEstado.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        colEstado.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Usuario u, boolean empty) {
                super.updateItem(u, empty);
                if (empty || u == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(u.activo() ? "Activo" : "Inactivo");
                badge.getStyleClass().addAll("badge", u.activo() ? "success" : "secondary");
                setGraphic(badge);
            }
        });

        colAcciones.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        colAcciones.setCellFactory(c -> new TableCell<>() {
            private final Button btnEditar = Componentes.iconButton("mdi2p-pencil-outline", "Editar");
            private final Button btnEliminar = Componentes.iconButton("mdi2t-trash-can-outline", "Eliminar");
            private final HBox box = new HBox(8, btnEditar, btnEliminar);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                box.setDisable(!Sesion.esAdmin());
                btnEliminar.getStyleClass().add("danger");
                btnEditar.setOnAction(e -> editar(getItem()));
                btnEliminar.setOnAction(e -> eliminar(getItem()));
            }
            @Override protected void updateItem(Usuario u, boolean empty) {
                super.updateItem(u, empty);
                setGraphic(empty || u == null ? null : box);
            }
        });
    }

    private void aplicarFiltro() {
        String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
        filtrados.setPredicate(u -> q.isEmpty()
                || u.nombre().toLowerCase(Locale.ROOT).contains(q)
                || u.usuario().toLowerCase(Locale.ROOT).contains(q)
                || rol(u.rol()).toLowerCase(Locale.ROOT).contains(q));
    }

    private void actualizarMetricas() {
        lblTotal.setText(String.valueOf(usuarios.size()));
        lblAdmins.setText(String.valueOf(usuarios.stream().filter(u -> u.rol() == Rol.ADMIN).count()));
        lblActivos.setText(String.valueOf(usuarios.stream().filter(Usuario::activo).count()));
    }

    @FXML
    private void onNuevoUsuario() {
        editar(null);
    }

    private void editar(Usuario existente) {
        boolean esNuevo = existente == null;
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(esNuevo ? "Nuevo usuario" : "Editar usuario");
        dialogo.setResizable(false);
        DialogPane panel = dialogo.getDialogPane();
        panel.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Dialogos.aplicarEstilos(panel);

        TextField nombre = new TextField(esNuevo ? "" : existente.nombre());
        TextField usuario = new TextField(esNuevo ? "" : existente.usuario());
        PasswordField password = new PasswordField();
        password.setPromptText(esNuevo ? "Contraseña" : "Dejar en blanco para conservar");
        ComboBox<Rol> rol = new ComboBox<>(FXCollections.observableArrayList(Rol.values()));
        rol.setValue(esNuevo ? Rol.CAJERO : existente.rol());
        rol.setMaxWidth(Double.MAX_VALUE);
        CheckBox activo = new CheckBox("Usuario activo");
        activo.setSelected(esNuevo || existente.activo());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8, 4, 4, 4));
        grid.add(Componentes.etiqueta("Nombre"), 0, 0);
        grid.add(nombre, 1, 0);
        grid.add(Componentes.etiqueta("Usuario"), 0, 1);
        grid.add(usuario, 1, 1);
        grid.add(Componentes.etiqueta("Contraseña"), 0, 2);
        grid.add(password, 1, 2);
        grid.add(Componentes.etiqueta("Rol"), 0, 3);
        grid.add(rol, 1, 3);
        grid.add(activo, 1, 4);
        panel.setContent(grid);

        Button btnOk = (Button) panel.lookupButton(ButtonType.OK);
        btnOk.setText("Guardar");
        btnOk.getStyleClass().add("btn-primary");
        Button btnCancel = (Button) panel.lookupButton(ButtonType.CANCEL);
        btnCancel.setText("Cancelar");
        btnCancel.getStyleClass().add("btn-secondary");

        dialogo.setResultConverter(boton -> boton);

        dialogo.showAndWait().ifPresent(ignorado -> {
            String pass = password.getText() == null ? "" : password.getText();
            try {
                if (esNuevo) {
                    Servicios.usuarios().crear(Componentes.texto(nombre), Componentes.texto(usuario),
                            pass, rol.getValue());
                } else {
                    Servicios.usuarios().actualizar(existente.id(), Componentes.texto(nombre),
                            Componentes.texto(usuario), rol.getValue(), activo.isSelected(),
                            pass.isEmpty() ? null : pass);
                }
                recargar();
            } catch (RuntimeException e) {
                Dialogos.error(e.getMessage());
            }
        });
    }

    private void eliminar(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        if (Dialogos.confirmar("¿Eliminar al usuario \"" + usuario.nombre() + "\"?")) {
            try {
                Servicios.usuarios().eliminar(usuario.id());
                recargar();
            } catch (RuntimeException e) {
                Dialogos.error(e.getMessage());
            }
        }
    }

    private String rol(Rol rol) {
        return rol == Rol.ADMIN ? "Administrador" : "Cajero";
    }
}
