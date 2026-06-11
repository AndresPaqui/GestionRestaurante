package com.restaurante.views;

import com.restaurante.data.InsumoDAO;
import com.restaurante.data.RecetaDAO;
import com.restaurante.logic.RecetaService;
import com.restaurante.model.Insumo;
import com.restaurante.model.Plato;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Route(value = "recetas", layout = MainLayout.class)
public class VistaRecetas extends HorizontalLayout {

    private final RecetaService recetaService = new RecetaService();
    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final InsumoDAO insumoDAO = new InsumoDAO();

    // Columna Izquierda
    private final Grid<Plato> gridPlatos = new Grid<>(Plato.class, false);
    private Plato platoSeleccionado = null;

    // Columna Derecha (Formulario Platos Nuevos)
    private final TextField txtNombrePlato = new TextField("Nombre del Plato Nuevo");
    private final NumberField numPrecioPlato = new NumberField("Precio de Venta ($)");
    private final Button btnCrearPlato = new Button("Registrar Plato", VaadinIcon.PLUS.create());

    // Columna Derecha (Formulario Recetas)
    private final H3 tituloReceta = new H3("Seleccione un plato para ver su receta");
    private final Grid<Map.Entry<Insumo, Double>> gridIngredientes = new Grid<>();
    private final ComboBox<Insumo> cbInsumos = new ComboBox<>("Seleccionar Insumo");
    private final NumberField numCantidad = new NumberField("Cantidad Necesaria");
    private final Button btnAgregarIngrediente = new Button("Añadir a Receta", VaadinIcon.DISC.create());

    public VistaRecetas() {
        setSizeFull();
        setSpacing(true);
        setPadding(true);

        // =========================================================================
        // COLUMNA IZQUIERDA: Catálogo de Platos (50%)
        // =========================================================================
        VerticalLayout colIzquierda = new VerticalLayout();
        colIzquierda.setWidth("50%");
        colIzquierda.setHeightFull();
        colIzquierda.setPadding(false);

        H2 tituloPagina = new H2("Recetas e Ingeniería de Menú");

        gridPlatos.setSizeFull();
        gridPlatos.addColumn(Plato::getNombre).setHeader("Plato Principal").setSortable(true);
        gridPlatos.addColumn(p -> String.format("$%.2f", p.getPrecioVenta())).setHeader("P. Venta");
        gridPlatos.addColumn(p -> String.format("$%.2f", p.getCostoProduccion())).setHeader("Costo Prod.");
        gridPlatos.addColumn(p -> String.format("$%.2f", p.getPrecioVenta() - p.getCostoProduccion())).setHeader("Ganancia");

        gridPlatos.asSingleSelect().addValueChangeListener(e -> cargarRecetaDePlato(e.getValue()));
        colIzquierda.add(tituloPagina, gridPlatos);

        // =========================================================================
        // COLUMNA DERECHA: Administración (50%)
        // =========================================================================
        VerticalLayout colDerecha = new VerticalLayout();
        colDerecha.setWidth("50%");
        colDerecha.setHeightFull();
        colDerecha.getStyle().set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "20px");

        // --- SUB-SECCIÓN 1: Crear Plato Nuevo ---
        H3 tituloNuevoPlato = new H3("1. Crear Nuevo Plato en el Menú");
        txtNombrePlato.setWidthFull();
        numPrecioPlato.setWidthFull();
        btnCrearPlato.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCrearPlato.setWidthFull();
        btnCrearPlato.addClickListener(e -> guardarNuevoPlato());

        HorizontalLayout formNuevoPlato = new HorizontalLayout(txtNombrePlato, numPrecioPlato);
        formNuevoPlato.setWidthFull();
        formNuevoPlato.setAlignItems(Alignment.BASELINE);

        // --- SUB-SECCIÓN 2: Gestionar Ingredientes ---
        gridIngredientes.setHeight("180px");
        gridIngredientes.addColumn(entry -> entry.getKey().getNombre()).setHeader("Ingrediente");
        gridIngredientes.addColumn(entry -> String.format("%.2f %s", entry.getValue(), entry.getKey().getUnidadMedida())).setHeader("Cant.");
        gridIngredientes.addColumn(entry -> String.format("$%.2f", entry.getKey().getCostoUnitario() * entry.getValue())).setHeader("Costo");

        cbInsumos.setWidthFull();
        cbInsumos.setItemLabelGenerator(Insumo::getNombre);
        numCantidad.setWidthFull();

        btnAgregarIngrediente.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btnAgregarIngrediente.setWidthFull();
        btnAgregarIngrediente.setEnabled(false);
        btnAgregarIngrediente.addClickListener(e -> asociarIngrediente());

        HorizontalLayout formIngredientes = new HorizontalLayout(cbInsumos, numCantidad);
        formIngredientes.setWidthFull();
        formIngredientes.setAlignItems(Alignment.BASELINE);

        // Armamos la columna derecha agregando todo en orden
        colDerecha.add(tituloNuevoPlato, formNuevoPlato, btnCrearPlato,
                new com.vaadin.flow.component.html.Hr(), // Línea divisoria visual
                tituloReceta, gridIngredientes, formIngredientes, btnAgregarIngrediente);

        add(colIzquierda, colDerecha);

        actualizarListaPlatos();
        actualizarComboInsumos();
    }

    private void actualizarListaPlatos() {
        gridPlatos.setItems(recetaService.listarMenu());
    }

    public void actualizarComboInsumos() {
        cbInsumos.setItems(insumoDAO.listarTodos());
    }

    /**
     * Lógica operativa para registrar un plato base en SQLite
     */
    private void guardarNuevoPlato() {
        String nombre = txtNombrePlato.getValue();
        Double precio = numPrecioPlato.getValue();

        if (nombre.trim().isEmpty() || precio == null || precio <= 0) {
            Notification.show("Ingrese un nombre y un precio válido mayor a cero.", 3000, Notification.Position.MIDDLE);
            return;
        }

        Plato nuevoPlato = new Plato(0, nombre, precio, 0.0, null);
        if (recetaService.crearPlato(nuevoPlato)) {
            Notification.show("¡Plato '" + nombre + "' creado con éxito!", 2000, Notification.Position.BOTTOM_END);
            txtNombrePlato.clear();
            numPrecioPlato.clear();
            actualizarListaPlatos();
        } else {
            Notification.show("Error al guardar el plato en SQLite.", 3000, Notification.Position.MIDDLE);
        }
    }

    private void cargarRecetaDePlato(Plato plato) {
        if (plato == null) {
            platoSeleccionado = null;
            tituloReceta.setText("2. Seleccione un plato para ver su receta");
            gridIngredientes.setItems(new ArrayList<>());
            btnAgregarIngrediente.setEnabled(false);
            return;
        }

        platoSeleccionado = plato;
        tituloReceta.setText("2. Receta de: " + plato.getNombre());
        btnAgregarIngrediente.setEnabled(true);

        Map<Insumo, Double> recetaMap = recetaDAO.obtenerIngredientesPorPlato(plato.getId());
        gridIngredientes.setItems(recetaMap.entrySet());
    }

    private void asociarIngrediente() {
        Insumo insumo = cbInsumos.getValue();
        Double cantidad = numCantidad.getValue();

        if (platoSeleccionado == null) return;

        if (insumo == null || cantidad == null || cantidad <= 0) {
            Notification.show("Seleccione un insumo e ingrese una cantidad mayor a 0", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (recetaService.agregarIngredienteAPlato(platoSeleccionado.getId(), insumo.getId(), cantidad)) {
            Notification.show("¡Ingrediente añadido!", 2000, Notification.Position.BOTTOM_END);
            cbInsumos.clear();
            numCantidad.clear();
            cargarRecetaDePlato(platoSeleccionado);
            actualizarListaPlatos();
        } else {
            Notification.show("Error al guardar el ingrediente en la receta", 3000, Notification.Position.MIDDLE);
        }
    }
}