package com.restaurante.logic;

import com.restaurante.data.InsumoDAO;
import com.restaurante.model.Insumo;

import java.util.ArrayList;
import java.util.List;

public class InventarioService {

    // El Service necesita hablar con el DAO de Joel
    private final InsumoDAO insumoDAO;

    public InventarioService() {
        this.insumoDAO = new InsumoDAO();
    }

    /**
     * Valida las reglas de negocio antes de guardar un nuevo insumo.
     */
    public boolean crearInsumo(Insumo insumo) {
        // Regla 1: El nombre no puede estar vacío
        if (insumo.getNombre() == null || insumo.getNombre().trim().isEmpty()) {
            System.err.println("[Lógica] Error: El nombre del insumo no puede estar vacío.");
            return false;
        }

        // Regla 2: Los valores monetarios y de stock no pueden ser negativos
        if (insumo.getCostoUnitario() < 0 || insumo.getStockActual() < 0 || insumo.getStockMinimo() < 0) {
            System.err.println("[Lógica] Error: No se permiten valores negativos en stock o costos.");
            return false;
        }

        // Si pasa las validaciones, le pasamos la pelota a Joel (Data)
        return insumoDAO.insertar(insumo);
    }

    /**
     * Valida antes de actualizar.
     */
    public boolean editarInsumo(Insumo insumo) {
        // Podríamos reusar las validaciones anteriores
        if (insumo.getCostoUnitario() < 0) {
            System.err.println("[Lógica] Error: El costo unitario no puede ser negativo.");
            return false;
        }
        return insumoDAO.actualizar(insumo);
    }

    /**
     * Elimina un insumo.
     * (Futura mejora: validar que el insumo no esté en una receta antes de borrarlo).
     */
    public boolean eliminarInsumo(int id) {
        if (id <= 0) {
            System.err.println("[Lógica] Error: ID inválido para eliminación.");
            return false;
        }
        return insumoDAO.eliminar(id);
    }

    /**
     * Devuelve todo el inventario para que José (UI) lo muestre en la tabla.
     */
    public List<Insumo> listarInventario() {
        return insumoDAO.listarTodos();
    }

    /**
     * RF02: Revisa el stock actual vs el mínimo y devuelve solo los que están en peligro.
     * Esta función es puramente lógica, no existe en la base de datos.
     */
    public List<Insumo> obtenerAlertasStock() {
        List<Insumo> todos = insumoDAO.listarTodos();
        List<Insumo> alertas = new ArrayList<>();

        for (Insumo insumo : todos) {
            if (insumo.getStockActual() <= insumo.getStockMinimo()) {
                alertas.add(insumo);
            }
        }

        return alertas;
    }
}