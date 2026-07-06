package com.restaurante.views;

import com.restaurante.data.InsumoDAO;
import com.restaurante.data.PlatoDAO;
import com.restaurante.data.RecetaDAO;
import com.restaurante.logic.RecetaService;
import com.restaurante.model.Insumo;
import com.restaurante.model.Plato;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.Map;

@Route(value = "recetas", layout = MainLayout.class)
public class VistaRecetas extends HorizontalLayout {

    private Map.Entry<Insumo, Double> ingredienteSeleccionadoEdicion = null;
    private final Button btnCancelarEdicionIngrediente = new Button("Cancelar", VaadinIcon.CLOSE.create());
    private final HorizontalLayout contenedorBotonesIngrediente = new HorizontalLayout();

    private final RecetaService recetaService = new RecetaService();
    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final PlatoDAO platoDAO = new PlatoDAO();
    private final InsumoDAO insumoDAO = new InsumoDAO();
    private Button btnCrearPlato = new Button();
    private final Button btnCancelarEdicion = new Button("Cancelar", com.vaadin.flow.component.icon.VaadinIcon.CLOSE.create());
    private final HorizontalLayout contenedorBotonesPlato = new HorizontalLayout();
    private H3 tituloNuevoPlato;

    // Columna Izquierda
    private final Grid<Plato> gridPlatos = new Grid<>(Plato.class, false);
    private Plato platoSeleccionado = null;

    // Columna Derecha (Formulario Platos Nuevos)
    private final TextField txtNombrePlato = new TextField("Nombre del Plato Nuevo");
    private final NumberField numPrecioPlato = new NumberField("Precio de Venta ($)");

    // Columna Derecha (Formulario Recetas)
    private final H3 tituloReceta = new H3("Seleccione un plato para ver su receta");
    private final Grid<Map.Entry<Insumo, Double>> gridIngredientes = new Grid<>();
    private final VerticalLayout emptyStateReceta = new VerticalLayout();
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
        gridPlatos.addColumn(new com.vaadin.flow.data.renderer.ComponentRenderer<>(plato -> {
            Button btnEliminarPlato = new Button(VaadinIcon.TRASH.create());
            btnEliminarPlato.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            btnEliminarPlato.getStyle().set("color", "var(--lumo-error-text-color)");
            btnEliminarPlato.addClickListener(e -> confirmarEliminacionPlato(plato));
            return btnEliminarPlato;
        })).setHeader("Eliminar").setWidth("90px").setFlexGrow(0);

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
        tituloNuevoPlato = new H3("1. Crear Nuevo Plato en el Menú");
        txtNombrePlato.setWidthFull();
        numPrecioPlato.setWidthFull();
        // Configuración inicial del botón principal
        btnCrearPlato.setText("Registrar Plato");
        btnCrearPlato.setIcon(VaadinIcon.PLUS.create());
        btnCrearPlato.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCrearPlato.setWidthFull();
        btnCrearPlato.addClickListener(e -> guardarNuevoPlato());

        // Configuración del botón Cancelar
        btnCancelarEdicion.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        btnCancelarEdicion.setWidthFull();
        btnCancelarEdicion.setVisible(false); // Oculto por defecto en modo creación
        btnCancelarEdicion.addClickListener(e -> cargarRecetaDePlato(null)); // Al cancelar limpiamos la selección

        // Contenedor que manejará los botones
        contenedorBotonesPlato.setWidthFull();
        contenedorBotonesPlato.add(btnCrearPlato, btnCancelarEdicion);

        HorizontalLayout formNuevoPlato = new HorizontalLayout(txtNombrePlato, numPrecioPlato);
        formNuevoPlato.setWidthFull();
        formNuevoPlato.setAlignItems(Alignment.BASELINE);

        // --- SUB-SECCIÓN 2: Gestionar Ingredientes ---
        gridIngredientes.setHeight("180px");
        gridIngredientes.addColumn(entry -> entry.getKey().getNombre()).setHeader("Ingrediente");
        gridIngredientes.addColumn(entry -> String.format("%.2f %s", entry.getValue(), entry.getKey().getUnidadMedida())).setHeader("Cant.");
        gridIngredientes.addColumn(entry -> String.format("$%.2f", entry.getKey().getCostoUnitario() * entry.getValue())).setHeader("Costo");


        // NUEVA COLUMNA: Eliminar ingrediente de la receta seleccionada
        gridIngredientes.addComponentColumn(entry -> {
            Button btnQuitarInsumo = new Button(VaadinIcon.CLOSE.create());
            btnQuitarInsumo.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            btnQuitarInsumo.addClickListener(e -> {
                if (platoSeleccionado != null) {
                    if (recetaService.removerIngredienteDePlato(platoSeleccionado.getId(), entry.getKey().getId())) {
                        Notification.show("Ingrediente removido de la receta", 2000, Notification.Position.BOTTOM_END);
                        cargarRecetaDePlato(platoSeleccionado);
                        actualizarListaPlatos();
                    }
                }
            });
            return btnQuitarInsumo;
        }).setHeader("Quitar").setWidth("75px").setFlexGrow(0);

        // Al dar clic en un ingrediente de la tabla, se activa el modo edición inferior
        gridIngredientes.asSingleSelect().addValueChangeListener(e -> cargarIngredienteParaEdicion(e.getValue()));


        configurarEmptyStateReceta();

        cbInsumos.setWidthFull();
        cbInsumos.setItemLabelGenerator(insumo -> insumo.getNombre() + " [" + insumo.getUnidadMedida() + "]");
        numCantidad.setWidthFull();

        // Creamos un sufijo visual nativo de Vaadin
        Span sufijoUnidad = new Span();
        sufijoUnidad.getStyle().set("color", "var(--lumo-secondary-text-color)");
        numCantidad.setSuffixComponent(sufijoUnidad);

        // EVENTO MÁGICO: Cuando eligen un insumo, el sufijo del NumberField cambia automáticamente
        cbInsumos.addValueChangeListener(event -> {
            Insumo seleccionado = event.getValue();
            if (seleccionado != null) {
                sufijoUnidad.setText(seleccionado.getUnidadMedida());
                numCantidad.setPlaceholder("Ej: 0.25");
            } else {
                sufijoUnidad.setText("");
                numCantidad.setPlaceholder("");
            }
        });

        // Configuración del botón principal de ingredientes
        btnAgregarIngrediente.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnAgregarIngrediente.setWidthFull();
        btnAgregarIngrediente.setEnabled(false);
        btnAgregarIngrediente.addClickListener(e -> asociarIngrediente());

        // Configuración del botón Cancelar ingrediente
        btnCancelarEdicionIngrediente.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        btnCancelarEdicionIngrediente.setWidthFull();
        btnCancelarEdicionIngrediente.setVisible(false); // Oculto inicialmente
        btnCancelarEdicionIngrediente.addClickListener(e -> limpiarFormularioIngrediente());

        // Contenedor dinámico de la botonera inferior
        contenedorBotonesIngrediente.setWidthFull();
        contenedorBotonesIngrediente.add(btnAgregarIngrediente, btnCancelarEdicionIngrediente);

        HorizontalLayout formIngredientes = new HorizontalLayout(cbInsumos, numCantidad);
        formIngredientes.setWidthFull();
        formIngredientes.setAlignItems(Alignment.BASELINE);

        // Armamos la columna derecha agregando todo en orden
        colDerecha.add(tituloNuevoPlato, formNuevoPlato, contenedorBotonesPlato,
                new com.vaadin.flow.component.html.Hr(),
                tituloReceta, gridIngredientes, emptyStateReceta, formIngredientes, contenedorBotonesIngrediente);

        add(colIzquierda, colDerecha);

        updateRecetaContentVisibility(false);
        actualizarListaPlatos();
        actualizarComboInsumos();
    }

    private void actualizarListaPlatos() {
        gridPlatos.setItems(recetaService.listarMenu());
    }

    public void actualizarComboInsumos() {
        cbInsumos.setItems(insumoDAO.listarTodos());
    }

    private void confirmarEliminacionPlato(Plato plato) {
        if (plato == null) {
            return;
        }

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmar eliminación");
        dialog.setText("¿Estás seguro de que deseas eliminar '" + plato.getNombre() + "'? Esto no afectará el historial de ventas.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Eliminar");
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelText("Cancelar");

        dialog.addConfirmListener(event -> {
            // AQUÍ ESTÁ EL CAMBIO MÁGICO: Llamamos al servicio unificado que hace el soft delete
            boolean platoEliminado = recetaService.eliminarPlatoCompleto(plato.getId());

            if (platoEliminado) {
                Notification.show("¡Plato eliminado con éxito!", 2500, Notification.Position.BOTTOM_END)
                        .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS);

                // Limpiamos la vista de receta derecha si el plato eliminado estaba seleccionado
                if (platoSeleccionado != null && platoSeleccionado.getId() == plato.getId()) {
                    platoSeleccionado = null;
                    tituloReceta.setText("2. Seleccione un plato para ver su receta");
                    gridIngredientes.setItems(new ArrayList<>());
                    updateRecetaContentVisibility(false);
                    btnAgregarIngrediente.setEnabled(false);
                }

                // Recargamos la lista visual de la izquierda
                actualizarListaPlatos();
            } else {
                Notification.show("Error interno. No se pudo eliminar el plato.", 3000, Notification.Position.MIDDLE);
            }
        });

        dialog.open();
    }

    /**
     * Lógica operativa para registrar o editar un plato en SQLite
     */
    private void guardarNuevoPlato() {
        String nombre = txtNombrePlato.getValue();
        Double precio = numPrecioPlato.getValue();

        if (nombre.trim().isEmpty() || precio == null || precio <= 0) {
            Notification.show("Ingrese un nombre y un precio válido mayor a cero.", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (platoSeleccionado == null) {
            // MODO CREAR
            Plato nuevoPlato = new Plato(0, nombre, precio, 0.0, null);
            if (recetaService.crearPlato(nuevoPlato)) {
                Notification.show("¡Plato '" + nombre + "' creado con éxito!", 2000, Notification.Position.BOTTOM_END);
                txtNombrePlato.clear();
                numPrecioPlato.clear();
                actualizarListaPlatos();
            } else {
                Notification.show("Error al guardar el plato en SQLite.", 3000, Notification.Position.MIDDLE);
            }
        } else {
            // MODO EDITAR
            platoSeleccionado.setNombre(nombre);
            platoSeleccionado.setPrecioVenta(precio);
            if (recetaService.modificarPlato(platoSeleccionado)) {
                Notification.show("¡Plato actualizado con éxito!", 2000, Notification.Position.BOTTOM_END);

                // Resetear estado del formulario
                platoSeleccionado = null;
                txtNombrePlato.clear();
                numPrecioPlato.clear();
                btnCrearPlato.setText("Registrar Plato");
                btnCrearPlato.setIcon(VaadinIcon.PLUS.create());

                actualizarListaPlatos();
                cargarRecetaDePlato(null); // Limpia la receta de la derecha
            } else {
                Notification.show("Error al actualizar el plato.", 3000, Notification.Position.MIDDLE);
            }
        }
    }

    private void cargarRecetaDePlato(Plato plato) {
        if (plato == null) {
            platoSeleccionado = null;
            tituloReceta.setText("2. Seleccione un plato para ver su receta");
            gridIngredientes.setItems(new ArrayList<>());
            updateRecetaContentVisibility(false);
            btnAgregarIngrediente.setEnabled(false);

            // Regresar el formulario al estado Base (Modo Crear)
            tituloNuevoPlato.setText("1. Crear Nuevo Plato en el Menú");
            txtNombrePlato.clear();
            numPrecioPlato.clear();

            btnCrearPlato.setText("Registrar Plato");
            btnCrearPlato.setIcon(VaadinIcon.PLUS.create());

            // CORRECCIÓN AQUÍ: Volvemos a darle el ancho completo al botón base
            btnCrearPlato.setWidthFull();
            btnCrearPlato.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
            btnCrearPlato.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            btnCancelarEdicion.setVisible(false); // Ocultamos el botón de cancelar
            gridPlatos.deselectAll(); // Quitamos la selección visual del Grid izquierdo
            return;
        }

        platoSeleccionado = plato;
        tituloReceta.setText("2. Receta de: " + plato.getNombre());
        btnAgregarIngrediente.setEnabled(true);

        // Cambios dinámicos de UX para Modo Edición
        tituloNuevoPlato.setText("Editando plato: " + plato.getNombre());
        txtNombrePlato.setValue(plato.getNombre());
        numPrecioPlato.setValue(plato.getPrecioVenta());

        btnCrearPlato.setText("Guardar Cambios");
        btnCrearPlato.setIcon(VaadinIcon.EDIT.create());

        // CORRECCIÓN AQUÍ: Quitamos el ancho completo para que entre el botón Cancelar a su lado
        btnCrearPlato.setWidth("auto");

        // Manejo limpio de los estilos de Vaadin
        btnCrearPlato.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCrearPlato.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        btnCancelarEdicion.setVisible(true); // Hacemos visible el botón para salir de la edición

        Map<Insumo, Double> recetaMap = recetaDAO.obtenerIngredientesPorPlato(plato.getId());
        if (recetaMap == null || recetaMap.isEmpty()) {
            gridIngredientes.setItems(new ArrayList<>());
            updateRecetaContentVisibility(true);
            return;
        }

        gridIngredientes.setItems(recetaMap.entrySet());
        updateRecetaContentVisibility(false);
    }

    private void asociarIngrediente() {
        Insumo insumo = cbInsumos.getValue();
        Double cantidad = numCantidad.getValue();

        if (platoSeleccionado == null) return;

        if (insumo == null || cantidad == null || cantidad <= 0) {
            Notification.show("Seleccione un insumo e ingrese una cantidad mayor a 0", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Ejecutamos la acción en la base de datos (guardarIngrediente usa ON CONFLICT DO UPDATE, así que sirve para ambos)
        if (recetaService.agregarIngredienteAPlato(platoSeleccionado.getId(), insumo.getId(), cantidad)) {
            if (ingredienteSeleccionadoEdicion == null) {
                Notification.show("¡Ingrediente añadido!", 2000, Notification.Position.BOTTOM_END);
            } else {
                Notification.show("¡Ingrediente modificado con éxito!", 2000, Notification.Position.BOTTOM_END);
            }

            limpiarFormularioIngrediente();
            cargarRecetaDePlato(platoSeleccionado);
            actualizarListaPlatos(); // Recarga costos en el Grid izquierdo
        } else {
            Notification.show("Error al guardar el ingrediente en la receta", 3000, Notification.Position.MIDDLE);
        }
    }

    private void configurarEmptyStateReceta() {
        emptyStateReceta.setWidthFull();
        emptyStateReceta.setHeight("180px");
        emptyStateReceta.setAlignItems(Alignment.CENTER);
        emptyStateReceta.setJustifyContentMode(JustifyContentMode.CENTER);
        emptyStateReceta.setSpacing(false);

        Icon iconoInfo = VaadinIcon.INFO_CIRCLE.create();
        iconoInfo.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("width", "28px")
                .set("height", "28px");

        Span textoEmpty = new Span("Esta receta aun no tiene ingredientes.");
        textoEmpty.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        emptyStateReceta.add(iconoInfo, textoEmpty);
        emptyStateReceta.setVisible(false);
    }

    private void updateRecetaContentVisibility(boolean showEmptyState) {
        emptyStateReceta.setVisible(showEmptyState);
        gridIngredientes.setVisible(!showEmptyState);
    }

    /**
     * Carga el ingrediente seleccionado en el formulario inferior para su edición.
     */
    private void cargarIngredienteParaEdicion(Map.Entry<Insumo, Double> entry) {
        if (entry == null) {
            limpiarFormularioIngrediente();
            return;
        }

        ingredienteSeleccionadoEdicion = entry;
        cbInsumos.setValue(entry.getKey());
        cbInsumos.setReadOnly(true); // Bloqueamos el combo para que solo edite la cantidad de ESTE insumo
        numCantidad.setValue(entry.getValue());

        btnAgregarIngrediente.setText("Modificar Insumo");
        btnAgregarIngrediente.setIcon(VaadinIcon.EDIT.create());
        btnAgregarIngrediente.setWidth("auto");
        btnCancelarEdicionIngrediente.setVisible(true);
    }

    /**
     * Limpia el formulario inferior regresándolo a su estado base de inserción.
     */
    private void limpiarFormularioIngrediente() {
        ingredienteSeleccionadoEdicion = null;
        cbInsumos.setReadOnly(false);
        cbInsumos.clear();
        numCantidad.clear();

        btnAgregarIngrediente.setText("Añadir a Receta");
        btnAgregarIngrediente.setIcon(VaadinIcon.DISC.create());
        btnAgregarIngrediente.setWidthFull();
        btnCancelarEdicionIngrediente.setVisible(false);
        gridIngredientes.deselectAll();

        // Mantener habilitado solo si hay un plato seleccionado
        btnAgregarIngrediente.setEnabled(platoSeleccionado != null);
    }
}