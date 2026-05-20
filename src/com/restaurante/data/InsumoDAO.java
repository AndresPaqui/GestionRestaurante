package com.restaurante.data;

import com.restaurante.model.Insumo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InsumoDAO {

    public boolean insertar(Insumo i) {
        String sql = """
                INSERT INTO insumos (nombre, categoria, stock_actual, stock_minimo, costo_unitario, unidad_medida)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, i.getNombre());
            ps.setString(2, i.getCategoria());
            ps.setDouble(3, i.getStockActual());
            ps.setDouble(4, i.getStockMinimo());
            ps.setDouble(5, i.getCostoUnitario());
            ps.setString(6, i.getUnidadMedida());      
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) i.setId(keys.getInt(1));
            return true;

        } catch (SQLException e) {
            System.err.println("[InsumoDAO] insertar: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Insumo i) {
        String sql = """
                UPDATE insumos
                SET nombre = ?, categoria = ?, stock_actual = ?,
                    stock_minimo = ?, costo_unitario = ?, unidad_medida = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, i.getNombre());
            ps.setString(2, i.getCategoria());
            ps.setDouble(3, i.getStockActual());
            ps.setDouble(4, i.getStockMinimo());
            ps.setDouble(5, i.getCostoUnitario());
            ps.setString(6, i.getUnidadMedida());      
            ps.setInt(7, i.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[InsumoDAO] actualizar: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM insumos WHERE id = ?";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[InsumoDAO] eliminar: " + e.getMessage());
            return false;
        }
    }

    public List<Insumo> listarTodos() {
        List<Insumo> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumos ORDER BY nombre";
        try (Statement st = Conexion.conectar().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("[InsumoDAO] listarTodos: " + e.getMessage());
        }
        return lista;
    }

    public Insumo buscarPorId(int id) {
        String sql = "SELECT * FROM insumos WHERE id = ?";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.err.println("[InsumoDAO] buscarPorId: " + e.getMessage());
        }
        return null;
    }

    private Insumo mapear(ResultSet rs) throws SQLException {
        return new Insumo(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("categoria"),
                rs.getDouble("stock_actual"),
                rs.getDouble("stock_minimo"),
                rs.getDouble("costo_unitario"),
                rs.getString("unidad_medida")         
        );
    }
}
