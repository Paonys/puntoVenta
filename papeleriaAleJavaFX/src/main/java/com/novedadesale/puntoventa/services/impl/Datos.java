package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.models.Categoria;
import com.novedadesale.puntoventa.models.Cliente;
import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.models.Promocion;
import com.novedadesale.puntoventa.models.Proveedor;
import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.models.TipoPromocion;
import com.novedadesale.puntoventa.models.Usuario;
import com.novedadesale.puntoventa.models.Venta;
import com.novedadesale.puntoventa.models.Pago;
import com.novedadesale.puntoventa.shared.Seguridad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Almacén de datos en memoria con datos de ejemplo (seed).
 * <p>
 * TEMPORAL: será reemplazado por la capa de persistencia SQLite + repositorios
 * (siguiente tarea del Agente A). El contrato de {@code services} no cambia.
 */
final class Datos {

    private static final Datos INSTANCIA = new Datos();

    final Map<Integer, Categoria> categorias = new LinkedHashMap<>();
    final Map<Integer, Producto> productos = new LinkedHashMap<>();
    final Map<Integer, Cliente> clientes = new LinkedHashMap<>();
    final Map<Integer, Proveedor> proveedores = new LinkedHashMap<>();
    final Map<Integer, Usuario> usuarios = new LinkedHashMap<>();
    final Map<Integer, Promocion> promociones = new LinkedHashMap<>();
    final List<Venta> ventas = new ArrayList<>();
    final List<Pago> pagos = new ArrayList<>();

    final AtomicInteger catSeq = new AtomicInteger();
    final AtomicInteger prodSeq = new AtomicInteger();
    final AtomicInteger cliSeq = new AtomicInteger();
    final AtomicInteger provSeq = new AtomicInteger();
    final AtomicInteger userSeq = new AtomicInteger();
    final AtomicInteger promoSeq = new AtomicInteger();
    final AtomicInteger ventaSeq = new AtomicInteger();
    final AtomicInteger pagoSeq = new AtomicInteger();

    private Datos() {
        seed();
    }

    static Datos get() {
        return INSTANCIA;
    }

    private void seed() {
        cat("Escolares");
        cat("Oficina");
        cat("Regalos");
        cat("Arte");

        user("Administrador", "admin", "admin123", Rol.ADMIN);
        user("Caja 1", "caja1", "caja123", Rol.CAJERO);

        prod("P-001", "Cuaderno espiral 100 hojas", 1, "25.50", 48);
        prod("P-002", "Plumas de gel (caja 12)", 2, "89.90", 8);
        prod("P-003", "Peluche oso mediano", 3, "149.00", 12);
        prod("P-004", "Lápices de colores 24 piezas", 4, "79.00", 0);
        prod("P-005", "Mochila escolar", 1, "399.00", 6);
        prod("P-006", "Taza personalizada", 3, "120.00", 20);
        prod("P-007", "Marcadores permanentes", 2, "45.00", 35);
        prod("P-008", "Block de dibujo profesional", 4, "55.00", 9);

        cli("María López", "555-100-2000", "maria@example.com", "5");
        cli("Juan Pérez", "555-300-4000", "juan@example.com", "0");

        prov("Papelera del Norte", "Rosa Martínez", "555-111-2222", "ventas@papelnorte.mx");
        prov("Distribuidora Escolar SA", "Luis Gómez", "555-333-4444", "contacto@descescolar.mx");

        promo("Regreso a clases -10%", TipoPromocion.PORCENTAJE, "10",
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(25), 1);
    }

    private void cat(String nombre) {
        int id = catSeq.incrementAndGet();
        categorias.put(id, new Categoria(id, nombre));
    }

    private void user(String nombre, String usuario, String password, Rol rol) {
        int id = userSeq.incrementAndGet();
        usuarios.put(id, new Usuario(id, nombre, usuario, Seguridad.hash(password), rol, true));
    }

    private void prod(String codigo, String nombre, int categoriaId, String precio, int stock) {
        int id = prodSeq.incrementAndGet();
        Categoria cat = categorias.get(categoriaId);
        productos.put(id, new Producto(id, codigo, nombre, categoriaId,
                cat == null ? "" : cat.nombre(), new BigDecimal(precio), stock, 10));
    }

    private void cli(String nombre, String telefono, String email, String descuento) {
        int id = cliSeq.incrementAndGet();
        clientes.put(id, new Cliente(id, nombre, telefono, email, new BigDecimal(descuento)));
    }

    private void prov(String nombre, String contacto, String telefono, String email) {
        int id = provSeq.incrementAndGet();
        proveedores.put(id, new Proveedor(id, nombre, contacto, telefono, email));
    }

    private void promo(String nombre, TipoPromocion tipo, String valor,
                       LocalDate inicio, LocalDate fin, Integer categoriaId) {
        int id = promoSeq.incrementAndGet();
        promociones.put(id, new Promocion(id, nombre, tipo, new BigDecimal(valor), inicio, fin, true, categoriaId));
    }
}
