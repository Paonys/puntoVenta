package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.App;
import com.novedadesale.puntoventa.models.Categoria;
import com.novedadesale.puntoventa.models.HistorialProducto;
import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.models.Proveedor;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.shared.AppConstants;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/** Vista de Productos: catálogo, detalle editable e historial de cambios. */
public class ProductosController implements Initializable {

    @FXML private VBox vistaLista;
    @FXML private VBox vistaDetalle;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, Producto> colImagen;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colProveedor;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Producto> colEstado;
    @FXML private TextField txtBuscar;
    @FXML private HBox chipsCategorias;
    @FXML private Label lblTotalProductos;
    @FXML private Label lblStockBajo;
    @FXML private Label lblValorInventario;
    @FXML private Label lblCategorias;
    @FXML private Button btnNuevo;

    @FXML private Label lblDetalleNombre;
    @FXML private Label lblDetalleCodigo;
    @FXML private Label lblEstadoDetalle;
    @FXML private Label lblDetalleContacto;
    @FXML private Label lblImagenDetalle;
    @FXML private ImageView imgDetalle;
    @FXML private FontIcon iconoImagenDetalle;
    @FXML private HBox accionesImagenDetalle;
    @FXML private Button btnEditarDetalle;
    @FXML private Button btnEliminarDetalle;
    @FXML private Button btnGuardarDetalle;
    @FXML private Button btnCancelarDetalle;
    @FXML private TextField txtDetalleCodigo;
    @FXML private TextField txtDetalleNombre;
    @FXML private TextField txtDetallePrecio;
    @FXML private TextField txtDetalleStock;
    @FXML private TextField txtDetalleStockMinimo;
    @FXML private ComboBox<String> cmbDetalleCategoria;
    @FXML private ComboBox<String> cmbDetalleProveedor;
    @FXML private VBox cajaHistorial;
    @FXML private TableView<HistorialProducto> tblHistorial;
    @FXML private TableColumn<HistorialProducto, String> colHistFecha;
    @FXML private TableColumn<HistorialProducto, String> colHistAccion;
    @FXML private TableColumn<HistorialProducto, String> colHistUsuario;
    @FXML private TableColumn<HistorialProducto, String> colHistDetalle;

    private static final String TODOS = "Todos";
    private static final String SIN_PROVEEDOR = "Sin proveedor";
    private static final int UMBRAL_STOCK_BAJO = AppConstants.UMBRAL_STOCK_BAJO;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final List<Categoria> categorias = new ArrayList<>();
    private final List<Proveedor> proveedores = new ArrayList<>();

    private FilteredList<Producto> filtrados;
    private String categoriaActiva = TODOS;
    private Producto productoDetalle;
    private String imagenDetalle;
    private boolean editando;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        filtrados = new FilteredList<>(productos, p -> true);
        tblProductos.setItems(filtrados);
        tblProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblProductos.setPlaceholder(Componentes.placeholder("No se encontraron productos"));
        tblProductos.setRowFactory(tv -> {
            TableRow<Producto> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1 && !fila.isEmpty()) {
                    abrirDetalle(fila.getItem());
                }
            });
            return fila;
        });

        configurarColumnas();
        configurarHistorial();
        Componentes.soloAdmin(btnNuevo);
        Componentes.soloAdmin(btnEditarDetalle);
        Componentes.soloAdmin(btnEliminarDetalle);
        Componentes.soloAdmin(cajaHistorial);
        cmbDetalleProveedor.valueProperty().addListener((obs, viejo, nuevo) -> actualizarContactoDetalle());
        txtBuscar.textProperty().addListener((obs, old, nuevo) -> aplicarFiltros());
        recargar();
    }

    private void recargar() {
        categorias.clear();
        categorias.addAll(Servicios.categorias().listar());
        proveedores.clear();
        proveedores.addAll(Servicios.proveedores().listar());

        cmbDetalleCategoria.setItems(FXCollections.observableArrayList(
                categorias.stream().map(Categoria::nombre).toList()));
        cmbDetalleProveedor.setItems(FXCollections.observableArrayList(opcionesProveedores()));

        productos.setAll(Servicios.productos().listar());
        crearChips();
        aplicarFiltros();
        actualizarMetricas();
    }

    /** Aplica una búsqueda desde el buscador global del layout (todas las categorías). */
    public void buscar(String texto) {
        mostrarLista();
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
        configurarImagen();
        colNombre.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nombre()));
        colCategoria.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().categoria()));
        colProveedor.setCellValueFactory(cd -> new ReadOnlyStringWrapper(Componentes.nulo(cd.getValue().proveedor())));

        colPrecio.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().precio()));
        colPrecio.setCellFactory(c -> Componentes.moneyCell());

        colStock.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().stock()));
        colStock.setCellFactory(c -> Componentes.numberCell());

        configurarEstado();
    }

    private void configurarImagen() {
        colImagen.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        colImagen.setCellFactory(c -> new TableCell<>() {
            private final ImageView vista = new ImageView();
            private final FontIcon icono = new FontIcon("mdi2i-image-outline");
            private final HBox caja = new HBox(vista, icono);
            {
                caja.setAlignment(Pos.CENTER_LEFT);
                vista.setFitWidth(36);
                vista.setFitHeight(36);
                vista.setPreserveRatio(true);
                icono.setIconSize(20);
            }
            @Override protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);
                if (empty || producto == null) {
                    setGraphic(null);
                    return;
                }
                Image imagen = cargarImagen(producto.imagen(), 36);
                vista.setImage(imagen);
                vista.setVisible(imagen != null);
                vista.setManaged(imagen != null);
                icono.setVisible(imagen == null);
                icono.setManaged(imagen == null);
                setGraphic(caja);
            }
        });
    }

    private static Image cargarImagen(String ruta, double tamano) {
        if (ruta == null || ruta.isBlank()) {
            return null;
        }
        try {
            Path archivo = Path.of(ruta);
            if (!Files.isReadable(archivo)) {
                return null;
            }
            return new Image(archivo.toUri().toString(), tamano, tamano, true, true);
        } catch (RuntimeException e) {
            return null;
        }
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
                setGraphic(badgeEstado(producto));
            }
        });
    }

    private static Label badgeEstado(Producto producto) {
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
        return badge;
    }

    // ------------------------------------------------------------------
    // Vista de detalle
    // ------------------------------------------------------------------
    private void abrirDetalle(Producto producto) {
        if (producto == null) {
            return;
        }
        productoDetalle = producto;
        imagenDetalle = producto.imagen();
        cargarDetalle();
        setModoEdicion(false);
        vistaLista.setVisible(false);
        vistaLista.setManaged(false);
        vistaDetalle.setVisible(true);
        vistaDetalle.setManaged(true);
        cargarHistorial();
    }

    private void cargarDetalle() {
        if (productoDetalle == null) {
            return;
        }
        lblDetalleNombre.setText(productoDetalle.nombre());
        lblDetalleCodigo.setText(productoDetalle.codigo());
        txtDetalleCodigo.setText(productoDetalle.codigo());
        txtDetalleNombre.setText(productoDetalle.nombre());
        cmbDetalleCategoria.setValue(productoDetalle.categoria());
        cmbDetalleProveedor.setValue(productoDetalle.proveedor() == null || productoDetalle.proveedor().isBlank()
                ? SIN_PROVEEDOR
                : productoDetalle.proveedor());
        txtDetallePrecio.setText(productoDetalle.precio().toPlainString());
        txtDetalleStock.setText(String.valueOf(productoDetalle.stock()));
        txtDetalleStockMinimo.setText(String.valueOf(productoDetalle.stockMinimo()));
        actualizarEstadoDetalle();
        actualizarContactoDetalle();
        refrescarImagenDetalle();
    }

    private void actualizarEstadoDetalle() {
        lblEstadoDetalle.getStyleClass().removeAll("success", "warning", "danger");
        if (productoDetalle.agotado()) {
            lblEstadoDetalle.setText("Agotado");
            lblEstadoDetalle.getStyleClass().add("danger");
        } else if (productoDetalle.stockBajo()) {
            lblEstadoDetalle.setText("Stock bajo");
            lblEstadoDetalle.getStyleClass().add("warning");
        } else {
            lblEstadoDetalle.setText("Disponible");
            lblEstadoDetalle.getStyleClass().add("success");
        }
    }

    private void actualizarContactoDetalle() {
        Proveedor proveedor = proveedores.stream()
                .filter(p -> p.nombre().equals(cmbDetalleProveedor.getValue()))
                .findFirst().orElse(null);
        List<String> datos = new ArrayList<>();
        if (proveedor != null) {
            agregarDato(datos, proveedor.contacto());
            agregarDato(datos, proveedor.telefono());
            agregarDato(datos, proveedor.email());
        }
        lblDetalleContacto.setText(datos.isEmpty() ? "Sin datos de contacto" : String.join(" · ", datos));
    }

    private static void agregarDato(List<String> datos, String valor) {
        if (valor != null && !valor.isBlank()) {
            datos.add(valor);
        }
    }

    private void refrescarImagenDetalle() {
        Image imagen = cargarImagen(imagenDetalle, 220);
        imgDetalle.setImage(imagen);
        imgDetalle.setVisible(imagen != null);
        imgDetalle.setManaged(imagen != null);
        iconoImagenDetalle.setVisible(imagen == null);
        iconoImagenDetalle.setManaged(imagen == null);
        lblImagenDetalle.setText(imagen == null ? "Sin imagen" : nombreArchivo(imagenDetalle));
    }

    private static String nombreArchivo(String ruta) {
        try {
            return Path.of(ruta).getFileName().toString();
        } catch (RuntimeException e) {
            return ruta;
        }
    }

    private void setModoEdicion(boolean activo) {
        editando = activo;
        boolean admin = Sesion.esAdmin();
        boolean verAcciones = admin && !activo;

        txtDetalleCodigo.setDisable(!activo);
        txtDetalleNombre.setDisable(!activo);
        txtDetallePrecio.setDisable(!activo);
        txtDetalleStock.setDisable(!activo);
        txtDetalleStockMinimo.setDisable(!activo);
        cmbDetalleCategoria.setDisable(!activo);
        cmbDetalleProveedor.setDisable(!activo);

        btnEditarDetalle.setVisible(verAcciones);
        btnEditarDetalle.setManaged(verAcciones);
        btnEliminarDetalle.setVisible(verAcciones);
        btnEliminarDetalle.setManaged(verAcciones);
        btnGuardarDetalle.setVisible(activo);
        btnGuardarDetalle.setManaged(activo);
        btnCancelarDetalle.setVisible(activo);
        btnCancelarDetalle.setManaged(activo);
        accionesImagenDetalle.setVisible(activo);
        accionesImagenDetalle.setManaged(activo);
    }

    private void mostrarLista() {
        productoDetalle = null;
        imagenDetalle = null;
        editando = false;
        tblHistorial.getItems().clear();
        vistaDetalle.setVisible(false);
        vistaDetalle.setManaged(false);
        vistaLista.setVisible(true);
        vistaLista.setManaged(true);
    }

    @FXML
    private void onVolverDetalle() {
        if (editando && !Dialogos.confirmar("¿Descartar los cambios sin guardar?")) {
            return;
        }
        mostrarLista();
    }

    @FXML
    private void onEditarDetalle() {
        if (productoDetalle == null || !Sesion.esAdmin()) {
            return;
        }
        setModoEdicion(true);
        txtDetalleNombre.requestFocus();
    }

    @FXML
    private void onCancelarDetalle() {
        if (productoDetalle == null) {
            return;
        }
        productoDetalle = Servicios.productos().porId(productoDetalle.id()).orElse(productoDetalle);
        imagenDetalle = productoDetalle.imagen();
        cargarDetalle();
        setModoEdicion(false);
    }

    @FXML
    private void onGuardarDetalle() {
        if (productoDetalle == null) {
            return;
        }
        try {
            Categoria categoria = categorias.stream()
                    .filter(c -> c.nombre().equals(cmbDetalleCategoria.getValue()))
                    .findFirst().orElse(null);
            Proveedor proveedor = proveedores.stream()
                    .filter(p -> p.nombre().equals(cmbDetalleProveedor.getValue()))
                    .findFirst().orElse(null);

            Producto actualizado = new Producto(
                    productoDetalle.id(),
                    Componentes.texto(txtDetalleCodigo),
                    Componentes.texto(txtDetalleNombre),
                    categoria == null ? 0 : categoria.id(),
                    categoria == null ? "" : categoria.nombre(),
                    proveedor == null ? null : proveedor.id(),
                    proveedor == null ? "" : proveedor.nombre(),
                    Formato.decimal(txtDetallePrecio.getText()),
                    (int) Formato.decimal(txtDetalleStock.getText()).doubleValue(),
                    (int) Formato.decimal(txtDetalleStockMinimo.getText()).doubleValue(),
                    imagenDetalle);

            Servicios.productos().guardar(actualizado);
            productoDetalle = Servicios.productos().porId(actualizado.id()).orElse(actualizado);
            imagenDetalle = productoDetalle.imagen();
            recargar();
            cargarDetalle();
            setModoEdicion(false);
            cargarHistorial();
        } catch (RuntimeException e) {
            Dialogos.error(e.getMessage());
        }
    }

    @FXML
    private void onEliminarDetalle() {
        if (productoDetalle == null || !Sesion.esAdmin()) {
            return;
        }
        if (!Dialogos.confirmar("¿Eliminar el producto \"" + productoDetalle.nombre() + "\"?")) {
            return;
        }
        try {
            Servicios.productos().eliminar(productoDetalle.id());
            mostrarLista();
            recargar();
        } catch (RuntimeException e) {
            Dialogos.error(e.getMessage());
        }
    }

    @FXML
    private void onCambiarImagenDetalle() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar imagen de referencia");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File archivo = chooser.showOpenDialog(App.getPrimaryStage());
        if (archivo != null) {
            imagenDetalle = archivo.getAbsolutePath();
            refrescarImagenDetalle();
        }
    }

    @FXML
    private void onQuitarImagenDetalle() {
        imagenDetalle = null;
        refrescarImagenDetalle();
    }

    // ------------------------------------------------------------------
    // Historial
    // ------------------------------------------------------------------
    private void configurarHistorial() {
        tblHistorial.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblHistorial.setPlaceholder(Componentes.placeholder("Sin cambios registrados"));
        colHistFecha.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().fecha()));
        colHistAccion.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().accion()));
        colHistUsuario.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().usuario()));
        colHistDetalle.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().detalle()));
    }

    private void cargarHistorial() {
        if (productoDetalle == null || !Sesion.esAdmin()) {
            tblHistorial.getItems().clear();
            return;
        }
        try {
            tblHistorial.setItems(FXCollections.observableArrayList(
                    Servicios.productos().historial(productoDetalle.id())));
        } catch (RuntimeException e) {
            tblHistorial.getItems().clear();
        }
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
    // Alta de productos
    // ------------------------------------------------------------------
    @FXML
    private void onNuevoProducto() {
        Producto borrador = mostrarFormulario();
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

    private Producto mostrarFormulario() {
        Dialog<Producto> dialogo = new Dialog<>();
        dialogo.setTitle("Nuevo producto");
        dialogo.setResizable(false);
        if (App.getPrimaryStage() != null) {
            dialogo.initOwner(App.getPrimaryStage());
        }

        DialogPane panel = dialogo.getDialogPane();
        panel.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Dialogos.aplicarEstilos(panel);

        TextField codigo = new TextField(nextCodigo());
        TextField nombre = new TextField();
        ComboBox<String> categoria = new ComboBox<>(FXCollections.observableArrayList(
                categorias.stream().map(Categoria::nombre).toList()));
        categoria.setValue(categorias.isEmpty() ? null : categorias.get(0).nombre());
        categoria.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> proveedor = new ComboBox<>(FXCollections.observableArrayList(opcionesProveedores()));
        proveedor.setValue(SIN_PROVEEDOR);
        proveedor.setMaxWidth(Double.MAX_VALUE);
        TextField precio = new TextField();
        precio.setPromptText("0.00");
        TextField stock = new TextField();
        stock.setPromptText("0");

        String[] imagenSeleccionada = {null};
        ImageView vistaPrevia = new ImageView();
        vistaPrevia.setFitWidth(72);
        vistaPrevia.setFitHeight(72);
        vistaPrevia.setPreserveRatio(true);
        Label lblImagen = new Label();
        lblImagen.getStyleClass().add("text-tertiary");
        Button btnElegirImagen = new Button("Elegir imagen");
        btnElegirImagen.getStyleClass().add("btn-secondary");
        Button btnQuitarImagen = new Button("Quitar");
        btnQuitarImagen.getStyleClass().add("btn-secondary");
        Runnable refrescarImagen = () -> {
            Image imagen = cargarImagen(imagenSeleccionada[0], 72);
            vistaPrevia.setImage(imagen);
            vistaPrevia.setVisible(imagen != null);
            vistaPrevia.setManaged(imagen != null);
            lblImagen.setText(imagen == null
                    ? "Sin imagen"
                    : nombreArchivo(imagenSeleccionada[0]));
        };
        refrescarImagen.run();
        btnElegirImagen.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Seleccionar imagen de referencia");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    "Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File archivo = chooser.showOpenDialog(App.getPrimaryStage());
            if (archivo != null) {
                imagenSeleccionada[0] = archivo.getAbsolutePath();
                refrescarImagen.run();
            }
        });
        btnQuitarImagen.setOnAction(e -> {
            imagenSeleccionada[0] = null;
            refrescarImagen.run();
        });
        HBox filaImagen = new HBox(10, vistaPrevia, lblImagen, btnElegirImagen, btnQuitarImagen);
        filaImagen.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8, 4, 4, 4));
        grid.addRow(0, Componentes.etiqueta("Código"), codigo);
        grid.addRow(1, Componentes.etiqueta("Nombre"), nombre);
        grid.addRow(2, Componentes.etiqueta("Categoría"), categoria);
        grid.addRow(3, Componentes.etiqueta("Proveedor"), proveedor);
        grid.addRow(4, Componentes.etiqueta("Precio"), precio);
        grid.addRow(5, Componentes.etiqueta("Stock"), stock);
        grid.addRow(6, Componentes.etiqueta("Imagen"), filaImagen);
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
            Proveedor prov = proveedores.stream()
                    .filter(p -> p.nombre().equals(proveedor.getValue()))
                    .findFirst().orElse(null);
            return new Producto(0,
                    textoCodigo.isEmpty() ? nextCodigo() : textoCodigo,
                    textoNombre,
                    cat == null ? 0 : cat.id(),
                    cat == null ? "" : cat.nombre(),
                    prov == null ? null : prov.id(),
                    prov == null ? "" : prov.nombre(),
                    Formato.decimal(precio.getText()),
                    (int) Formato.decimal(stock.getText()).doubleValue(),
                    UMBRAL_STOCK_BAJO,
                    imagenSeleccionada[0]);
        });

        return dialogo.showAndWait().orElse(null);
    }

    private List<String> opcionesProveedores() {
        List<String> opciones = new ArrayList<>();
        opciones.add(SIN_PROVEEDOR);
        proveedores.forEach(p -> opciones.add(p.nombre()));
        return opciones;
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
