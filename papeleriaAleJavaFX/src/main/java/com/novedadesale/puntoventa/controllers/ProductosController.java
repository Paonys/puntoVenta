package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.App;
import com.novedadesale.puntoventa.models.Categoria;
import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.shared.AppConstants;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/** Vista de Productos: CRUD real sobre {@link Servicios#productos()}. */
public class ProductosController implements Initializable {

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Producto> colEstado;
    @FXML private TableColumn<Producto, Producto> colAcciones;
    @FXML private TextField txtBuscar;
    @FXML private HBox chipsCategorias;
    @FXML private Label lblTotalProductos;
    @FXML private Label lblStockBajo;
    @FXML private Label lblValorInventario;
    @FXML private Label lblCategorias;

    private static final String TODOS = "Todos";
    private static final int UMBRAL_STOCK_BAJO = AppConstants.UMBRAL_STOCK_BAJO;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final List<Categoria> categorias = new ArrayList<>();

    private FilteredList<Producto> filtrados;
    private String categoriaActiva = TODOS;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        filtrados = new FilteredList<>(productos, p -> true);
        tblProductos.setItems(filtrados);
        tblProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblProductos.setPlaceholder(Componentes.placeholder("No se encontraron productos"));

        configurarColumnas();
        txtBuscar.textProperty().addListener((obs, old, nuevo) -> aplicarFiltros());
        recargar();
    }

    private void recargar() {
        categorias.clear();
        categorias.addAll(Servicios.categorias().listar());
        productos.setAll(Servicios.productos().listar());
        crearChips();
        aplicarFiltros();
        actualizarMetricas();
    }

    /** Aplica una búsqueda desde el buscador global del layout (todas las categorías). */
    public void buscar(String texto) {
        categoriaActiva = TODOS;
        crearChips();
        txtBuscar.setText(texto == null ? "" : texto);
        aplicarFiltros();
        txtBuscar.requestFocus();
    }

    // ------------------------------------------------------------------
    // Configuración de columnas
    // ------------------------------------------------------------------
    private void configurarColumnas() {
        colCodigo.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().codigo()));
        colNombre.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nombre()));
        colCategoria.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().categoria()));

        colPrecio.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().precio()));
        colPrecio.setCellFactory(c -> Componentes.moneyCell());

        colStock.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().stock()));
        colStock.setCellFactory(c -> Componentes.numberCell());

        configurarEstado();
        configurarAcciones();
    }

    private void configurarEstado() {
        colEstado.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        colEstado.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);
                if (empty || producto == null) {
                    setGraphic(null);
                    return;
                }
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
                setGraphic(badge);
            }
        });
    }

    private void configurarAcciones() {
        colAcciones.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        colAcciones.setCellFactory(c -> new TableCell<>() {
            private final Button btnEditar = Componentes.iconButton("mdi2p-pencil-outline", "Editar");
            private final Button btnEliminar = Componentes.iconButton("mdi2t-trash-can-outline", "Eliminar");
            private final HBox box = new HBox(8, btnEditar, btnEliminar);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                btnEliminar.getStyleClass().add("danger");
                Componentes.soloAdmin(btnEliminar);
                btnEditar.setOnAction(e -> onEditar(getItem()));
                btnEliminar.setOnAction(e -> onEliminar(getItem()));
            }
            @Override protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);
                setGraphic(empty || producto == null ? null : box);
            }
        });
    }

    // ------------------------------------------------------------------
    // Filtros y métricas
    // ------------------------------------------------------------------
    private void crearChips() {
        chipsCategorias.getChildren().clear();
        ToggleGroup grupo = new ToggleGroup();
        List<String> opciones = new ArrayList<>();
        opciones.add(TODOS);
        categorias.forEach(c -> opciones.add(c.nombre()));

        if (!TODOS.equals(categoriaActiva) && !opciones.contains(categoriaActiva)) {
            categoriaActiva = TODOS;
        }

        for (String categoria : opciones) {
            ToggleButton chip = new ToggleButton(categoria);
            chip.getStyleClass().add("chip");
            chip.setToggleGroup(grupo);
            chip.setSelected(categoria.equals(categoriaActiva));
            chip.setOnAction(e -> {
                if (!chip.isSelected()) {
                    chip.setSelected(true);
                }
                categoriaActiva = categoria;
                aplicarFiltros();
            });
            chipsCategorias.getChildren().add(chip);
        }
    }

    private void aplicarFiltros() {
        String consulta = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
        filtrados.setPredicate(producto -> {
            boolean coincideCategoria = TODOS.equals(categoriaActiva)
                    || producto.categoria().equalsIgnoreCase(categoriaActiva);
            boolean coincideTexto = consulta.isEmpty()
                    || producto.nombre().toLowerCase(Locale.ROOT).contains(consulta)
                    || producto.codigo().toLowerCase(Locale.ROOT).contains(consulta);
            return coincideCategoria && coincideTexto;
        });
    }

    private void actualizarMetricas() {
        long stockBajo = productos.stream().filter(Producto::stockBajo).count();
        BigDecimal valor = productos.stream()
                .map(p -> p.precio().multiply(BigDecimal.valueOf(p.stock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotalProductos.setText(String.valueOf(productos.size()));
        lblStockBajo.setText(String.valueOf(stockBajo));
        lblValorInventario.setText(Formato.money(valor));
        lblCategorias.setText(String.valueOf(categorias.size()));
    }

    // ------------------------------------------------------------------
    // Acciones CRUD
    // ------------------------------------------------------------------
    @FXML
    private void onNuevoProducto() {
        Producto borrador = mostrarFormulario(null);
        if (borrador == null) {
            return;
        }
        try {
            Servicios.productos().guardar(borrador);
            recargar();
        } catch (RuntimeException e) {
            Dialogos.error(e.getMessage());
        }
    }

    private void onEditar(Producto producto) {
        if (producto == null) {
            return;
        }
        Producto borrador = mostrarFormulario(producto);
        if (borrador == null) {
            return;
        }
        try {
            Servicios.productos().guardar(borrador);
            recargar();
        } catch (RuntimeException e) {
            Dialogos.error(e.getMessage());
        }
    }

    private void onEliminar(Producto producto) {
        if (producto == null) {
            return;
        }
        if (Dialogos.confirmar("¿Eliminar el producto \"" + producto.nombre() + "\"?")) {
            try {
                Servicios.productos().eliminar(producto.id());
                recargar();
            } catch (RuntimeException e) {
                Dialogos.error(e.getMessage());
            }
        }
    }

    private Producto mostrarFormulario(Producto existente) {
        boolean esNuevo = existente == null;
        Dialog<Producto> dialogo = new Dialog<>();
        dialogo.setTitle(esNuevo ? "Nuevo producto" : "Editar producto");
        dialogo.setResizable(false);
        if (App.getPrimaryStage() != null) {
            dialogo.initOwner(App.getPrimaryStage());
        }

        DialogPane panel = dialogo.getDialogPane();
        panel.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Dialogos.aplicarEstilos(panel);

        TextField codigo = new TextField(esNuevo ? nextCodigo() : existente.codigo());
        TextField nombre = new TextField(esNuevo ? "" : existente.nombre());
        ComboBox<String> categoria = new ComboBox<>(FXCollections.observableArrayList(
                categorias.stream().map(Categoria::nombre).toList()));
        categoria.setValue(esNuevo
                ? (categorias.isEmpty() ? null : categorias.get(0).nombre())
                : existente.categoria());
        categoria.setMaxWidth(Double.MAX_VALUE);
        TextField precio = new TextField(esNuevo ? "" : existente.precio().toPlainString());
        precio.setPromptText("0.00");
        TextField stock = new TextField(esNuevo ? "" : String.valueOf(existente.stock()));
        stock.setPromptText("0");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8, 4, 4, 4));
        grid.addRow(0, Componentes.etiqueta("Código"), codigo);
        grid.addRow(1, Componentes.etiqueta("Nombre"), nombre);
        grid.addRow(2, Componentes.etiqueta("Categoría"), categoria);
        grid.addRow(3, Componentes.etiqueta("Precio"), precio);
        grid.addRow(4, Componentes.etiqueta("Stock"), stock);
        panel.setContent(grid);

        Button btnOk = (Button) panel.lookupButton(ButtonType.OK);
        btnOk.setText("Guardar");
        btnOk.getStyleClass().add("btn-primary");
        Button btnCancelar = (Button) panel.lookupButton(ButtonType.CANCEL);
        btnCancelar.setText("Cancelar");
        btnCancelar.getStyleClass().add("btn-secondary");

        dialogo.setResultConverter(boton -> {
            if (boton != ButtonType.OK) {
                return null;
            }
            String textoNombre = nombre.getText() == null ? "" : nombre.getText().trim();
            if (textoNombre.isEmpty()) {
                return null;
            }
            String textoCodigo = codigo.getText() == null ? "" : codigo.getText().trim();
            Categoria cat = categorias.stream()
                    .filter(c -> c.nombre().equals(categoria.getValue()))
                    .findFirst().orElse(null);
            int id = esNuevo ? 0 : existente.id();
            int stockMinimo = esNuevo ? UMBRAL_STOCK_BAJO : existente.stockMinimo();
            return new Producto(id,
                    textoCodigo.isEmpty() ? nextCodigo() : textoCodigo,
                    textoNombre,
                    cat == null ? 0 : cat.id(),
                    cat == null ? "" : cat.nombre(),
                    Formato.decimal(precio.getText()),
                    (int) Formato.decimal(stock.getText()).doubleValue(),
                    stockMinimo);
        });

        return dialogo.showAndWait().orElse(null);
    }

    private String nextCodigo() {
        int maximo = 0;
        for (Producto producto : productos) {
            String codigo = producto.codigo();
            if (codigo != null && codigo.startsWith("P-")) {
                try {
                    maximo = Math.max(maximo, Integer.parseInt(codigo.substring(2)));
                } catch (NumberFormatException ignored) {
                    // código con formato no numérico, se ignora
                }
            }
        }
        return String.format("P-%03d", maximo + 1);
    }
}
