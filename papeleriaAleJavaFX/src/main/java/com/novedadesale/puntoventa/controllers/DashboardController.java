package com.novedadesale.puntoventa.controllers;

import com.novedadesale.puntoventa.models.ProductoMasVendido;
import com.novedadesale.puntoventa.models.ResumenJornada;
import com.novedadesale.puntoventa.models.Venta;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.shared.Formato;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/** Dashboard de Inicio: métricas del día, más vendidos y ventas recientes. */
public class DashboardController implements Initializable {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private Label lblVentasDia;
    @FXML private Label lblNumeroVentas;
    @FXML private Label lblTicketPromedio;
    @FXML private Label lblStockBajo;
    @FXML private Label lblEfectivo;
    @FXML private Label lblTarjeta;
    @FXML private Label lblTransferencia;

    @FXML private TableView<ProductoMasVendido> tblMasVendidos;
    @FXML private TableColumn<ProductoMasVendido, String> colMvProducto;
    @FXML private TableColumn<ProductoMasVendido, Integer> colMvCantidad;
    @FXML private TableColumn<ProductoMasVendido, BigDecimal> colMvImporte;

    @FXML private TableView<Venta> tblVentas;
    @FXML private TableColumn<Venta, String> colFolio;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, String> colCliente;
    @FXML private TableColumn<Venta, String> colMetodo;
    @FXML private TableColumn<Venta, BigDecimal> colTotal;

    private final ObservableList<ProductoMasVendido> masVendidos = FXCollections.observableArrayList();
    private final ObservableList<Venta> ventas = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarTablas();
        recargar();
    }

    private void configurarTablas() {
        tblMasVendidos.setItems(masVendidos);
        tblMasVendidos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblMasVendidos.setPlaceholder(placeholder("Aún no hay ventas registradas"));

        colMvProducto.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nombre()));
        colMvCantidad.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().cantidad()));
        colMvImporte.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().importe()));
        colMvImporte.setCellFactory(c -> moneyCell());

        tblVentas.setItems(ventas);
        tblVentas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblVentas.setPlaceholder(placeholder("No hay ventas el día de hoy"));

        colFolio.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().folio()));
        colFecha.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().fecha().format(HORA)));
        colCliente.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
                cd.getValue().clienteNombre() == null ? "Público general" : cd.getValue().clienteNombre()));
        colMetodo.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().metodoPago().name()));
        colTotal.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().total()));
        colTotal.setCellFactory(c -> moneyCell());
    }

    @FXML
    private void recargar() {
        LocalDate hoy = LocalDate.now();
        ResumenJornada resumen = Servicios.reportes().resumenJornada(hoy);

        lblVentasDia.setText(Formato.money(resumen.totalVentas()));
        lblNumeroVentas.setText(String.valueOf(resumen.numeroVentas()));
        lblTicketPromedio.setText(Formato.money(resumen.ticketPromedio()));
        lblEfectivo.setText(Formato.money(resumen.totalEfectivo()));
        lblTarjeta.setText(Formato.money(resumen.totalTarjeta()));
        lblTransferencia.setText(Formato.money(resumen.totalTransferencia()));
        lblStockBajo.setText(String.valueOf(Servicios.productos().stockBajo().size()));

        masVendidos.setAll(Servicios.reportes().productosMasVendidos(hoy.minusDays(29), hoy, 8));

        List<Venta> recientes = Servicios.reportes().ventas(hoy, hoy);
        ventas.setAll(recientes.size() > 12 ? recientes.subList(0, 12) : recientes);
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
