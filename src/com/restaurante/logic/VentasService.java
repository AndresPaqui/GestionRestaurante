package com.restaurante.logic;

import com.restaurante.data.InsumoDAO;
import com.restaurante.data.RecetaDAO;
import com.restaurante.data.VentasDAO;
import com.restaurante.model.Insumo;
import com.restaurante.model.ItemVenta;
import com.restaurante.model.Venta;

import java.util.List;
import java.util.Map;

public class VentasService {

    // El Jefe necesita hablar con todos los departamentos (DAOs)
    private final VentasDAO ventasDAO;
    private final RecetaDAO recetaDAO;
    private final InsumoDAO insumoDAO;

    public VentasService() {
        this.ventasDAO = new VentasDAO();
        this.recetaDAO = new RecetaDAO();
        this.insumoDAO = new InsumoDAO();
    }

    /**
     * RF06 y RF07: El corazón del sistema. Registra la venta y descuenta la materia prima.
     */
    public boolean registrarVentaCompleta(Venta venta) {
        // 1. Validar que la venta no esté vacía
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            System.err.println("[Lógica] Error: No se puede procesar una venta sin productos.");
            return false;
        }

        // 2. Validar que el total sea coherente
        if (venta.getTotalVenta() <= 0) {
            System.err.println("[Lógica] Error: El total de la venta debe ser mayor a cero.");
            return false;
        }

        // 3. Registrar la "Cabecera" de la venta (La fecha y el total)
        // OJO: Al hacer esto, Joel le asigna el ID a la venta en la base de datos.
        if (!ventasDAO.registrarVenta(venta)) {
            System.err.println("[Lógica] Error: Falló el registro en la base de datos.");
            return false;
        }

        // 4. Procesar cada plato (ItemVenta) del recibo
        for (ItemVenta item : venta.getDetalles()) {

            // A. Guardamos el detalle de la venta (El renglón del recibo)
            ventasDAO.registrarDetalleVenta(item, venta.getIdVenta());

            // B. ¡LA MAGIA! Descontamos los ingredientes usados
            descontarInventario(item.getPlato().getId(), item.getCantidad());
        }

        return true; // Venta exitosa
    }

    /**
     * Módulo Privado: La Explosión de Inventario.
     * Busca la receta de un plato y resta los insumos de la bodega.
     */
    private void descontarInventario(int idPlato, int cantidadComprada) {

        // 1. Le pedimos a la BD qué ingredientes lleva este plato
        Map<Insumo, Double> receta = recetaDAO.obtenerIngredientesPorPlato(idPlato);

        // 2. Recorremos cada ingrediente (Ej: Cerdo, Mote, Papas)
        for (Map.Entry<Insumo, Double> entrada : receta.entrySet()) {
            Insumo insumo = entrada.getKey();
            double cantidadPorPlato = entrada.getValue();

            // 3. Multiplicamos (Lo que lleva 1 plato * La cantidad de platos vendidos)
            double cantidadTotalA_Descontar = cantidadPorPlato * cantidadComprada;

            // 4. Calculamos el nuevo stock en memoria
            double nuevoStock = insumo.getStockActual() - cantidadTotalA_Descontar;
            insumo.setStockActual(nuevoStock);

            // 5. Le decimos a Joel que actualice la base de datos con el nuevo valor
            insumoDAO.actualizar(insumo);
        }
    }

    /**
     * Para que José (UI) pueda mostrar el historial de ventas en pantalla.
     */
    public List<Venta> obtenerHistorialVentas() {
        return ventasDAO.listarHistorial();
    }

    public List<ItemVenta> obtenerDetallesPorVenta(int idVenta) {
        return ventasDAO.obtenerDetallesPorVenta(idVenta);
    }

    /**
     * Verifica matemáticamente si hay suficiente inventario para cocinar todo el pedido
     */
    public String validarDisponibilidadStock(Venta venta) {
        // Mapa para acumular cuántos gramos/unidades necesitamos de cada insumo para toda la factura
        java.util.Map<Integer, Double> stockNecesario = new java.util.HashMap<>();

        for (ItemVenta item : venta.getDetalles()) {
            // Buscamos la receta del plato
            java.util.Map<Insumo, Double> receta = recetaDAO.obtenerIngredientesPorPlato(item.getPlato().getId());

            for (java.util.Map.Entry<Insumo, Double> entry : receta.entrySet()) {
                int idInsumo = entry.getKey().getId();
                // Multiplicamos (Lo que lleva 1 plato * La cantidad de platos en el carrito)
                double cantTotalRequerida = entry.getValue() * item.getCantidad();

                // Acumulamos (por si dos platos diferentes usan el mismo ingrediente, ej. Papas)
                stockNecesario.put(idInsumo, stockNecesario.getOrDefault(idInsumo, 0.0) + cantTotalRequerida);
            }
        }

        // Ahora cruzamos los totales calculados contra la base de datos real
        for (java.util.Map.Entry<Integer, Double> requerimiento : stockNecesario.entrySet()) {
            Insumo insumoBD = insumoDAO.buscarPorId(requerimiento.getKey());

            if (insumoBD.getStockActual() < requerimiento.getValue()) {
                return "Stock insuficiente de '" + insumoBD.getNombre() + "'. Se requieren "
                        + requerimiento.getValue() + " " + insumoBD.getUnidadMedida()
                        + ", pero solo hay " + insumoBD.getStockActual() + ".";
            }
        }

        return "OK"; // Si pasa el bucle, hay stock para todo
    }
}