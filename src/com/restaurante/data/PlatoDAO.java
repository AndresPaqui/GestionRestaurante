package com.restaurante.data;

import com.restaurante.model.Plato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlatoDAO {

    public boolean insertar(Plato p) {
        String sql = "INSERT INTO platos (nombre, precio_venta) VALUES (?, ?)";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecioVenta());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) p.setId(keys.getInt(1));  // ← setId()
            return true;

        } catch (SQLException e) {
            System.err.println("[PlatoDAO] insertar: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Plato p) {
        String sql = "UPDATE platos SET nombre = ?, precio_venta = ? WHERE id = ?";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecioVenta());
            ps.setInt(3, p.getId());                   // ← getId()
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PlatoDAO] actualizar: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM platos WHERE id = ?";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PlatoDAO] eliminar: " + e.getMessage());
            return false;
        }
    }

    public List<Plato> listarTodos() {
        List<Plato> lista = new ArrayList<>();
        String sql = "SELECT * FROM platos ORDER BY nombre";
        try (Statement st = Conexion.conectar().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("[PlatoDAO] listarTodos: " + e.getMessage());
        }
        return lista;
    }


    private Plato mapear(ResultSet rs) throws SQLException {

        return new Plato(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getDouble("precio_venta"),
                0.0,
                null
        );
    }
}