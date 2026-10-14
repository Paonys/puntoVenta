package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.models.Categoria;
import com.novedadesale.puntoventa.models.Promocion;
import com.novedadesale.puntoventa.models.TipoPromocion;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.shared.Formato;
import com.novedadesale.puntoventa.ui.Componentes;
import com.novedadesale.puntoventa.ui.Dialogos;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/** Ofertas y promociones: CRUD de reglas de descuento por temporada. */
public class OfertasController implements Initializable {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String TODAS = "Todas las categorías";

    @FXML private TableView<Promocion> tblOfertas;
    @FXML private TableColumn<Promocion, String> colNombre;
    @FXML private TableColumn<Promocion, String> colTipo;
    @FXML private TableColumn<Promocion, String> colValor;
    @FXML private TableColumn<Promocion, String> colCategoria;
    @FXML private TableColumn<Promocion, String> colInicio;
    @FXML private TableColumn<Promocion, String> colFin;
    @FXML private TableColumn<Promocion, Promocion> colEstado;
    @FXML private TableColumn<Promocion, Promocion> colAcciones;
    @FXML private Label lblTotal;
    @FXML private Label lblActivas;
    @FXML private Label lblVigentes;

    private final ObservableList<Promocion> ofertas = FXCollections.observableArrayList();
    private List<Categoria> categorias = List.of();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tblOfertas.setItems(ofertas);
        tblOfertas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblOfertas.setPlaceholder(Componentes.placeholder("No hay ofertas registradas"));

        configurarColumnas();
        recargar();
    }

    private void recargar() {
        categorias = Servicios.categorias().listar();
        ofertas.setAll(Servicios.promociones().listar());
        actualizarMetricas();
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nombre()));
        colTipo.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
                cd.getValue().tipo() == TipoPromocion.PORCENTAJE ? "Porcentaje" : "Monto fijo"));
        colValor.setCellValueFactory(cd -> new ReadOnlyStringWrapper(valor(cd.getValue())));
        colCategoria.setCellValueFactory(cd -> new ReadOnlyStringWrapper(categoria(cd.getValue())));
        colInicio.setCellValueFactory(cd -> new ReadOnlyStringWrapper(fecha(cd.getValue().fechaInicio())));
        colFin.setCellValueFactory(cd -> new ReadOnlyStringWrapper(fecha(cd.getValue().fechaFin())));

        colEstado.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        colEstado.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Promocion p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label();
                badge.getStyleClass().add("badge");
                LocalDate hoy = LocalDate.now();
                if (!p.activa()) {
                    badge.setText("Inactiva");
                    badge.getStyleClass().add("secondary");
                } else if (p.fechaInicio() != null && hoy.isBefore(p.fechaInicio())) {
                    badge.setText("Programada");
                } else if (p.fechaFin() != null && hoy.isAfter(p.fechaFin())) {
                    badge.setText("Expirada");
                    badge.getStyleClass().add("danger");
                } else {
                    badge.setText("Vigente");
                    badge.getStyleClass().add("success");
                }
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
                btnEliminar.getStyleClass().add("danger");
                btnEditar.setOnAction(e -> editar(getItem()));
                btnEliminar.setOnAction(e -> eliminar(getItem()));
            }
            @Override protected void updateItem(Promocion p, boolean empty) {
                super.updateItem(p, empty);
                setGraphic(empty || p == null ? null : box);
            }
        });
    }

    private void actualizarMetricas() {
        LocalDate hoy = LocalDate.now();
        lblTotal.setText(String.valueOf(ofertas.size()));
        lblActivas.setText(String.valueOf(ofertas.stream().filter(Promocion::activa).count()));
        lblVigentes.setText(String.valueOf(ofertas.stream().filter(p -> p.vigente(hoy)).count()));
    }

    @FXML
    private void onNuevaOferta() {
        editar(null);
    }

    private void editar(Promocion existente) {
        boolean esNuevo = existente == null;
        Dialog<Promocion> dialogo = new Dialog<>();
        dialogo.setTitle(esNuevo ? "Nueva oferta" : "Editar oferta");
        dialogo.setResizable(false);
        DialogPane panel = dialogo.getDialogPane();
        panel.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Dialogos.aplicarEstilos(panel);

        TextField nombre = new TextField(esNuevo ? "" : existente.nombre());
        ComboBox<TipoPromocion> tipo = new ComboBox<>(FXCollections.observableArrayList(TipoPromocion.values()));
        tipo.setValue(esNuevo ? TipoPromocion.PORCENTAJE : existente.tipo());
        tipo.setMaxWidth(Double.MAX_VALUE);
        TextField valor = new TextField(esNuevo ? "" : existente.valor().stripTrailingZeros().toPlainString());
        DatePicker inicio = new DatePicker(esNuevo ? LocalDate.now() : existente.fechaInicio());
        DatePicker fin = new DatePicker(esNuevo ? LocalDate.now().plusDays(30) : existente.fechaFin());
        inicio.setMaxWidth(Double.MAX_VALUE);
        fin.setMaxWidth(Double.MAX_VALUE);
        CheckBox activa = new CheckBox("Oferta activa");
        activa.setSelected(esNuevo || existente.activa());

        ComboBox<String> categoria = new ComboBox<>();
        categoria.getItems().add(TODAS);
        categorias.forEach(c -> categoria.getItems().add(c.nombre()));
        categoria.setValue(TODAS);
        categoria.setMaxWidth(Double.MAX_VALUE);
        if (!esNuevo && existente.categoriaId() != null) {
            categorias.stream().filter(c -> c.id() == existente.categoriaId())
                    .findFirst().ifPresent(c -> categoria.setValue(c.nombre()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8, 4, 4, 4));
        grid.add(Componentes.etiqueta("Nombre"), 0, 0);
        grid.add(nombre, 1, 0);
        grid.add(Componentes.etiqueta("Tipo"), 0, 1);
        grid.add(tipo, 1, 1);
        grid.add(Componentes.etiqueta("Valor"), 0, 2);
        grid.add(valor, 1, 2);
        grid.add(Componentes.etiqueta("Categoría"), 0, 3);
        grid.add(categoria, 1, 3);
        grid.add(Componentes.etiqueta("Inicio"), 0, 4);
        grid.add(inicio, 1, 4);
        grid.add(Componentes.etiqueta("Fin"), 0, 5);
        grid.add(fin, 1, 5);
        grid.add(activa, 1, 6);
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
            Integer categoriaId = categorias.stream()
                    .filter(c -> c.nombre().equals(categoria.getValue()))
                    .map(Categoria::id).findFirst().orElse(null);
            return new Promocion(esNuevo ? 0 : existente.id(),
                    Componentes.texto(nombre),
                    tipo.getValue(), Formato.decimal(valor.getText()),
                    inicio.getValue(), fin.getValue(), activa.isSelected(), categoriaId);
        });

        dialogo.showAndWait().ifPresent(promocion -> {
            try {
                Servicios.promociones().guardar(promocion);
                recargar();
            } catch (RuntimeException e) {
                Dialogos.error(e.getMessage());
            }
        });
    }

    private void eliminar(Promocion oferta) {
        if (oferta == null) {
            return;
        }
        if (Dialogos.confirmar("¿Eliminar la oferta \"" + oferta.nombre() + "\"?")) {
            try {
                Servicios.promociones().eliminar(oferta.id());
                recargar();
            } catch (RuntimeException e) {
                Dialogos.error(e.getMessage());
            }
        }
    }

    private String valor(Promocion p) {
        if (p.valor() == null) {
            return "";
        }
        return p.tipo() == TipoPromocion.PORCENTAJE
                ? p.valor().stripTrailingZeros().toPlainString() + "%"
                : Formato.money(p.valor());
    }

    private String categoria(Promocion p) {
        if (p.categoriaId() == null) {
            return TODAS;
        }
        return categorias.stream().filter(c -> c.id() == p.categoriaId())
                .map(Categoria::nombre).findFirst().orElse("—");
    }

    private String fecha(LocalDate valor) {
        return valor == null ? "—" : valor.format(FECHA);
    }
}
