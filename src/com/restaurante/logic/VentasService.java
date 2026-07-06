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

    /**
     * Calcula la sumatoria de todas las ventas realizadas (con IVA incluido)
     */
    public double calcularTotalVentasFacturadas() {
        return obtenerHistorialVentas().stream().mapToDouble(Venta::getTotalVenta).sum();
    }

    /**
     * Calcula el costo total de producción de los platos vendidos históricamente
     */
    public double calcularCostoTotalInvertido() {
        double costoTotal = 0.0;
        List<Venta> ventas = obtenerHistorialVentas();

        for (Venta v : ventas) {
            // Jalamos los detalles reales de cada factura
            List<ItemVenta> items = ventasDAO.obtenerDetallesPorVenta(v.getIdVenta());
            for (ItemVenta item : items) {
                // Multiplicamos (Costo de producción del plato * Cantidad de veces vendida)
                double costoPlato = recetaDAO.obtenerIngredientesPorPlato(item.getPlato().getId())
                        .entrySet().stream()
                        .mapToDouble(entry -> entry.getKey().getCostoUnitario() * entry.getValue())
                        .sum();
                costoTotal += (costoPlato * item.getCantidad());
            }
        }
        return costoTotal;
    }

    /**
     * Devuelve el historial de ventas acotado entre un rango de fechas.
     */
    public List<Venta> obtenerHistorialVentasPorFecha(java.time.LocalDate inicio, java.time.LocalDate fin) {
        if (inicio == null || fin == null) {
            return obtenerHistorialVentas();
        }

        // Convertimos los rangos a las horas límite del día (00:00:00 y 23:59:59)
        java.time.LocalDateTime desde = inicio.atStartOfDay();
        java.time.LocalDateTime hasta = fin.atTime(23, 59, 59);

        // Filtramos comparando los objetos de fecha nativos
        return obtenerHistorialVentas().stream()
                .filter(v -> {
                    if (v.getFecha() == null) return false;

                    // Si tu método v.getFecha() devuelve un LocalDateTime:
                    java.time.LocalDateTime fechaVenta = v.getFecha();
                    return !fechaVenta.isBefore(desde) && !fechaVenta.isAfter(hasta);
                })
                .toList();
    }

    /**
     * Calcula la sumatoria de las ventas facturadas en un rango de fechas.
     */
    public double calcularTotalVentasFacturadasPorFecha(java.time.LocalDate inicio, java.time.LocalDate fin) {
        return obtenerHistorialVentasPorFecha(inicio, fin).stream()
                .mapToDouble(Venta::getTotalVenta)
                .sum();
    }

    /**
     * Calcula el costo de producción acumulado únicamente de las ventas del periodo seleccionado.
     */
    public double calcularCostoTotalInvertidoPorFecha(java.time.LocalDate inicio, java.time.LocalDate fin) {
        double costoTotal = 0.0;
        List<Venta> ventasPeriodo = obtenerHistorialVentasPorFecha(inicio, fin);

        for (Venta v : ventasPeriodo) {
            List<ItemVenta> items = ventasDAO.obtenerDetallesPorVenta(v.getIdVenta()); // Usando la conexión por renglón
            if (items == null) {
                // Validación por si tu método devuelve la lista vacía o nula
                items = ventasDAO.obtenerDetallesPorVenta(v.getIdVenta());
            }
            for (ItemVenta item : items) {
                double costoPlato = recetaDAO.obtenerIngredientesPorPlato(item.getPlato().getId())
                        .entrySet().stream()
                        .mapToDouble(entry -> entry.getKey().getCostoUnitario() * entry.getValue())
                        .sum();
                costoTotal += (costoPlato * item.getCantidad());
            }
        }
        return costoTotal;
    }
}