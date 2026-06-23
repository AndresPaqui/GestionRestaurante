package com.restaurante.data;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataInjector {

    public static void inyectarDatosDemostrativos() {
        try (Connection conn = Conexion.conectar();
             Statement st = conn.createStatement()) {

            System.out.println("[Inyector] Iniciando carga de datos reales...");

            // USAMOS "INSERT OR IGNORE" para evitar el error de UNIQUE constraint fallido
            // =========================================================================
            // 1. INYECCIÓN DE 25 INSUMOS
            // =========================================================================
            String[] insumosSQL = {
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (1, 'Lomo de Res', 'Carnes', 45.0, 10.0, 6.50, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (2, 'Pechuga de Pollo', 'Carnes', 60.0, 12.0, 4.20, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (3, 'Camarón Pomada', 'Carnes', 25.0, 5.0, 8.00, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (4, 'Pescado Picudo', 'Carnes', 30.0, 6.0, 7.50, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (5, 'Chuleta de Cerdo', 'Carnes', 35.0, 8.0, 4.80, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (6, 'Papa Chola', 'Verduras/Frutas', 150.0, 40.0, 0.45, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (7, 'Cebolla Paiteña', 'Verduras/Frutas', 80.0, 20.0, 0.60, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (8, 'Tomate Riñón', 'Verduras/Frutas', 70.0, 15.0, 0.55, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (9, 'Aguacate', 'Verduras/Frutas', 40.0, 10.0, 1.20, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (10, 'Lechuga Crespa', 'Verduras/Frutas', 50.0, 15.0, 0.35, 'unidad')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (11, 'Plátano Verde', 'Verduras/Frutas', 120.0, 30.0, 0.15, 'unidad')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (12, 'Plátano Maduro', 'Verduras/Frutas', 90.0, 25.0, 0.12, 'unidad')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (13, 'Queso Criollo', 'Lácteos', 25.0, 5.0, 3.80, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (14, 'Leche Entera', 'Lácteos', 40.0, 10.0, 0.95, 'lt')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (15, 'Crema de Leche', 'Lácteos', 15.0, 4.0, 2.50, 'lt')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (16, 'Arroz Súper Extra', 'Abarrotes', 200.0, 50.0, 0.65, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (17, 'Aceite Girasol', 'Abarrotes', 50.0, 12.0, 2.10, 'lt')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (18, 'Harina de Trigo', 'Abarrotes', 40.0, 10.0, 0.80, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (19, 'Mani Molido', 'Abarrotes', 20.0, 5.0, 3.20, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (20, 'Huevos', 'Otros', 150.0, 30.0, 0.12, 'unidad')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (21, 'Corvina', 'Carnes', 20.0, 5.0, 6.80, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (22, 'Mote Cocinado', 'Abarrotes', 35.0, 8.0, 1.10, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (23, 'Ajo Procesado', 'Abarrotes', 10.0, 2.0, 4.50, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (24, 'Cilantro/Hierbita', 'Verduras/Frutas', 15.0, 3.0, 0.80, 'kg')",
                    "INSERT OR IGNORE INTO insumos (id, nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida) VALUES (25, 'Limón Sutil', 'Verduras/Frutas', 30.0, 8.0, 1.50, 'kg')"
            };
            for (String sql : insumosSQL) st.addBatch(sql);

            // =========================================================================
            // 2. INYECCIÓN DE 15 PLATOS REALES
            // =========================================================================
            String[] platosSQL = {
                    "INSERT OR IGNORE INTO platos (id, nombre, precio_venta) VALUES (1, 'Churrasco Ecuatoriano', 6.50)",
                    "INSERT OR IGNORE INTO platos (id, nombre, precio_venta) VALUES (2, 'Seco de Pollo Clásico', 4.50)",
                    "INSERT OR IGNORE INTO platos (id, nombre, precio_venta) VALUES (3, 'Ceviche de Camarón', 8.50)",
                    "INSERT OR IGNORE INTO platos (id, nombre, precio_venta) VALUES (4, 'Locro de Papa con Queso', 3.75)",
                    "INSERT OR IGNORE INTO platos (id, nombre, precio_venta) VALUES (5, 'Lomo Fino Salteado', 9.00)",
                    "INSERT OR IGNORE INTO platos (id, nombre, precio_venta) VALUES (6, 'Corvina Frita Extravaganza', 7.50)",
                    "INSERT OR IGNORE INTO platos (id, nombre, precio_venta) VALUES (7, 'Tigrillo con Huevo y Queso', 3.50)",
                    "INSERT OR IGNORE INTO platos (id, nombre, precio_venta) VALUES (8, 'Encebollado de Pescado', 5.00)",
                    "INSERT INTO platos (id, nombre, precio_venta) VALUES (9, 'Caldo de Gallina Criolla', 4.00) ON CONFLICT(id) DO NOTHING",
                    "INSERT INTO platos (id, nombre, precio_venta) VALUES (10, 'Arroz con Chuleta', 5.50) ON CONFLICT(id) DO NOTHING",
                    "INSERT INTO platos (id, nombre, precio_venta) VALUES (11, 'Bolón de Queso Gigante', 2.75) ON CONFLICT(id) DO NOTHING",
                    "INSERT INTO platos (id, nombre, precio_venta) VALUES (12, 'Tallarín de Pollo Casero', 4.50) ON CONFLICT(id) DO NOTHING",
                    "INSERT INTO platos (id, nombre, precio_venta) VALUES (13, 'Camarones al Ajillo', 9.50) ON CONFLICT(id) DO NOTHING",
                    "INSERT INTO platos (id, nombre, precio_venta) VALUES (14, 'Mote Pillo Tradicional', 3.00) ON CONFLICT(id) DO NOTHING",
                    "INSERT INTO platos (id, nombre, precio_venta) VALUES (15, 'Arroz Marinero Especial', 11.00) ON CONFLICT(id) DO NOTHING"
            };
            for (String sql : platosSQL) st.addBatch(sql);

            // =========================================================================
            // 3. ENLACE DE RECETAS
            // =========================================================================
            String[] recetasSQL = {
                    "INSERT OR IGNORE INTO recetas VALUES (1, 1, 0.20), (1, 16, 0.15), (1, 6, 0.25), (1, 20, 2.0)",
                    "INSERT OR IGNORE INTO recetas VALUES (2, 2, 0.25), (2, 16, 0.15), (2, 7, 0.10), (2, 8, 0.08)",
                    "INSERT OR IGNORE INTO recetas VALUES (3, 3, 0.20), (3, 7, 0.12), (3, 8, 0.10), (3, 25, 0.05)",
                    "INSERT OR IGNORE INTO recetas VALUES (4, 6, 0.40), (4, 13, 0.08), (4, 14, 0.15), (4, 9, 0.5)",
                    "INSERT OR IGNORE INTO recetas VALUES (5, 1, 0.22), (5, 6, 0.20), (5, 7, 0.08), (5, 8, 0.08)",
                    "INSERT OR IGNORE INTO recetas VALUES (6, 21, 0.25), (6, 16, 0.15), (6, 10, 1.0), (6, 17, 0.05)",
                    "INSERT OR IGNORE INTO recetas VALUES (7, 11, 2.0), (7, 20, 1.0), (7, 13, 0.05), (7, 17, 0.02)",
                    "INSERT OR IGNORE INTO recetas VALUES (8, 4, 0.18), (8, 6, 0.20), (8, 7, 0.15), (8, 24, 0.02)",
                    "INSERT OR IGNORE INTO recetas VALUES (9, 2, 0.25), (9, 6, 0.15), (9, 16, 0.05), (9, 20, 1.0)",
                    "INSERT OR IGNORE INTO recetas VALUES (10, 5, 0.22), (10, 16, 0.18), (10, 17, 0.03)",
                    "INSERT OR IGNORE INTO recetas VALUES (11, 11, 3.0), (11, 13, 0.08), (11, 17, 0.04)",
                    "INSERT OR IGNORE INTO recetas VALUES (12, 2, 0.20), (12, 7, 0.08), (12, 8, 0.08)",
                    "INSERT OR IGNORE INTO recetas VALUES (13, 3, 0.22), (13, 23, 0.02), (13, 15, 0.10)",
                    "INSERT OR IGNORE INTO recetas VALUES (14, 22, 0.25), (14, 20, 2.0), (14, 14, 0.05)",
                    "INSERT OR IGNORE INTO recetas VALUES (15, 3, 0.15), (15, 4, 0.10), (15, 16, 0.20)"
            };
            for (String sql : recetasSQL) st.addBatch(sql);

            st.executeBatch();

            // =========================================================================
            // 4. HISTORIAL DE VENTAS (Cabeceras mapeadas por columna)
            // =========================================================================
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime ahora = LocalDateTime.now();

            String v1 = String.format("INSERT OR IGNORE INTO ventas (id, fecha, total_venta, metodo_pago, cliente_nombre, cliente_cedula) VALUES (1, '%s', 24.15, 'Efectivo', 'Carlos Ortiz', '1723456789')", ahora.minusHours(5).format(fmt));
            String v2 = String.format("INSERT OR IGNORE INTO ventas (id, fecha, total_venta, metodo_pago, cliente_nombre, cliente_cedula) VALUES (2, '%s', 15.53, 'Tarjeta de Crédito', 'Ana Galarza', '0912837465')", ahora.minusHours(3).format(fmt));
            String v3 = String.format("INSERT OR IGNORE INTO ventas (id, fecha, total_venta, metodo_pago, cliente_nombre, cliente_cedula) VALUES (3, '%s', 46.58, 'Transferencia', 'Estudio Jurídico', '1792244556001')", ahora.minusHours(2).format(fmt));
            String v4 = String.format("INSERT OR IGNORE INTO ventas (id, fecha, total_venta, metodo_pago, cliente_nombre, cliente_cedula) VALUES (4, '%s', 10.35, 'Efectivo', 'Consumidor Final', '9999999999')", ahora.minusMinutes(30).format(fmt));

            st.addBatch(v1); st.addBatch(v2); st.addBatch(v3); st.addBatch(v4);
            st.executeBatch();

            // =========================================================================
            // CORRECCIÓN DETALLE_VENTAS: Especificamos columnas exactas de tu tabla
            // =========================================================================
            st.addBatch("INSERT OR IGNORE INTO detalle_ventas (id_venta, id_plato, cantidad, subtotal) VALUES (1, 1, 3, 19.50)");
            st.addBatch("INSERT OR IGNORE INTO detalle_ventas (id_venta, id_plato, cantidad, subtotal) VALUES (2, 4, 3, 11.25)");
            st.addBatch("INSERT OR IGNORE INTO detalle_ventas (id_venta, id_plato, cantidad, subtotal) VALUES (3, 3, 4, 34.00)");
            st.addBatch("INSERT OR IGNORE INTO detalle_ventas (id_venta, id_plato, cantidad, subtotal) VALUES (3, 7, 2, 7.00)");
            st.addBatch("INSERT OR IGNORE INTO detalle_ventas (id_venta, id_plato, cantidad, subtotal) VALUES (4, 8, 2, 10.00)");

            st.executeBatch();
            System.out.println("[Inyector] ¡Base de datos poblada de forma segura al 100%!");

        } catch (SQLException e) {
            System.err.println("[Inyector] Error crítico cargando la semilla: " + e.getMessage());
        }
    }
}