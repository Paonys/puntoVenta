package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.App;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Vista de Productos (frontEnd).
 * Trabaja con datos mock en memoria; la persistencia y los modelos reales
 * se incorporarán en la fase de backend.
 */
public class ProductosController implements Initializable {

    @FXML private TableView<ProductRow> tblProductos;
    @FXML private TableColumn<ProductRow, String> colCodigo;
    @FXML private TableColumn<ProductRow, String> colNombre;
    @FXML private TableColumn<ProductRow, String> colCategoria;
    @FXML private TableColumn<ProductRow, Double> colPrecio;
    @FXML private TableColumn<ProductRow, Integer> colStock;
    @FXML private TableColumn<ProductRow, ProductRow> colEstado;
    @FXML private TableColumn<ProductRow, ProductRow> colAcciones;
    @FXML private TextField txtBuscar;
    @FXML private HBox chipsCategorias;
    @FXML private Label lblTotalProductos;
    @FXML private Label lblStockBajo;
    @FXML private Label lblValorInventario;
    @FXML private Label lblCategorias;

    private static final String TODOS = "Todos";
    private static final int UMBRAL_STOCK_BAJO = 10;

    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private final ObservableList<ProductRow> productos = FXCollections.observableArrayList();
    private final List<String> categorias = List.of("Escolares", "Oficina", "Regalos", "Arte");

    private FilteredList<ProductRow> filtrados;
    private String categoriaActiva = TODOS;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productos.setAll(mockProductos());
        filtrados = new FilteredList<>(productos, p -> true);
        tblProductos.setItems(filtrados);
        tblProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        Label placeholder = new Label("No se encontraron productos");
        placeholder.getStyleClass().add("text-tertiary");
        tblProductos.setPlaceholder(placeholder);

        configurarColumnas();
        crearChips();
        txtBuscar.textProperty().addListener((obs, old, nuevo) -> aplicarFiltros());
        actualizarMetricas();
    }

    // ------------------------------------------------------------------
    // Datos mock (será reemplazado por el backend)
    // ------------------------------------------------------------------
    private List<ProductRow> mockProductos() {
        return List.of(
                new ProductRow("P-001", "Cuaderno espiral 100 hojas", "Escolares", 25.50, 48),
                new ProductRow("P-002", "Plumas de gel (caja 12)", "Oficina", 89.90, 8),
                new ProductRow("P-003", "Peluche oso mediano", "Regalos", 149.00, 12),
                new ProductRow("P-004", "Lápices de colores 24 piezas", "Arte", 79.00, 0),
                new ProductRow("P-005", "Mochila escolar", "Escolares", 399.00, 6),
                new ProductRow("P-006", "Taza personalizada", "Regalos", 120.00, 20),
                new ProductRow("P-007", "Marcadores permanentes", "Oficina", 45.00, 35),
                new ProductRow("P-008", "Block de dibujo profesional", "Arte", 55.00, 9)
        );
    }

    // ------------------------------------------------------------------
    // Configuración de columnas
    // ------------------------------------------------------------------
    private void configurarColumnas() {
        colCodigo.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().codigo()));
        colNombre.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nombre()));
        colCategoria.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().categoria()));

        colPrecio.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().precio()));
        colPrecio.setCellFactory(c -> new TableCell<>() {
            { getStyleClass().add("num-cell"); }
            @Override protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : currency.format(valor));
            }
        });

        colStock.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().stock()));
        colStock.setCellFactory(c -> new TableCell<>() {
            { getStyleClass().add("num-cell"); }
            @Override protected void updateItem(Integer valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : String.valueOf(valor));
            }
        });

        configurarEstado();
        configurarAcciones();
    }

    private void configurarEstado() {
        colEstado.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        colEstado.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(ProductRow producto, boolean empty) {
                super.updateItem(producto, empty);
                if (empty || producto == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label();
                badge.getStyleClass().add("badge");
                if (producto.stock() <= 0) {
                    badge.setText("Agotado");
                    badge.getStyleClass().add("danger");
                } else if (producto.stock() <= UMBRAL_STOCK_BAJO) {
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
            private final Button btnEditar = iconButton("mdi2p-pencil-outline", "Editar");
            private final Button btnEliminar = iconButton("mdi2t-trash-can-outline", "Eliminar");
            private final HBox box = new HBox(8, btnEditar, btnEliminar);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                btnEliminar.getStyleClass().add("danger");
                btnEditar.setOnAction(e -> onEditar(filaEnIndice(getIndex())));
                btnEliminar.setOnAction(e -> onEliminar(filaEnIndice(getIndex())));
            }
            @Override protected void updateItem(ProductRow producto, boolean empty) {
                super.updateItem(producto, empty);
                setGraphic(empty || producto == null ? null : box);
            }
        });
    }

    private Button iconButton(String iconLiteral, String tooltip) {
        Button boton = new Button();
        boton.getStyleClass().add("btn-icon");
        FontIcon icono = new FontIcon(iconLiteral);
        icono.setIconSize(16);
        boton.setGraphic(icono);
        boton.setTooltip(new Tooltip(tooltip));
        return boton;
    }

    private ProductRow filaEnIndice(int indice) {
        return indice >= 0 && indice < tblProductos.getItems().size()
                ? tblProductos.getItems().get(indice) : null;
    }

    // ------------------------------------------------------------------
    // Filtros y métricas
    // ------------------------------------------------------------------
    private void crearChips() {
        chipsCategorias.getChildren().clear();
        ToggleGroup grupo = new ToggleGroup();
        List<String> opciones = new ArrayList<>();
        opciones.add(TODOS);
        opciones.addAll(categorias);

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
        int total = productos.size();
        long stockBajo = productos.stream().filter(p -> p.stock() <= UMBRAL_STOCK_BAJO).count();
        double valor = productos.stream().mapToDouble(p -> p.precio() * p.stock()).sum();
        long totalCategorias = productos.stream().map(ProductRow::categoria).distinct().count();

        lblTotalProductos.setText(String.valueOf(total));
        lblStockBajo.setText(String.valueOf(stockBajo));
        lblValorInventario.setText(currency.format(valor));
        lblCategorias.setText(String.valueOf(totalCategorias));
    }

    // ------------------------------------------------------------------
    // Acciones CRUD (frontend, en memoria)
    // ------------------------------------------------------------------
    @FXML
    private void onNuevoProducto() {
        ProductRow nuevo = mostrarFormulario(null);
        if (nuevo != null) {
            productos.add(0, nuevo);
            actualizarMetricas();
            aplicarFiltros();
            tblProductos.getSelectionModel().select(nuevo);
        }
    }

    private void onEditar(ProductRow producto) {
        if (producto == null) {
            return;
        }
        ProductRow editado = mostrarFormulario(producto);
        if (editado != null) {
            int indice = productos.indexOf(producto);
            if (indice >= 0) {
                productos.set(indice, editado);
                actualizarMetricas();
                aplicarFiltros();
            }
        }
    }

    private void onEliminar(ProductRow producto) {
        if (producto == null) {
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el producto \"" + producto.nombre() + "\"?", ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Eliminar producto");
        confirmacion.setHeaderText(null);
        aplicarEstilos(confirmacion.getDialogPane());
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            productos.remove(producto);
            actualizarMetricas();
            aplicarFiltros();
        }
    }

    private ProductRow mostrarFormulario(ProductRow existente) {
        boolean esNuevo = existente == null;
        Dialog<ProductRow> dialogo = new Dialog<>();
        dialogo.setTitle(esNuevo ? "Nuevo producto" : "Editar producto");
        dialogo.setResizable(false);
        if (App.getPrimaryStage() != null) {
            dialogo.initOwner(App.getPrimaryStage());
        }

        DialogPane panel = dialogo.getDialogPane();
        panel.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        aplicarEstilos(panel);

        TextField codigo = new TextField(esNuevo ? nextCodigo() : existente.codigo());
        TextField nombre = new TextField(esNuevo ? "" : existente.nombre());
        ComboBox<String> categoria = new ComboBox<>(FXCollections.observableArrayList(categorias));
        categoria.setValue(esNuevo ? categorias.get(0) : existente.categoria());
        categoria.setMaxWidth(Double.MAX_VALUE);
        TextField precio = new TextField(esNuevo ? "" : String.valueOf(existente.precio()));
        precio.setPromptText("0.00");
        TextField stock = new TextField(esNuevo ? "" : String.valueOf(existente.stock()));
        stock.setPromptText("0");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8, 4, 4, 4));
        grid.addRow(0, etiqueta("Código"), codigo);
        grid.addRow(1, etiqueta("Nombre"), nombre);
        grid.addRow(2, etiqueta("Categoría"), categoria);
        grid.addRow(3, etiqueta("Precio"), precio);
        grid.addRow(4, etiqueta("Stock"), stock);
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
            return new ProductRow(
                    textoCodigo.isEmpty() ? nextCodigo() : textoCodigo,
                    textoNombre,
                    categoria.getValue(),
                    parseDouble(precio.getText()),
                    (int) parseDouble(stock.getText()));
        });

        return dialogo.showAndWait().orElse(null);
    }

    private Label etiqueta(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("label-body");
        return label;
    }

    private void aplicarEstilos(DialogPane panel) {
        Stage owner = App.getPrimaryStage();
        if (owner != null && owner.getScene() != null) {
            panel.getStylesheets().addAll(owner.getScene().getStylesheets());
        }
    }

    private String nextCodigo() {
        int maximo = 0;
        for (ProductRow producto : productos) {
            String codigo = producto.codigo();
            if (codigo != null && codigo.startsWith("P-")) {
                try {
                    maximo = Math.max(maximo, Integer.parseInt(codigo.substring(2)));
                } catch (NumberFormatException ignored) {
                    // código con formato no numérico, se ignora para el cálculo
                }
            }
        }
        return String.format("P-%03d", maximo + 1);
    }

    private double parseDouble(String valor) {
        if (valor == null) {
            return 0;
        }
        try {
            return Double.parseDouble(valor.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Fila de la tabla (frontend). El modelo de dominio real llegará en la fase de backend. */
    public record ProductRow(String codigo, String nombre, String categoria, double precio, int stock) {
    }
}
