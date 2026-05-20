package com.restaurante.data;

import com.restaurante.model.Insumo;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class RecetaDAO {

    public boolean guardarIngrediente(int idPlato, int idInsumo, double cant) {
        String sql = """
                INSERT INTO recetas (id_plato, id_insumo, cantidad_necesaria)
                VALUES (?, ?, ?)
                ON CONFLICT(id_plato, id_insumo)
                DO UPDATE SET cantidad_necesaria = excluded.cantidad_necesaria
                """;
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idPlato);
            ps.setInt(2, idInsumo);
            ps.setDouble(3, cant);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RecetaDAO] guardarIngrediente: " + e.getMessage());
            return false;
        }
    }

    public Map<Insumo, Double> obtenerIngredientesPorPlato(int idPlato) {
        Map<Insumo, Double> mapa = new HashMap<>();
        String sql = """
                SELECT i.id, i.nombre, i.categoria,
                       i.stock_actual, i.stock_minimo, i.costo_unitario, i.unidad_medida,
                       r.cantidad_necesaria
                FROM recetas r
                JOIN insumos i ON r.id_insumo = i.id
                WHERE r.id_plato = ?
                """;
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idPlato);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Insumo insumo = new Insumo(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getDouble("stock_actual"),
                        rs.getDouble("stock_minimo"),
                        rs.getDouble("costo_unitario"),
                        rs.getString("unidad_medida")   // ← unidad_medida
                );
                mapa.put(insumo, rs.getDouble("cantidad_necesaria"));
            }
        } catch (SQLException e) {
            System.err.println("[RecetaDAO] obtenerIngredientesPorPlato: " + e.getMessage());
        }
        return mapa;
    }

    public boolean eliminarIngredientesDePlato(int idPlato) {
        String sql = "DELETE FROM recetas WHERE id_plato = ?";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idPlato);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RecetaDAO] eliminarIngredientesDePlato: " + e.getMessage());
            return false;
        }
    }
}