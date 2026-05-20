package com.restaurante.data;

import com.restaurante.model.ItemVenta;
import com.restaurante.model.Venta;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VentasDAO {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean registrarVenta(Venta v) {
        String sql = """
                INSERT INTO ventas (fecha, total_venta, metodo_pago)
                VALUES (?, ?, ?)
                """;
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            // Usa LocalDateTime igual que tu modelo Venta
            LocalDateTime fecha = v.getFecha() != null ? v.getFecha() : LocalDateTime.now();
            ps.setString(1, fecha.format(FMT));
            ps.setDouble(2, v.getTotalVenta());
            ps.setString(3, v.getMetodoPago());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) v.setIdVenta(keys.getInt(1));
            return true;

        } catch (SQLException e) {
            System.err.println("[VentasDAO] registrarVenta: " + e.getMessage());
            return false;
        }
    }

    public boolean registrarDetalleVenta(ItemVenta item, int idVenta) {
        String sql = """
                INSERT INTO detalle_ventas (id_venta, id_plato, cantidad, subtotal)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.setInt(2, item.getPlato().getId());     // ← getId() de Plato
            ps.setInt(3, item.getCantidad());
            ps.setDouble(4, item.getSubtotal());       // ← no hay precioUnitario en ItemVenta
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[VentasDAO] registrarDetalleVenta: " + e.getMessage());
            return false;
        }
    }

    public List<Venta> listarHistorial() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM ventas ORDER BY fecha DESC";
        try (Statement st = Conexion.conectar().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                LocalDateTime fecha = LocalDateTime.parse(rs.getString("fecha"), FMT);
                lista.add(new Venta(
                        rs.getInt("id"),
                        fecha,
                        new ArrayList<>(),              // detalles vacíos en el historial
                        rs.getDouble("total_venta"),
                        rs.getString("metodo_pago")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[VentasDAO] listarHistorial: " + e.getMessage());
        }
        return lista;
    }
}