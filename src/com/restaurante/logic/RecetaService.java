package com.restaurante.logic;

import com.restaurante.data.PlatoDAO;
import com.restaurante.data.RecetaDAO;
import com.restaurante.model.Insumo;
import com.restaurante.model.Plato;

import java.util.List;
import java.util.Map;

public class RecetaService {

    // Este servicio necesita hablar con dos DAOs diferentes
    private final PlatoDAO platoDAO;
    private final RecetaDAO recetaDAO;

    public RecetaService() {
        this.platoDAO = new PlatoDAO();
        this.recetaDAO = new RecetaDAO();
    }

    /**
     * Valida y crea la "cáscara" del plato (solo nombre y precio de venta).
     */
    public boolean crearPlato(Plato plato) {
        if (plato.getNombre() == null || plato.getNombre().trim().isEmpty()) {
            System.err.println("[Lógica] Error: El plato debe tener un nombre.");
            return false;
        }
        if (plato.getPrecioVenta() <= 0) {
            System.err.println("[Lógica] Error: El precio de venta debe ser mayor a cero.");
            return false;
        }
        return platoDAO.insertar(plato);
    }

    /**
     * Vincula un ingrediente a un plato existente validando cantidades.
     */
    public boolean agregarIngredienteAPlato(int idPlato, int idInsumo, double cantidad) {
        if (cantidad <= 0) {
            System.err.println("[Lógica] Error: La cantidad del ingrediente debe ser mayor a cero.");
            return false;
        }
        return recetaDAO.guardarIngrediente(idPlato, idInsumo, cantidad);
    }

    /**
     * RF03: Calcula el costo real de producir un plato basado en sus ingredientes.
     * Esta es la inteligencia del negocio.
     */
    public double calcularCostoProduccion(int idPlato) {
        // 1. Le pedimos a Joel la receta (El Map con los insumos y sus cantidades)
        Map<Insumo, Double> ingredientes = recetaDAO.obtenerIngredientesPorPlato(idPlato);

        double costoTotal = 0.0;

        // 2. Iteramos sobre cada ingrediente de la receta
        for (Map.Entry<Insumo, Double> entrada : ingredientes.entrySet()) {
            Insumo insumo = entrada.getKey();
            double cantidadNecesaria = entrada.getValue();

            // 3. Multiplicamos (Costo unitario * Cantidad usada) y lo sumamos al total
            costoTotal += (insumo.getCostoUnitario() * cantidadNecesaria);
        }

        return costoTotal;
    }

    /**
     * Devuelve todos los platos. (Opcional: aquí Helen podría llamar a calcularCostoProduccion
     * para cada plato antes de enviarlos a la UI, para que José los muestre completos).
     */
    /**
     * Devuelve todos los platos del menú inyectándoles su costo de producción en tiempo real.
     */
    public List<Plato> listarMenu() {
        List<Plato> platos = platoDAO.listarTodos();
        for (Plato p : platos) {
            double costo = calcularCostoProduccion(p.getId());
            p.setCostoProduccion(costo); // Seteamos el costo calculado dinámicamente
        }
        return platos;
    }

    /**
     * Elimina los ingredientes del plato en la tabla pivote y oculta el plato
     * del sistema sin borrar su historial financiero.
     */
    public boolean eliminarPlatoCompleto(int idPlato) {
        // 1. Liberamos los insumos (solo elimina la relación en la tabla 'recetas')
        recetaDAO.eliminarIngredientesDePlato(idPlato);

        // 2. Ocultamos el plato (Soft delete)
        return platoDAO.eliminar(idPlato);
    }

    /**
     * Actualiza el nombre y el precio de un plato existente.
     */
    public boolean modificarPlato(Plato plato) {
        if (plato == null || plato.getId() <= 0) return false;
        if (plato.getNombre() == null || plato.getNombre().trim().isEmpty()) return false;
        if (plato.getPrecioVenta() <= 0) return false;
        return platoDAO.actualizar(plato);
    }

    /**
     * Elimina un único insumo de la receta de un plato.
     */
    public boolean removerIngredienteDePlato(int idPlato, int idInsumo) {
        if (idPlato <= 0 || idInsumo <= 0) return false;
        return recetaDAO.eliminarIngredienteDePlato(idPlato, idInsumo);
    }
}