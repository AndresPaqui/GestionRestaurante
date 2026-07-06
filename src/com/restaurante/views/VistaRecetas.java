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
import com.vaadin.flow.shared.Registration;

import java.util.ArrayList;
import java.util.Map;

@Route(value = "recetas", layout = MainLayout.class)
public class VistaRecetas extends HorizontalLayout {

    private Map.Entry<Insumo, Double> ingredienteSeleccionadoEdicion = null;
    private final Button btnCancelarEdicionIngrediente = new Button("Cancelar", VaadinIcon.CLOSE.create());
    private final HorizontalLayout contenedorBotonesIngrediente = new HorizontalLayout();

    // Variable para controlar dinámicamente el listener del Grid de Platos
    private Registration registroListenerPlatos;

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

    // Variable para controlar dinámicamente el listener del Grid de ingredientes
    private Registration registroListenerIngredientes;

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

        // Activamos el listener controlado para los platos
        conectarListenerPlatos();
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

        btnCrearPlato.setText("Registrar Plato");
        btnCrearPlato.setIcon(VaadinIcon.PLUS.create());
        btnCrearPlato.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCrearPlato.setWidthFull();
        btnCrearPlato.addClickListener(e -> guardarNuevoPlato());

        btnCancelarEdicion.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        btnCancelarEdicion.setWidthFull();
        btnCancelarEdicion.setVisible(false);
        btnCancelarEdicion.addClickListener(e -> cargarRecetaDePlato(null));

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

        // NUEVA COLUMNA: Eliminar ingrediente de la receta seleccionada (VERSIÓN BLINDADA DE UX)
        gridIngredientes.addComponentColumn(entry -> {
            Button btnQuitarInsumo = new Button(VaadinIcon.CLOSE.create());
            btnQuitarInsumo.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            btnQuitarInsumo.addClickListener(e -> {
                if (platoSeleccionado != null) {
                    if (recetaService.removerIngredienteDePlato(platoSeleccionado.getId(), entry.getKey().getId())) {
                        Notification.show("Ingrediente removido de la receta", 2000, Notification.Position.BOTTOM_END);

                        // Reseteamos el estado de edición local de insumos
                        ingredienteSeleccionadoEdicion = null;

                        // CAPA DE SEGURIDAD INTERNA: Diferimos el refresco visual al siguiente ciclo de renderizado
                        // Esto evita que los clicks residuales del Grid limpien el formulario lateral de edición.
                        getUI().ifPresent(ui -> ui.access(() -> {
                            limpiarFormularioIngrediente();
                            cargarRecetaDePlato(platoSeleccionado);
                            actualizarListaPlatos(); // Recarga costos en el Grid izquierdo
                        }));
                    }
                }
            });
            return btnQuitarInsumo;
        }).setHeader("Quitar").setWidth("75px").setFlexGrow(0);

        // Activamos de forma inicial el listener controlado
        conectarListenerIngredientes();

        configurarEmptyStateReceta();

        cbInsumos.setWidthFull();
        cbInsumos.setItemLabelGenerator(insumo -> insumo.getNombre() + " [" + insumo.getUnidadMedida() + "]");
        numCantidad.setWidthFull();

        Span sufijoUnidad = new Span();
        sufijoUnidad.getStyle().set("color", "var(--lumo-secondary-text-color)");
        numCantidad.setSuffixComponent(sufijoUnidad);

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

        btnAgregarIngrediente.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnAgregarIngrediente.setWidthFull();
        btnAgregarIngrediente.setEnabled(false);
        btnAgregarIngrediente.addClickListener(e -> asociarIngrediente());

        btnCancelarEdicionIngrediente.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        btnCancelarEdicionIngrediente.setWidthFull();
        btnCancelarEdicionIngrediente.setVisible(false);
        btnCancelarEdicionIngrediente.addClickListener(e -> limpiarFormularioIngrediente());

        contenedorBotonesIngrediente.setWidthFull();
        contenedorBotonesIngrediente.add(btnAgregarIngrediente, btnCancelarEdicionIngrediente);

        HorizontalLayout formIngredientes = new HorizontalLayout(cbInsumos, numCantidad);
        formIngredientes.setWidthFull();
        formIngredientes.setAlignItems(Alignment.BASELINE);

        colDerecha.add(tituloNuevoPlato, formNuevoPlato, contenedorBotonesPlato,
                new com.vaadin.flow.component.html.Hr(),
                tituloReceta, gridIngredientes, emptyStateReceta, formIngredientes, contenedorBotonesIngrediente);

        add(colIzquierda, colDerecha);

        updateRecetaContentVisibility(false);
        actualizarListaPlatos();
        actualizarComboInsumos();
    }

    /**
     * Conecta de forma segura el listener encargado de escuchar la selección de la tabla de insumos.
     */
    private void conectarListenerIngredientes() {
        if (registroListenerIngredientes == null) {
            registroListenerIngredientes = gridIngredientes.asSingleSelect()
                    .addValueChangeListener(e -> cargarIngredienteParaEdicion(e.getValue()));
        }
    }

    /**
     * Desconecta temporalmente el listener para que los refrescos de datos no ejecuten acciones inesperadas.
     */
    private void desconectarListenerIngredientes() {
        if (registroListenerIngredientes != null) {
            registroListenerIngredientes.remove();
            registroListenerIngredientes = null;
        }
    }

    /**
     * Conecta el listener del catálogo de platos.
     */
    private void conectarListenerPlatos() {
        if (registroListenerPlatos == null) {
            registroListenerPlatos = gridPlatos.asSingleSelect()
                    .addValueChangeListener(e -> cargarRecetaDePlato(e.getValue()));
        }
    }

    /**
     * Desconecta el listener del catálogo de platos.
     */
    private void desconectarListenerPlatos() {
        if (registroListenerPlatos != null) {
            registroListenerPlatos.remove();
            registroListenerPlatos = null;
        }
    }

    /**
     * Actualiza la tabla izquierda sin disparar el evento de deselección que cierra la edición.
     */
    private void actualizarListaPlatos() {
        // 1. Guardamos el ID del plato que estamos editando actualmente
        Integer idSeleccionado = (platoSeleccionado != null) ? platoSeleccionado.getId() : null;

        // 2. Apagamos el listener para que Vaadin no lance un "null" al cambiar los datos
        desconectarListenerPlatos();

        // 3. Cargamos los datos frescos (con los costos recalculados)
        java.util.List<Plato> nuevaLista = recetaService.listarMenu();
        gridPlatos.setItems(nuevaLista);

        // 4. Si estábamos editando un plato, lo volvemos a seleccionar visualmente en la nueva lista
        if (idSeleccionado != null) {
            Plato platoActualizado = nuevaLista.stream()
                    .filter(p -> p.getId() == idSeleccionado)
                    .findFirst()
                    .orElse(null);

            if (platoActualizado != null) {
                gridPlatos.select(platoActualizado); // Marcamos la fila de azul
                platoSeleccionado = platoActualizado; // Actualizamos nuestra variable global
            }
        }

        // 5. Volvemos a encender el listener para futuros clics
        conectarListenerPlatos();
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
            boolean platoEliminado = recetaService.eliminarPlatoCompleto(plato.getId());

            if (platoEliminado) {
                Notification.show("¡Plato eliminado con éxito!", 2500, Notification.Position.BOTTOM_END)
                        .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS);

                if (platoSeleccionado != null && platoSeleccionado.getId() == plato.getId()) {
                    platoSeleccionado = null;
                    tituloReceta.setText("2. Seleccione un plato para ver su receta");
                    gridIngredientes.setItems(new ArrayList<>());
                    updateRecetaContentVisibility(false);
                    btnAgregarIngrediente.setEnabled(false);
                }

                actualizarListaPlatos();
            } else {
                Notification.show("Error interno. No se pudo eliminar el plato.", 3000, Notification.Position.MIDDLE);
            }
        });

        dialog.open();
    }

    private void guardarNuevoPlato() {
        String nombre = txtNombrePlato.getValue();
        Double precio = numPrecioPlato.getValue();

        if (nombre.trim().isEmpty() || precio == null || precio <= 0) {
            Notification.show("Ingrese un nombre y un precio válido mayor a cero.", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (platoSeleccionado == null) {
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
            platoSeleccionado.setNombre(nombre);
            platoSeleccionado.setPrecioVenta(precio);
            if (recetaService.modificarPlato(platoSeleccionado)) {
                Notification.show("¡Plato actualizado con éxito!", 2000, Notification.Position.BOTTOM_END);

                platoSeleccionado = null;
                txtNombrePlato.clear();
                numPrecioPlato.clear();
                btnCrearPlato.setText("Registrar Plato");
                btnCrearPlato.setIcon(VaadinIcon.PLUS.create());

                actualizarListaPlatos();
                cargarRecetaDePlato(null);
            } else {
                Notification.show("Error al actualizar el plato.", 3000, Notification.Position.MIDDLE);
            }
        }
    }

    private void cargarRecetaDePlato(Plato plato) {
        if (plato == null) {
            platoSeleccionado = null;
            tituloReceta.setText("2. Seleccione un plato para ver su receta");

            desconectarListenerIngredientes();
            gridIngredientes.setItems(new ArrayList<>());
            conectarListenerIngredientes();

            updateRecetaContentVisibility(false);
            btnAgregarIngrediente.setEnabled(false);

            tituloNuevoPlato.setText("1. Crear Nuevo Plato en el Menú");
            txtNombrePlato.clear();
            numPrecioPlato.clear();

            btnCrearPlato.setText("Registrar Plato");
            btnCrearPlato.setIcon(VaadinIcon.PLUS.create());

            btnCrearPlato.setWidthFull();
            btnCrearPlato.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
            btnCrearPlato.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            btnCancelarEdicion.setVisible(false);
            gridPlatos.deselectAll();
            return;
        }

        platoSeleccionado = plato;
        tituloReceta.setText("2. Receta de: " + plato.getNombre());
        btnAgregarIngrediente.setEnabled(true);

        tituloNuevoPlato.setText("Editando plato: " + plato.getNombre());
        txtNombrePlato.setValue(plato.getNombre());
        numPrecioPlato.setValue(plato.getPrecioVenta());

        btnCrearPlato.setText("Guardar Cambios");
        btnCrearPlato.setIcon(VaadinIcon.EDIT.create());
        btnCrearPlato.setWidth("auto");

        btnCrearPlato.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCrearPlato.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        btnCancelarEdicion.setVisible(true);

        Map<Insumo, Double> recetaMap = recetaDAO.obtenerIngredientesPorPlato(plato.getId());

        // APAGAMOS EL LISTENER ANTES DE SETEAR LOS ÍTEMS NUEVOS
        desconectarListenerIngredientes();
        if (recetaMap == null || recetaMap.isEmpty()) {
            gridIngredientes.setItems(new ArrayList<>());
            conectarListenerIngredientes(); // LO VOLVEMOS A PRENDER
            updateRecetaContentVisibility(true);
            return;
        }

        gridIngredientes.setItems(recetaMap.entrySet());
        conectarListenerIngredientes(); // LO VOLVEMOS A PRENDER
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

        if (recetaService.agregarIngredienteAPlato(platoSeleccionado.getId(), insumo.getId(), cantidad)) {
            if (ingredienteSeleccionadoEdicion == null) {
                Notification.show("¡Ingrediente añadido!", 2000, Notification.Position.BOTTOM_END);
            } else {
                Notification.show("¡Ingrediente modificado con éxito!", 2000, Notification.Position.BOTTOM_END);
            }

            limpiarFormularioIngrediente();
            cargarRecetaDePlato(platoSeleccionado);
            actualizarListaPlatos();
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
            return;
        }

        ingredienteSeleccionadoEdicion = entry;
        cbInsumos.setValue(entry.getKey());
        cbInsumos.setReadOnly(true);
        numCantidad.setValue(entry.getValue());

        btnAgregarIngrediente.setText("Modificar Insumo");
        btnAgregarIngrediente.setIcon(VaadinIcon.EDIT.create());
        btnAgregarIngrediente.setWidth("auto");
        btnCancelarEdicionIngrediente.setVisible(true);
    }

    /**
     * Limpia el formulario inferior regresándolo a su estado base de inserción
     * sin cerrar la edición del plato principal.
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

        // Desconectamos el listener para vaciar de forma segura la selección visual sin disparar bugs
        desconectarListenerIngredientes();
        gridIngredientes.deselectAll();
        conectarListenerIngredientes();

        btnAgregarIngrediente.setEnabled(platoSeleccionado != null);
    }
}