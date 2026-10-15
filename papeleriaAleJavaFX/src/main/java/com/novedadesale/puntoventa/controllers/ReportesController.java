package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.App;
import com.novedadesale.puntoventa.models.EstadoVenta;
import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.ProductoMasVendido;
import com.novedadesale.puntoventa.models.Venta;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.shared.Formato;
import com.novedadesale.puntoventa.ui.Componentes;
import com.novedadesale.puntoventa.ui.Dialogos;
import com.novedadesale.puntoventa.ui.Sesion;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/** Reportes: ventas por rango de fechas, más vendidos y corte. */
public class ReportesController implements Initializable {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Label lblTotalVendido;
    @FXML private Label lblTickets;
    @FXML private Label lblTicketPromedio;
    @FXML private Label lblUnidades;
    @FXML private Label lblRango;

    @FXML private TableView<ProductoMasVendido> tblMasVendidos;
    @FXML private TableColumn<ProductoMasVendido, String> colMvCodigo;
    @FXML private TableColumn<ProductoMasVendido, String> colMvProducto;
    @FXML private TableColumn<ProductoMasVendido, Integer> colMvCantidad;
    @FXML private TableColumn<ProductoMasVendido, BigDecimal> colMvImporte;

    @FXML private TableView<Venta> tblVentas;
    @FXML private TableColumn<Venta, String> colFolio;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, String> colCliente;
    @FXML private TableColumn<Venta, String> colMetodo;
    @FXML private TableColumn<Venta, Venta> colEstado;
    @FXML private TableColumn<Venta, BigDecimal> colTotal;
    @FXML private TableColumn<Venta, Venta> colAcciones;

    private final ObservableList<ProductoMasVendido> masVendidos = FXCollections.observableArrayList();
    private final ObservableList<Venta> ventas = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dpDesde.setValue(LocalDate.now().minusDays(29));
        dpHasta.setValue(LocalDate.now());
        configurarTablas();
        generar();
    }

    private void configurarTablas() {
        tblMasVendidos.setItems(masVendidos);
        tblMasVendidos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblMasVendidos.setPlaceholder(Componentes.placeholder("Sin ventas en el periodo seleccionado"));

        colMvCodigo.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().codigo()));
        colMvProducto.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nombre()));
        colMvCantidad.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().cantidad()));
        colMvImporte.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().importe()));
        colMvImporte.setCellFactory(c -> Componentes.moneyCell());

        tblVentas.setItems(ventas);
        tblVentas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblVentas.setPlaceholder(Componentes.placeholder("Sin ventas en el periodo seleccionado"));

        colFolio.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().folio()));
        colFecha.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().fecha().format(FECHA)));
        colCliente.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
                cd.getValue().clienteNombre() == null ? "Público general" : cd.getValue().clienteNombre()));
        colMetodo.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().metodoPago().name()));
        colTotal.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().total()));
        colTotal.setCellFactory(c -> Componentes.moneyCell());

        colEstado.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        colEstado.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Venta venta, boolean empty) {
                super.updateItem(venta, empty);
                if (empty || venta == null) {
                    setGraphic(null);
                    return;
                }
                boolean completada = venta.estado() == EstadoVenta.COMPLETADA;
                Label badge = new Label(completada ? "Completada" : "Cancelada");
                badge.getStyleClass().addAll("badge", completada ? "success" : "danger");
                setGraphic(badge);
            }
        });

        colAcciones.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        colAcciones.setCellFactory(c -> new TableCell<>() {
            private final Button btnCancelar = Componentes.iconButton("mdi2c-close-circle-outline", "Cancelar venta");
            {
                btnCancelar.getStyleClass().add("danger");
                btnCancelar.setOnAction(e -> cancelar(getItem()));
            }
            @Override protected void updateItem(Venta venta, boolean empty) {
                super.updateItem(venta, empty);
                if (empty || venta == null) {
                    setGraphic(null);
                    return;
                }
                boolean puedeCancelar = venta.estado() == EstadoVenta.COMPLETADA && Sesion.esAdmin();
                btnCancelar.setDisable(!puedeCancelar);
                setGraphic(btnCancelar);
            }
        });
        colAcciones.setVisible(Sesion.esAdmin());
    }

    private void cancelar(Venta venta) {
        if (venta == null) {
            return;
        }
        if (!Dialogos.confirmar("¿Cancelar la venta " + venta.folio()
                + "?\nSe repondrá el stock de los productos.")) {
            return;
        }
        try {
            Servicios.ventas().cancelar(venta.id());
            generar();
        } catch (RuntimeException e) {
            Dialogos.error(e.getMessage());
        }
    }

    @FXML
    private void generar() {
        LocalDate inicio = dpDesde.getValue();
        LocalDate fin = dpHasta.getValue();
        if (inicio == null || fin == null) {
            return;
        }
        if (inicio.isAfter(fin)) {
            LocalDate tmp = inicio;
            inicio = fin;
            fin = tmp;
            dpDesde.setValue(inicio);
            dpHasta.setValue(fin);
        }

        List<Venta> resultado = Servicios.reportes().ventas(inicio, fin).stream()
                .sorted((a, b) -> b.fecha().compareTo(a.fecha()))
                .toList();
        ventas.setAll(resultado);

        BigDecimal total = resultado.stream()
                .filter(v -> v.estado() == EstadoVenta.COMPLETADA)
                .map(Venta::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long tickets = resultado.stream().filter(v -> v.estado() == EstadoVenta.COMPLETADA).count();
        long unidades = resultado.stream().filter(v -> v.estado() == EstadoVenta.COMPLETADA)
                .flatMap(v -> v.items().stream())
                .mapToInt(ItemVenta::cantidad).sum();

        lblTotalVendido.setText(Formato.money(total));
        lblTickets.setText(String.valueOf(tickets));
        lblTicketPromedio.setText(Formato.money(tickets == 0 ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(tickets), 2, RoundingMode.HALF_UP)));
        lblUnidades.setText(String.valueOf(unidades));
        lblRango.setText(inicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - "
                + fin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        masVendidos.setAll(Servicios.reportes().productosMasVendidos(inicio, fin, 10));
    }

    @FXML
    private void exportarCsv() {
        if (ventas.isEmpty()) {
            Dialogos.aviso("No hay ventas para exportar en el periodo seleccionado.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar reporte a CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo CSV", "*.csv"));
        chooser.setInitialFileName("reporte_" + LocalDate.now() + ".csv");
        File archivo = chooser.showSaveDialog(App.getPrimaryStage());
        if (archivo == null) {
            return;
        }
        try (Writer writer = Files.newBufferedWriter(archivo.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            writer.write("Folio,Fecha,Cliente,Método,Estado,Total\n");
            for (Venta venta : ventas) {
                writer.write(String.join(",",
                        csv(venta.folio()),
                        csv(venta.fecha().format(FECHA)),
                        csv(venta.clienteNombre() == null ? "Público general" : venta.clienteNombre()),
                        csv(venta.metodoPago().name()),
                        csv(venta.estado() == EstadoVenta.COMPLETADA ? "Completada" : "Cancelada"),
                        csv(Formato.money(venta.total()))));
                writer.write("\n");
            }
            Dialogos.info("Reporte exportado en:\n" + archivo.getAbsolutePath());
        } catch (IOException e) {
            Dialogos.error("No se pudo exportar el reporte: " + e.getMessage());
        }
    }

    private String csv(String valor) {
        String texto = valor == null ? "" : valor;
        if (texto.contains(",") || texto.contains("\"") || texto.contains("\n")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }

    @FXML
    private void imprimir() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            Dialogos.aviso("No hay impresora disponible.");
            return;
        }
        if (!job.showPrintDialog(App.getPrimaryStage())) {
            job.endJob();
            return;
        }
        try {
            if (job.printPage(construirReporte())) {
                job.endJob();
            } else {
                Dialogos.error("No se pudo imprimir el reporte.");
            }
        } catch (RuntimeException e) {
            Dialogos.error("No se pudo imprimir: " + e.getMessage());
        }
    }

    private VBox construirReporte() {
        VBox doc = new VBox(4);
        doc.setPadding(new Insets(24));
        doc.getChildren().addAll(
                titulo("NOVEDADES ALE"),
                Componentes.etiqueta("Reporte de ventas"),
                Componentes.etiqueta("Periodo: " + lblRango.getText()),
                Componentes.etiqueta("Generado: " + LocalDate.now()),
                new Separator(),
                Componentes.etiqueta("Total vendido: " + lblTotalVendido.getText()),
                Componentes.etiqueta("Tickets: " + lblTickets.getText()),
                Componentes.etiqueta("Ticket promedio: " + lblTicketPromedio.getText()),
                Componentes.etiqueta("Productos vendidos: " + lblUnidades.getText()),
                new Separator(),
                titulo("Productos más vendidos"));
        if (masVendidos.isEmpty()) {
            doc.getChildren().add(Componentes.etiqueta("Sin datos"));
        }
        for (ProductoMasVendido producto : masVendidos) {
            doc.getChildren().add(Componentes.etiqueta(producto.cantidad() + " x " + producto.nombre()
                    + "  " + Formato.money(producto.importe())));
        }
        doc.getChildren().addAll(new Separator(), titulo("Detalle de ventas"));
        int limite = Math.min(ventas.size(), 30);
        for (int i = 0; i < limite; i++) {
            Venta venta = ventas.get(i);
            doc.getChildren().add(Componentes.etiqueta(venta.folio() + "  " + venta.fecha().format(FECHA)
                    + "  " + (venta.clienteNombre() == null ? "Público general" : venta.clienteNombre())
                    + "  " + Formato.money(venta.total())));
        }
        if (ventas.size() > limite) {
            doc.getChildren().add(Componentes.etiqueta("... y " + (ventas.size() - limite) + " ventas más"));
        }
        return doc;
    }

    private Label titulo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("label-heading");
        return label;
    }
}
