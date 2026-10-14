package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.models.EstadoVenta;
import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.ProductoMasVendido;
import com.novedadesale.puntoventa.models.Venta;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.shared.Formato;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
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
        tblMasVendidos.setPlaceholder(placeholder("Sin ventas en el periodo seleccionado"));

        colMvCodigo.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().codigo()));
        colMvProducto.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nombre()));
        colMvCantidad.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().cantidad()));
        colMvImporte.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().importe()));
        colMvImporte.setCellFactory(c -> moneyCell());

        tblVentas.setItems(ventas);
        tblVentas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblVentas.setPlaceholder(placeholder("Sin ventas en el periodo seleccionado"));

        colFolio.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().folio()));
        colFecha.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().fecha().format(FECHA)));
        colCliente.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
                cd.getValue().clienteNombre() == null ? "Público general" : cd.getValue().clienteNombre()));
        colMetodo.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().metodoPago().name()));
        colTotal.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().total()));
        colTotal.setCellFactory(c -> moneyCell());

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

    private Label placeholder(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("text-tertiary");
        return label;
    }

    private <T> TableCell<T, BigDecimal> moneyCell() {
        return new TableCell<>() {
            {
                getStyleClass().add("num-cell");
            }
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : Formato.money(valor));
            }
        };
    }
}
