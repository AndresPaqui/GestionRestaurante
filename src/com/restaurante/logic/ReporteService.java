package com.restaurante.logic;

import com.restaurante.data.PlatoDAO;
import com.restaurante.data.VentasDAO;
import com.restaurante.model.ItemVenta;
import com.restaurante.model.Plato;
import com.restaurante.model.Venta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporteService {

    private final VentasDAO ventasDAO;
    private final PlatoDAO platoDAO;
    private final RecetaService recetaService; // ¡Reutilizamos el servicio que ya creamos!

    public ReporteService() {
        this.ventasDAO = new VentasDAO();
        this.platoDAO = new PlatoDAO();
        this.recetaService = new RecetaService();
    }

    /**
     * INDICADOR 1: ¿Cuánto se gana con cada plato?
     * Ganancia Neta = PVP - Costo de los ingredientes
     */
    public double calcularGananciaNetaPorPlato(Plato plato) {
        // Llamamos al otro servicio para que haga el trabajo sucio de sumar insumos
        double costoProduccion = recetaService.calcularCostoProduccion(plato.getId());

        // Restamos: Precio de Venta - Lo que costó hacerlo
        return plato.getPrecioVenta() - costoProduccion;
    }

    /**
     * INDICADOR 2: Plato más comprado (El favorito de los clientes)
     * Revisa el historial de ventas y cuenta las cantidades.
     */
    public Plato obtenerPlatoMasVendido() {
        List<Venta> historial = ventasDAO.listarHistorial();

        // Un Map para llevar el conteo: <ID del Plato, Cantidad Total Vendida>
        Map<Integer, Integer> conteoVentas = new HashMap<>();

        // 1. Recorremos todas las facturas y sumamos los platos vendidos
        for (Venta venta : historial) {
            for (ItemVenta item : venta.getDetalles()) {
                int idPlato = item.getPlato().getId();
                int cantidad = item.getCantidad();

                // Si ya existe, le sumamos la nueva cantidad; si no, empieza en la cantidad vendida
                conteoVentas.put(idPlato, conteoVentas.getOrDefault(idPlato, 0) + cantidad);
            }
        }

        // 2. Buscamos cuál es el ID con el número mayor
        int idGanador = -1;
        int maxVentas = 0;

        for (Map.Entry<Integer, Integer> entry : conteoVentas.entrySet()) {
            if (entry.getValue() > maxVentas) {
                maxVentas = entry.getValue();
                idGanador = entry.getKey();
            }
        }

        // 3. Devolvemos el objeto Plato correspondiente a ese ID
        return buscarPlatoEnMemoria(idGanador);
    }

    /**
     * INDICADOR 3: Plato que genera MÁS GANANCIA TOTAL al restaurante
     * (A veces el más vendido no es el que deja más plata).
     */
    public Plato obtenerPlatoMasRentable() {
        List<Venta> historial = ventasDAO.listarHistorial();

        // Map: <ID del Plato, Dinero Neto Generado>
        Map<Integer, Double> gananciaTotalPorPlato = new HashMap<>();

        for (Venta venta : historial) {
            for (ItemVenta item : venta.getDetalles()) {
                int idPlato = item.getPlato().getId();
                Plato platoBase = buscarPlatoEnMemoria(idPlato);

                if (platoBase != null) {
                    // Calculamos cuánto dejó de ganancia este ítem en esta venta específica
                    double gananciaUnitaria = calcularGananciaNetaPorPlato(platoBase);
                    double gananciaDeEstaVenta = gananciaUnitaria * item.getCantidad();

                    gananciaTotalPorPlato.put(idPlato,
                            gananciaTotalPorPlato.getOrDefault(idPlato, 0.0) + gananciaDeEstaVenta);
                }
            }
        }

        // Buscamos el ID que haya generado más dinero
        int idGanador = -1;
        double maxGanancia = 0.0;

        for (Map.Entry<Integer, Double> entry : gananciaTotalPorPlato.entrySet()) {
            if (entry.getValue() > maxGanancia) {
                maxGanancia = entry.getValue();
                idGanador = entry.getKey();
            }
        }

        return buscarPlatoEnMemoria(idGanador);
    }

    /**
     * Método auxiliar privado para no consultar la base de datos a cada rato.
     */
    private Plato buscarPlatoEnMemoria(int idPlato) {
        if (idPlato == -1) return null;

        List<Plato> todosLosPlatos = platoDAO.listarTodos();
        for (Plato p : todosLosPlatos) {
            if (p.getId() == idPlato) {
                return p;
            }
        }
        return null;
    }
}