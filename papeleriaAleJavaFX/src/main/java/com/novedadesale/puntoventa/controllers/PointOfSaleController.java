package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.App;
import com.novedadesale.puntoventa.models.Cliente;
import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.MetodoPago;
import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.models.SolicitudVenta;
import com.novedadesale.puntoventa.models.Totales;
import com.novedadesale.puntoventa.models.Venta;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.shared.CalculadoraTotales;
import com.novedadesale.puntoventa.shared.Formato;
import com.novedadesale.puntoventa.ui.Componentes;
import com.novedadesale.puntoventa.ui.Dialogos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/** Punto de venta: catálogo, carrito, cobro y ticket. */
public class PointOfSaleController implements Initializable {

    @FXML private FlowPane productGrid;
    @FXML private VBox cartItemsContainer;
    @FXML private Label lblVentasDia;
    @FXML private Label lblProductos;
    @FXML private Label lblValorInventario;
    @FXML private Label lblStockBajo;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIVA;
    @FXML private Label lblDescuento;
    @FXML private Label lblDescuentoTexto;
    @FXML private Label lblTotal;
    @FXML private TextField txtBuscarProducto;
    @FXML private ComboBox<Cliente> cmbCliente;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Cliente PUBLICO_GENERAL =
            new Cliente(0, "Público general", "", "", BigDecimal.ZERO);

    private final ObservableList<ItemVenta> carrito = FXCollections.observableArrayList();
    private Cliente clienteSeleccionado;
    private BigDecimal descuentoPct = BigDecimal.ZERO;
    private Totales totales = CalculadoraTotales.calcular(List.of(), BigDecimal.ZERO);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        txtBuscarProducto.textProperty().addListener((obs, old, nuevo) -> cargarProductos(nuevo));
        cargarClientes();
        cargarProductos("");
        cargarMetricas();
        recalcular();
    }

    private void cargarClientes() {
        List<Cliente> clientes = new ArrayList<>();
        clientes.add(PUBLICO_GENERAL);
        clientes.addAll(Servicios.clientes().listar());
        cmbCliente.setItems(FXCollections.observableArrayList(clientes));
        cmbCliente.setConverter(new StringConverter<>() {
            @Override public String toString(Cliente cliente) {
                if (cliente == null) {
                    return "";
                }
                if (cliente.id() == 0 || cliente.descuento() == null || cliente.descuento().signum() <= 0) {
                    return cliente.nombre();
                }
                return cliente.nombre() + " (" + cliente.descuento().stripTrailingZeros().toPlainString() + "%)";
            }
            @Override public Cliente fromString(String texto) {
                return null;
            }
        });
        cmbCliente.setValue(PUBLICO_GENERAL);
        clienteSeleccionado = null;
        cmbCliente.valueProperty().addListener((obs, old, nuevo) -> {
            clienteSeleccionado = (nuevo == null || nuevo.id() == 0) ? null : nuevo;
            recalcular();
        });
    }

    // ------------------------------------------------------------------
    // Catálogo
    // ------------------------------------------------------------------
    private void cargarProductos(String texto) {
        productGrid.getChildren().clear();
        List<Producto> productos = (texto == null || texto.isBlank())
                ? Servicios.productos().listar()
                : Servicios.productos().buscar(texto);
        for (Producto producto : productos) {
            productGrid.getChildren().add(crearTarjeta(producto));
        }
    }

    private VBox crearTarjeta(Producto producto) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(180);
        card.setSpacing(8);

        VBox imgContainer = new VBox();
        imgContainer.getStyleClass().add("product-image");
        imgContainer.setAlignment(Pos.CENTER);
        FontIcon icono = new FontIcon("mdi2p-package-variant-closed");
        icono.setIconSize(34);
        imgContainer.getChildren().add(icono);

        Label priceLabel = new Label(Formato.money(producto.precio()));
        priceLabel.getStyleClass().add("product-price");

        Label nameLabel = new Label(producto.nombre());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);

        Label stockLabel = new Label("Stock: " + producto.stock());
        stockLabel.getStyleClass().add("product-stock");

        Button addBtn = new Button("Agregar");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> agregar(producto));
        addBtn.setDisable(producto.agotado());

        card.getChildren().addAll(imgContainer, priceLabel, nameLabel, stockLabel, addBtn);
        return card;
    }

    private void agregar(Producto producto) {
        Optional<ItemVenta> existente = carrito.stream()
                .filter(i -> i.productoId() == producto.id())
                .findFirst();
        if (existente.isPresent()) {
            ItemVenta item = existente.get();
            if (item.cantidad() >= producto.stock()) {
                Dialogos.aviso("No hay más existencias de \"" + producto.nombre() + "\".");
                return;
            }
            int indice = carrito.indexOf(item);
            carrito.set(indice, item.conCantidad(item.cantidad() + 1));
        } else {
            carrito.add(new ItemVenta(producto.id(), producto.codigo(), producto.nombre(),
                    producto.precio(), 1));
        }
        recalcular();
    }

    private void cambiarCantidad(ItemVenta item, int delta) {
        Optional<Producto> producto = Servicios.productos().porId(item.productoId());
        int nueva = item.cantidad() + delta;
        if (nueva <= 0) {
            carrito.remove(item);
            recalcular();
            return;
        }
        if (producto.isPresent() && nueva > producto.get().stock()) {
            Dialogos.aviso("Stock insuficiente de \"" + item.nombre() + "\".");
            return;
        }
        carrito.set(carrito.indexOf(item), item.conCantidad(nueva));
        recalcular();
    }

    // ------------------------------------------------------------------
    // Carrito y totales
    // ------------------------------------------------------------------
    private void recalcular() {
        BigDecimal promoPct = carrito.isEmpty()
                ? BigDecimal.ZERO
                : Servicios.promociones().calcularDescuento(carrito, LocalDate.now());
        BigDecimal clientePct = (clienteSeleccionado != null && clienteSeleccionado.descuento() != null)
                ? clienteSeleccionado.descuento()
                : BigDecimal.ZERO;
        // Solo para la vista previa: el servicio vuelve a resolver las promociones
        // y el descuento configurado del cliente al registrar la venta.
        descuentoPct = promoPct.add(clientePct);
        if (descuentoPct.compareTo(BigDecimal.valueOf(100)) > 0) {
            descuentoPct = BigDecimal.valueOf(100);
        }
        totales = CalculadoraTotales.calcular(carrito, descuentoPct);

        lblSubtotal.setText(Formato.money(totales.subtotal()));
        lblIVA.setText(Formato.money(totales.iva()));
        lblDescuento.setText("-" + Formato.money(totales.descuento()));
        lblDescuentoTexto.setText(descuentoPct.signum() > 0
                ? "Descuento (" + descuentoPct.stripTrailingZeros().toPlainString() + "%)"
                : "Descuento");
        lblTotal.setText(Formato.money(totales.total()));

        renderCarrito();
    }

    private void renderCarrito() {
        cartItemsContainer.getChildren().clear();
        if (carrito.isEmpty()) {
            Label vacio = new Label("El carrito está vacío");
            vacio.getStyleClass().add("empty-cart-text");
            VBox contenedor = new VBox(vacio);
            contenedor.getStyleClass().add("empty-cart");
            contenedor.setAlignment(Pos.CENTER);
            cartItemsContainer.getChildren().add(contenedor);
            return;
        }
        for (ItemVenta item : carrito) {
            cartItemsContainer.getChildren().add(filaCarrito(item));
        }
    }

    private HBox filaCarrito(ItemVenta item) {
        HBox fila = new HBox(8);
        fila.getStyleClass().add("cart-item");
        fila.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(2);
        Label nombre = new Label(item.nombre());
        nombre.getStyleClass().add("cart-item-name");
        nombre.setWrapText(true);
        Label precio = new Label(Formato.money(item.precioUnitario()) + " c/u");
        precio.getStyleClass().add("cart-item-price");
        info.getChildren().addAll(nombre, precio);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button menos = new Button("−");
        menos.getStyleClass().add("qty-btn");
        menos.setOnAction(e -> cambiarCantidad(item, -1));
        Label cantidad = new Label(String.valueOf(item.cantidad()));
        cantidad.getStyleClass().add("qty-label");
        Button mas = new Button("+");
        mas.getStyleClass().add("qty-btn");
        mas.setOnAction(e -> cambiarCantidad(item, 1));
        HBox cantidadBox = new HBox(4, menos, cantidad, mas);
        cantidadBox.getStyleClass().add("quantity-control");
        cantidadBox.setAlignment(Pos.CENTER);

        Label importe = new Label(Formato.money(item.importe()));
        importe.getStyleClass().add("cart-item-price");

        Button quitar = new Button();
        quitar.getStyleClass().addAll("btn-icon", "danger");
        FontIcon icono = new FontIcon("mdi2c-close");
        icono.setIconSize(14);
        quitar.setGraphic(icono);
        quitar.setOnAction(e -> {
            carrito.remove(item);
            recalcular();
        });

        fila.getChildren().addAll(info, cantidadBox, importe, quitar);
        return fila;
    }

    // ------------------------------------------------------------------
    // Métricas
    // ------------------------------------------------------------------
    private void cargarMetricas() {
        List<Producto> productos = Servicios.productos().listar();
        BigDecimal valor = productos.stream()
                .map(p -> p.precio().multiply(BigDecimal.valueOf(p.stock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long stockBajo = productos.stream().filter(p -> p.stockBajo()).count();

        lblVentasDia.setText(Formato.money(
                Servicios.reportes().resumenJornada(LocalDate.now()).totalVentas()));
        lblProductos.setText(String.valueOf(productos.size()));
        lblValorInventario.setText(Formato.money(valor));
        lblStockBajo.setText(String.valueOf(stockBajo));
    }

    // ------------------------------------------------------------------
    // Cobro
    // ------------------------------------------------------------------
    @FXML
    private void cobrar() {
        if (carrito.isEmpty()) {
            Dialogos.aviso("Agrega al menos un producto al carrito.");
            return;
        }
        PagoSeleccion pago = pedirPago(totales.total());
        if (pago == null) {
            return;
        }
        try {
            // Sin descuento manual: el del cliente y las promociones los
            // resuelve el servicio. La identidad de la venta sale de la sesión.
            SolicitudVenta solicitud = new SolicitudVenta(
                    List.copyOf(carrito),
                    clienteSeleccionado != null ? clienteSeleccionado.id() : null,
                    clienteSeleccionado != null ? clienteSeleccionado.nombre() : null,
                    BigDecimal.ZERO,
                    pago.metodo(), pago.recibido());
            Venta venta = Servicios.ventas().registrar(solicitud);
            carrito.clear();
            cmbCliente.setValue(PUBLICO_GENERAL);
            recalcular();
            cargarProductos(txtBuscarProducto.getText());
            cargarMetricas();
            mostrarTicket(venta);
        } catch (RuntimeException e) {
            Dialogos.error(e.getMessage());
        }
    }

    private PagoSeleccion pedirPago(BigDecimal total) {
        while (true) {
            Dialog<PagoSeleccion> dialogo = new Dialog<>();
            dialogo.setTitle("Cobrar venta");
            dialogo.setResizable(false);
            if (App.getPrimaryStage() != null) {
                dialogo.initOwner(App.getPrimaryStage());
            }
            DialogPane panel = dialogo.getDialogPane();
            panel.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            Dialogos.aplicarEstilos(panel);

            ComboBox<MetodoPago> metodo = new ComboBox<>(FXCollections.observableArrayList(MetodoPago.values()));
            metodo.setValue(MetodoPago.EFECTIVO);
            metodo.setMaxWidth(Double.MAX_VALUE);
            TextField recibido = new TextField(total.toPlainString());
            recibido.setPromptText("Monto recibido");

            Label cambioTitulo = new Label("Cambio");
            cambioTitulo.getStyleClass().add("ticket-change-caption");
            Label cambioMonto = new Label();
            cambioMonto.getStyleClass().add("ticket-change-amount");
            VBox cambioCard = new VBox(cambioTitulo, cambioMonto);
            cambioCard.getStyleClass().add("ticket-change-card");
            cambioCard.setAlignment(Pos.CENTER);

            Runnable actualizarCambio = () -> {
                BigDecimal r = Formato.decimal(recibido.getText());
                BigDecimal c = r.subtract(total);
                cambioMonto.setText(Formato.money(c.max(BigDecimal.ZERO)));
            };
            recibido.textProperty().addListener((o, a, b) -> actualizarCambio.run());
            metodo.valueProperty().addListener((o, a, b) -> {
                boolean efectivo = b == MetodoPago.EFECTIVO;
                recibido.setDisable(!efectivo);
                if (!efectivo) {
                    recibido.setText(total.toPlainString());
                }
            });
            actualizarCambio.run();

            Label totalTitulo = new Label("TOTAL A PAGAR");
            totalTitulo.getStyleClass().add("ticket-total-caption");
            Label totalMonto = new Label(Formato.money(total));
            totalMonto.getStyleClass().add("ticket-total-amount");
            VBox totalCard = new VBox(totalTitulo, totalMonto);
            totalCard.getStyleClass().add("ticket-total-card");
            totalCard.setAlignment(Pos.CENTER);

            Label lblMetodo = new Label("MÉTODO DE PAGO");
            lblMetodo.getStyleClass().add("dialog-field-label");
            Label lblRecibido = new Label("MONTO RECIBIDO");
            lblRecibido.getStyleClass().add("dialog-field-label");

            VBox contenido = new VBox(12,
                    totalCard,
                    lblMetodo, metodo,
                    lblRecibido, recibido,
                    cambioCard);
            contenido.setPadding(new Insets(8, 4, 4, 4));
            contenido.setMinWidth(360);
            panel.setContent(contenido);

            Button btnOk = (Button) panel.lookupButton(ButtonType.OK);
            btnOk.setText("Confirmar");
            btnOk.getStyleClass().add("btn-primary");
            Button btnCancelar = (Button) panel.lookupButton(ButtonType.CANCEL);
            btnCancelar.setText("Cancelar");
            btnCancelar.getStyleClass().add("btn-secondary");

            dialogo.setResultConverter(boton -> {
                if (boton != ButtonType.OK) {
                    return null;
                }
                MetodoPago m = metodo.getValue();
                BigDecimal r = (m == MetodoPago.EFECTIVO) ? Formato.decimal(recibido.getText()) : total;
                return new PagoSeleccion(m, r);
            });

            Optional<PagoSeleccion> resultado = dialogo.showAndWait();
            if (resultado.isEmpty()) {
                return null;
            }
            PagoSeleccion seleccion = resultado.get();
            if (seleccion.metodo() == MetodoPago.EFECTIVO && seleccion.recibido().compareTo(total) < 0) {
                Dialogos.aviso("El monto recibido es menor al total.");
                continue;
            }
            return seleccion;
        }
    }

    private void mostrarTicket(Venta venta) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Ticket " + venta.folio());
        dialogo.setResizable(true);
        if (App.getPrimaryStage() != null) {
            dialogo.initOwner(App.getPrimaryStage());
        }
        DialogPane panel = dialogo.getDialogPane();
        panel.getButtonTypes().addAll(ButtonType.OK);
        Dialogos.aplicarEstilos(panel);

        panel.setContent(construirTicket(venta));
        panel.getButtonTypes().add(0, ButtonType.APPLY);
        Button btnImprimir = (Button) panel.lookupButton(ButtonType.APPLY);
        btnImprimir.setText("Imprimir");
        btnImprimir.getStyleClass().add("btn-secondary");
        btnImprimir.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            imprimir(venta);
            e.consume();
        });

        dialogo.showAndWait();
    }

    private VBox construirTicket(Venta venta) {
        FontIcon iconoExito = new FontIcon("mdi2c-check-circle");
        iconoExito.setIconSize(26);
        VBox disco = new VBox(iconoExito);
        disco.getStyleClass().add("ticket-check");
        disco.setAlignment(Pos.CENTER);

        Label tituloLabel = new Label("Venta registrada");
        tituloLabel.getStyleClass().add("ticket-title");
        Label folio = new Label("Folio " + venta.folio());
        folio.getStyleClass().add("ticket-folio");
        VBox encabezadoTexto = new VBox(4, tituloLabel, folio);
        HBox encabezado = new HBox(12, disco, encabezadoTexto);
        encabezado.getStyleClass().add("ticket-header");
        encabezado.setAlignment(Pos.CENTER_LEFT);

        Label seccionVenta = new Label("VENTA");
        seccionVenta.getStyleClass().add("ticket-section");
        Label seccionArticulos = new Label("ARTÍCULOS (" + venta.items().size() + ")");
        seccionArticulos.getStyleClass().add("ticket-section");
        Label seccionPago = new Label("PAGO");
        seccionPago.getStyleClass().add("ticket-section");

        VBox ticket = new VBox(8,
                encabezado,
                seccionVenta,
                filaTicket("ticket-meta", "Fecha", venta.fecha().format(FECHA)),
                filaTicket("ticket-meta", "Atendió", venta.usuarioNombre()),
                seccionArticulos);
        for (ItemVenta item : venta.items()) {
            ticket.getChildren().add(filaTicket("ticket-item",
                    item.cantidad() + " × " + item.nombre(), Formato.money(item.importe())));
        }
        ticket.getChildren().addAll(
                seccionPago,
                filaTicket("ticket-total", "Subtotal", Formato.money(venta.subtotal())),
                filaTicket("ticket-total", "Descuento", "-" + Formato.money(venta.descuento())),
                filaTicket("ticket-total", "IVA (16%)", Formato.money(venta.iva())));

        Label totalTitulo = new Label("TOTAL");
        totalTitulo.getStyleClass().add("ticket-total-caption");
        Label totalMonto = new Label(Formato.money(venta.total()));
        totalMonto.getStyleClass().add("ticket-total-amount");
        VBox totalCard = new VBox(totalTitulo, totalMonto);
        totalCard.getStyleClass().add("ticket-total-card");
        totalCard.setAlignment(Pos.CENTER);

        Label cambioTitulo = new Label("Cambio");
        cambioTitulo.getStyleClass().add("ticket-change-caption");
        Label cambioMonto = new Label(Formato.money(venta.cambio()));
        cambioMonto.getStyleClass().add("ticket-change-amount");
        VBox cambioCard = new VBox(cambioTitulo, cambioMonto);
        cambioCard.getStyleClass().add("ticket-change-card");
        cambioCard.setAlignment(Pos.CENTER);

        ticket.getChildren().addAll(
                filaTicket("ticket-meta", "Método", String.valueOf(venta.metodoPago())),
                filaTicket("ticket-meta", "Recibido", Formato.money(venta.montoRecibido())),
                totalCard,
                cambioCard);
        ticket.setPadding(new Insets(8, 4, 4, 4));
        ticket.setMinWidth(420);
        ticket.setAlignment(Pos.TOP_LEFT);
        return ticket;
    }

    /** Fila etiqueta/valor: etiqueta discreta a la izquierda, importe destacado a la derecha. */
    private HBox filaTicket(String clase, String etiqueta, String valor) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add(clase + "-label");
        Label val = new Label(valor);
        val.getStyleClass().add(clase + "-value");
        HBox.setHgrow(val, Priority.ALWAYS);
        val.setMaxWidth(Double.MAX_VALUE);
        val.setAlignment(Pos.CENTER_RIGHT);
        HBox fila = new HBox(lbl, val);
        fila.getStyleClass().add(clase + "-row");
        return fila;
    }

    /** Versión en texto plano para la impresión (sin depender de hojas de estilo). */
    private VBox construirTicketImpresion(Venta venta) {
        VBox ticket = new VBox(4);
        ticket.setPadding(new Insets(12));
        ticket.setAlignment(Pos.TOP_LEFT);
        ticket.getChildren().addAll(
                titulo("NOVEDADES ALE"),
                Componentes.etiqueta("Folio: " + venta.folio()),
                Componentes.etiqueta("Fecha: " + venta.fecha().format(FECHA)),
                Componentes.etiqueta("Atendió: " + venta.usuarioNombre()),
                new javafx.scene.control.Separator());
        for (ItemVenta item : venta.items()) {
            ticket.getChildren().add(Componentes.etiqueta(item.cantidad() + " x " + item.nombre()
                    + "  " + Formato.money(item.importe())));
        }
        ticket.getChildren().addAll(
                new javafx.scene.control.Separator(),
                Componentes.etiqueta("Subtotal: " + Formato.money(venta.subtotal())),
                Componentes.etiqueta("Descuento: " + Formato.money(venta.descuento())),
                Componentes.etiqueta("IVA: " + Formato.money(venta.iva())),
                titulo("TOTAL: " + Formato.money(venta.total())),
                Componentes.etiqueta("Método: " + venta.metodoPago()),
                Componentes.etiqueta("Recibido: " + Formato.money(venta.montoRecibido())),
                titulo("Cambio: " + Formato.money(venta.cambio())));
        return ticket;
    }

    private boolean imprimir(Venta venta) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            Dialogos.aviso("No hay impresora disponible.");
            return false;
        }
        if (!job.showPrintDialog(App.getPrimaryStage())) {
            job.endJob();
            return false;
        }
        try {
            boolean ok = job.printPage(construirTicketImpresion(venta));
            if (ok) {
                job.endJob();
            }
            return ok;
        } catch (RuntimeException e) {
            Dialogos.error("No se pudo imprimir: " + e.getMessage());
            return false;
        }
    }

    private Label titulo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("label-heading");
        return label;
    }

    /** Resultado del diálogo de pago. */
    private record PagoSeleccion(MetodoPago metodo, BigDecimal recibido) {
    }
}
