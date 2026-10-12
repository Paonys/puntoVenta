package com.novedadesale.puntoventa.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.util.Duration;

import java.util.Locale;

/**
 * Micro-interacciones de estilo macOS para JavaFX.
 *
 * - Hover: escala con interpolador "spring" (overshoot), como los controles de Apple.
 * - Presionado: compresión rápida 0.97.
 * - Tarjetas: además de escala, se elevan (-3px) al pasar el mouse.
 * - Entrada de vista: fade + slide hacia arriba.
 */
public final class Animations {

    private static final Interpolator SPRING = Interpolator.SPLINE(0.2, 0.9, 0.3, 1);

    private Animations() {
    }

    /** Recorre el árbol y adjunta hover/press a los nodos con styleClass animable. */
    public static void enableMicroInteractions(Parent root) {
        applyToTree(root);
    }

    /** Entrada de página: fade 0→1 + slide 12px→0. */
    public static void viewEntrance(Parent node) {
        node.setOpacity(0);
        node.setTranslateY(12);
        FadeTransition fade = new FadeTransition(Duration.millis(320), node);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);
        TranslateTransition slide = new TranslateTransition(Duration.millis(320), node);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fade, slide).play();
    }

    /** Pulso rápido para feedback de clicks sin hover (p. ej. menú de usuario). */
    public static void pressPulse(Node node) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(140), node);
        pulse.setToX(0.94);
        pulse.setToY(0.94);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.setInterpolator(Interpolator.EASE_OUT);
        pulse.play();
    }

    private static void applyToTree(Node node) {
        for (String styleClass : node.getStyleClass()) {
            String cls = styleClass.toLowerCase(Locale.ROOT);
            double scale = hoverScaleFor(cls);
            if (scale > 0) {
                attachHover(node, scale, liftFor(cls));
                break;
            }
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                applyToTree(child);
            }
        }
    }

    private static double hoverScaleFor(String cls) {
        switch (cls) {
            case "product-card":
            case "card-hover":
                return 1.02;
            case "btn-primary":
            case "btn-secondary":
            case "btn-danger":
            case "btn-icon":
            case "qty-btn":
            case "chip":
            case "user-menu":
            case "user-avatar":
            case "button-hover":
                return 1.03;
            default:
                return 0;
        }
    }

    private static double liftFor(String cls) {
        return "product-card".equals(cls) || "card-hover".equals(cls) ? -3 : 0;
    }

    private static void attachHover(Node node, double hoverScale, double lift) {
        double baseScaleX = node.getScaleX();
        double baseScaleY = node.getScaleY();
        double baseY = node.getTranslateY();

        ScaleTransition enter = scaleTransition(node, baseScaleX * hoverScale, baseScaleY * hoverScale, 150, SPRING);
        ScaleTransition exit = scaleTransition(node, baseScaleX, baseScaleY, 220, Interpolator.EASE_OUT);
        ScaleTransition press = scaleTransition(node, baseScaleX * 0.97, baseScaleY * 0.97, 90, Interpolator.EASE_IN);
        ScaleTransition release = scaleTransition(node, baseScaleX * hoverScale, baseScaleY * hoverScale, 180, SPRING);

        TranslateTransition liftIn = lift == 0 ? null : translateTransition(node, baseY + lift, 150, SPRING);
        TranslateTransition liftOut = lift == 0 ? null : translateTransition(node, baseY, 220, Interpolator.EASE_OUT);

        node.setOnMouseEntered(e -> {
            if (node.isDisabled()) {
                return;
            }
            stopAll(exit, press);
            enter.playFromStart();
            if (liftIn != null) {
                liftIn.playFromStart();
            }
        });
        node.setOnMouseExited(e -> {
            stopAll(enter, press);
            exit.playFromStart();
            if (liftOut != null) {
                liftOut.playFromStart();
            }
        });
        node.setOnMousePressed(e -> {
            if (node.isDisabled()) {
                return;
            }
            stopAll(enter, exit);
            press.playFromStart();
        });
        node.setOnMouseReleased(e -> {
            stopAll(press);
            if (node.isHover()) {
                release.playFromStart();
            } else {
                exit.playFromStart();
            }
        });
    }

    private static ScaleTransition scaleTransition(Node node, double toX, double toY, long ms, Interpolator ip) {
        ScaleTransition t = new ScaleTransition(Duration.millis(ms), node);
        t.setToX(toX);
        t.setToY(toY);
        t.setInterpolator(ip);
        return t;
    }

    private static TranslateTransition translateTransition(Node node, double toY, long ms, Interpolator ip) {
        TranslateTransition t = new TranslateTransition(Duration.millis(ms), node);
        t.setToY(toY);
        t.setInterpolator(ip);
        return t;
    }

    private static void stopAll(javafx.animation.Animation... animations) {
        for (javafx.animation.Animation a : animations) {
            if (a != null) {
                a.stop();
            }
        }
    }
}