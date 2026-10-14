package com.novedadesale.puntoventa.ui;

import com.novedadesale.puntoventa.shared.Formato;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;

/**
 * Helpers reutilizables de botones, etiquetas y celdas de tabla.
 *
 * <p>Concentran los patrones que antes estaban duplicados en cada controlador
 * (iconos de acción, etiquetas de formulario, celdas de dinero/número y
 * placeholders de tablas vacías).</p>
 */
public final class Componentes {

    private Componentes() {
    }

    /** Botón tipo icono ({@code .btn-icon}) con tooltip. */
    public static Button iconButton(String iconLiteral, String tooltip) {
        Button boton = new Button();
        boton.getStyleClass().add("btn-icon");
        FontIcon icono = new FontIcon(iconLiteral);
        icono.setIconSize(16);
        boton.setGraphic(icono);
        boton.setTooltip(new Tooltip(tooltip));
        return boton;
    }

    /** Etiqueta de formulario ({@code .label-body}). */
    public static Label etiqueta(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("label-body");
        return label;
    }

    /** Etiqueta neutra para el placeholder de una tabla vacía ({@code .text-tertiary}). */
    public static Label placeholder(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("text-tertiary");
        return label;
    }

    /** Celda decimal alineada a la derecha, formateada como dinero. */
    public static <T> TableCell<T, BigDecimal> moneyCell() {
        return new TableCell<>() {
            {
                getStyleClass().add("num-cell");
            }
            @Override protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : Formato.money(valor));
            }
        };
    }

    /** Celda entera alineada a la derecha. */
    public static <T> TableCell<T, Integer> numberCell() {
        return new TableCell<>() {
            {
                getStyleClass().add("num-cell");
            }
            @Override protected void updateItem(Integer valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : String.valueOf(valor));
            }
        };
    }

    /** Texto de un campo, sin espacios sobrantes. */
    public static String texto(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    /** Devuelve cadena vacía si el valor es nulo. */
    public static String nulo(String valor) {
        return valor == null ? "" : valor;
    }
}
