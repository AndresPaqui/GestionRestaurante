package com.restaurante.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexion {

    private static final String URL = "jdbc:sqlite:restaurante.db";
    private static Connection instancia = null;

    private Conexion() {}

    /**
     * Devuelve la conexión activa. La crea la primera vez o si fue cerrada.
     */
    public static Connection conectar() {
        try {
            if (instancia == null || instancia.isClosed()) {
                instancia = DriverManager.getConnection(URL);
                instancia.setAutoCommit(true);
                crearTablas();
            }
        } catch (SQLException e) {
            System.err.println("[Conexion] Error al conectar: " + e.getMessage());
        }
        return instancia;
    }

    public static void cerrarConexion() {
        try {
            if (instancia != null && !instancia.isClosed()) {
                instancia.close();
            }
        } catch (SQLException e) {
            System.err.println("[Conexion] Error al cerrar: " + e.getMessage());
        }
    }

    // ── Crea las tablas si no existen 

    private static void crearTablas() {
        String pragma = "PRAGMA foreign_keys = ON;";

        String insumos = """
                CREATE TABLE IF NOT EXISTS insumos (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre         TEXT    NOT NULL,
                    categoria      TEXT    NOT NULL,
                    stock_actual   REAL    NOT NULL DEFAULT 0,
                    stock_minimo   REAL    NOT NULL DEFAULT 0,
                    costo_unitario REAL    NOT NULL DEFAULT 0,
                    unidad_medida  TEXT    NOT NULL
                );""";

        String platos = """
                CREATE TABLE IF NOT EXISTS platos (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre       TEXT    NOT NULL,
                    precio_venta REAL    NOT NULL DEFAULT 0
                );""";

        String recetas = """
                CREATE TABLE IF NOT EXISTS recetas (
                    id_plato           INTEGER NOT NULL,
                    id_insumo          INTEGER NOT NULL,
                    cantidad_necesaria REAL    NOT NULL,
                    PRIMARY KEY (id_plato, id_insumo),
                    FOREIGN KEY (id_plato)  REFERENCES platos(id)  ON DELETE CASCADE,
                    FOREIGN KEY (id_insumo) REFERENCES insumos(id) ON DELETE CASCADE
                );""";

        String ventas = """
                CREATE TABLE IF NOT EXISTS ventas (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    fecha       TEXT    NOT NULL,
                    total_venta REAL    NOT NULL DEFAULT 0,
                    metodo_pago TEXT    NOT NULL
                );""";

        String detalleVentas = """
                CREATE TABLE IF NOT EXISTS detalle_ventas (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_venta      INTEGER NOT NULL,
                    id_plato      INTEGER NOT NULL,
                    cantidad      INTEGER NOT NULL DEFAULT 1,
                    subtotal      REAL    NOT NULL,
                    FOREIGN KEY (id_venta) REFERENCES ventas(id) ON DELETE CASCADE,
                    FOREIGN KEY (id_plato) REFERENCES platos(id)
                );""";

        try (Statement st = instancia.createStatement()) {
            st.execute(pragma);
            st.execute(insumos);
            st.execute(platos);
            st.execute(recetas);
            st.execute(ventas);
            st.execute(detalleVentas);
        } catch (SQLException e) {
            System.err.println("[Conexion] Error al crear tablas: " + e.getMessage());
        }
    }
}
