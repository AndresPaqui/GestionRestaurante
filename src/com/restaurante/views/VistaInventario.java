package com.restaurante.views;

import com.restaurante.data.InsumoDAO;
import com.restaurante.logic.InventarioService;
import com.restaurante.model.Insumo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "inventario", layout = MainLayout.class)
public class VistaInventario extends HorizontalLayout {

    private final InsumoDAO insumoDAO = new InsumoDAO();
    private final InventarioService inventarioService = new InventarioService();
    /*private final com.restaurante.logic.InventarioService inventarioService = new com.restaurante.logic.InventarioService();*/

    // Componentes interactivos de la interfaz
    private final Grid<Insumo> gridInsumos = new Grid<>(Insumo.class, false);

    // Campos del Formulario de Entrada
    private final TextField txtNombre = new TextField("Nombre del Insumo");
    private final ComboBox<String> cbCategoria = new ComboBox<>("Categoría");
    private final NumberField numStockActual = new NumberField("Stock Actual");
    private final NumberField numStockMinimo = new NumberField("Stock Mínimo");
    private final NumberField numCostoUnitario = new NumberField("Costo Unitario ($)");
    private final ComboBox<String> cbUnidadMedida = new ComboBox<>("Unidad de Medida");

    private final Button btnGuardar = new Button("Guardar Insumo", VaadinIcon.DISC.create());
    private final Button btnEliminar = new Button("Eliminar", VaadinIcon.TRASH.create());

    // Puntero para identificar si estamos editando o creando
    private Insumo insumoSeleccionado = null;

    public VistaInventario() {
        // Ajustamos el Layout para que use toda la ventana de forma elástica
        setSizeFull();
        setSpacing(true);
        setPadding(true);

        // =========================================================================
        // COLUMNA IZQUIERDA: Tabla de Auditoría de Bodega (70%)
        // =========================================================================
        VerticalLayout colTabla = new VerticalLayout();
        colTabla.setWidth("70%");
        colTabla.setHeightFull();
        colTabla.setPadding(false);

        H2 tituloInventario = new H2("Control de Inventario (Materia Prima)");

        // Configuración de las columnas del Grid
        gridInsumos.setSizeFull();
        gridInsumos.addColumn(Insumo::getId).setHeader("ID").setWidth("60px").setFlexGrow(0);
        gridInsumos.addColumn(Insumo::getNombre).setHeader("Nombre").setSortable(true);
        gridInsumos.addColumn(Insumo::getCategoria).setHeader("Categoría").setSortable(true);

        // Columna Numérica (Solo muestra el número)
        gridInsumos.addColumn(insumo -> String.format("%.2f %s", insumo.getStockActual(), insumo.getUnidadMedida()))
                .setHeader("Stock Actual").setSortable(true);

        // NUEVA COLUMNA: Etiqueta descriptiva del estado
        gridInsumos.addColumn(new ComponentRenderer<>(insumo -> {
            com.vaadin.flow.component.html.Span estado = new com.vaadin.flow.component.html.Span();
            if (insumo.getStockActual() <= insumo.getStockMinimo()) {
                estado.setText("Estado crítico, reabastecimiento necesario");
                estado.getElement().getThemeList().add("badge error");
            } else {
                estado.setText("Niveles Normales");
                estado.getElement().getThemeList().add("badge success");
            }
            return estado;
        })).setHeader("Estado de Stock").setWidth("350px").setFlexGrow(0);

        gridInsumos.addColumn(insumo -> String.format("%.2f %s", insumo.getStockMinimo(), insumo.getUnidadMedida())).setHeader("Stock Mín.");
        gridInsumos.addColumn(insumo -> String.format("$%.2f", insumo.getCostoUnitario())).setHeader("Costo Unit.");

        gridInsumos.addColumn(new ComponentRenderer<>(insumo -> {
            Button btnEliminarFila = new Button(VaadinIcon.TRASH.create());
            btnEliminarFila.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            btnEliminarFila.getStyle().set("color", "var(--lumo-error-text-color)");
            btnEliminarFila.addClickListener(e -> confirmarEliminacion(insumo));
            return btnEliminarFila;
        })).setHeader("Eliminar").setWidth("90px").setFlexGrow(0);

        // Evento de selección: Al tocar una fila de la tabla, carga los datos en el formulario lateral
        gridInsumos.asSingleSelect().addValueChangeListener(event -> cargarInsumoParaEditar(event.getValue()));

        colTabla.add(tituloInventario, gridInsumos);

        // =========================================================================
        // COLUMNA DERECHA: Formulario de Gestión y Abastecimiento (30%)
        // =========================================================================
        VerticalLayout colFormulario = new VerticalLayout();
        colFormulario.setWidth("30%");
        colFormulario.setHeightFull();
        colFormulario.getStyle().set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "20px");

        H3 tituloForm = new H3("Ingreso / Edición de Materia Prima");

        // Formateo de controles web
        txtNombre.setWidthFull();
        txtNombre.setRequired(true);

        cbCategoria.setItems("Carnes", "Lácteos", "Verduras/Frutas", "Abarrotes", "Bebidas", "Otros");
        cbCategoria.setWidthFull();
        cbCategoria.setRequired(true);

        numStockActual.setWidthFull();
        numStockMinimo.setWidthFull();
        numCostoUnitario.setWidthFull();
        numStockActual.setMin(0);
        numStockMinimo.setMin(0);
        numCostoUnitario.setMin(0);

        cbUnidadMedida.setItems("kg", "lt", "unidad", "porciones");
        cbUnidadMedida.setWidthFull();
        cbUnidadMedida.setRequired(true);

        FormLayout formInsumo = new FormLayout();
        formInsumo.setWidthFull();
        formInsumo.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("900px", 2)
        );
        formInsumo.add(txtNombre, cbCategoria, numStockActual, numStockMinimo, numCostoUnitario, cbUnidadMedida);

        // Estilos y eventos para botones
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.setWidthFull();
        btnGuardar.addClickListener(e -> guardarInsumo());

        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);
        btnEliminar.setWidthFull();
        btnEliminar.setVisible(true);
        btnEliminar.setEnabled(false);
        btnEliminar.addClickListener(e -> confirmarEliminacionSeleccionada());

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setWidthFull();
        btnLimpiar.addClickListener(e -> limpiarFormulario());

        HorizontalLayout acciones = new HorizontalLayout(btnGuardar, btnEliminar);
        acciones.setWidthFull();
        acciones.setSpacing(true);
        acciones.setFlexGrow(1, btnGuardar, btnEliminar);

        colFormulario.add(tituloForm, formInsumo, acciones, btnLimpiar);

        // Agregamos las dos columnas estructurales a la interfaz
        add(colTabla, colFormulario);

        // Consultamos la base de datos al inicializar la pestaña
        actualizarTablaInsumos();
        actualizarEstadoBotonEliminar();

    }

    /**
     * Sincroniza la tabla de Vaadin con los registros almacenados en SQLite
     */
    private void actualizarTablaInsumos() {
        List<Insumo> insumosBD = insumoDAO.listarTodos();
        gridInsumos.setItems(insumosBD);
    }



    /**
     * Lee el formulario y decide si debe registrar una inserción o una actualización en la BD
     */
    private void guardarInsumo() {
        if (txtNombre.getValue().isEmpty() || cbCategoria.getValue() == null || cbUnidadMedida.getValue() == null) {
            Notification.show("Por favor, complete los campos obligatorios.", 3000, Notification.Position.MIDDLE);
            return;
        }

        double stock = numStockActual.getValue() != null ? numStockActual.getValue() : 0.0;
        double minimo = numStockMinimo.getValue() != null ? numStockMinimo.getValue() : 0.0;
        double costo = numCostoUnitario.getValue() != null ? numCostoUnitario.getValue() : 0.0;

        if (stock < 0 || minimo < 0 || costo < 0) {
            Notification.show("Los valores de stock y costo no pueden ser negativos.", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (insumoSeleccionado == null) {
            // OPERACIÓN: Insertar nuevo insumo
            Insumo nuevoInsumo = new Insumo(0, txtNombre.getValue(), cbCategoria.getValue(), stock, minimo, costo, cbUnidadMedida.getValue());
            if (insumoDAO.insertar(nuevoInsumo)) {
                Notification.show("¡Materia prima agregada con éxito!", 2000, Notification.Position.BOTTOM_END);
            } else {
                Notification.show("Error al insertar el registro en la base de datos", 3000, Notification.Position.MIDDLE);
            }
        } else {
            // OPERACIÓN: Actualizar insumo existente seleccionado
            insumoSeleccionado.setNombre(txtNombre.getValue());
            insumoSeleccionado.setCategoria(cbCategoria.getValue());
            insumoSeleccionado.setStockActual(stock);
            insumoSeleccionado.setStockMinimo(minimo);
            insumoSeleccionado.setCostoUnitario(costo);
            insumoSeleccionado.setUnidadMedida(cbUnidadMedida.getValue());

            if (insumoDAO.actualizar(insumoSeleccionado)) {
                Notification.show("¡Registro actualizado correctamente!", 2000, Notification.Position.BOTTOM_END);
            } else {
                Notification.show("Error al intentar actualizar en SQLite", 3000, Notification.Position.MIDDLE);
            }
        }

        limpiarFormulario();
        actualizarTablaInsumos();
    }

    private void confirmarEliminacion(Insumo insumo) {
        if (insumo == null) {
            return;
        }

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmar eliminación");
        dialog.setText("¿Estás seguro de que deseas eliminar este insumo?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Eliminar");
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelText("Cancelar");

        dialog.addConfirmListener(event -> {
            boolean eliminado = inventarioService.eliminarInsumo(insumo.getId());
            if (eliminado) {
                Notification.show("¡Insumo eliminado con éxito!", 2500, Notification.Position.BOTTOM_END);
                limpiarFormulario();
                actualizarTablaInsumos();
            } else {
                Notification.show("No se pudo eliminar el insumo.", 3000, Notification.Position.MIDDLE);
            }
        });

        dialog.open();
    }

    private void confirmarEliminacionSeleccionada() {
        if (insumoSeleccionado != null) {
            confirmarEliminacion(insumoSeleccionado);
        }
    }

    private void actualizarEstadoBotonEliminar() {
        boolean haySeleccion = insumoSeleccionado != null;
        btnEliminar.setEnabled(haySeleccion);
    }

    /**
     * Mapea el elemento seleccionado en la tabla y rellena los campos del formulario
     */
    private void cargarInsumoParaEditar(Insumo seleccionado) {
        if (seleccionado == null) return;

        insumoSeleccionado = seleccionado;
        txtNombre.setValue(seleccionado.getNombre());
        cbCategoria.setValue(seleccionado.getCategoria());
        numStockActual.setValue(seleccionado.getStockActual());
        numStockMinimo.setValue(seleccionado.getStockMinimo());
        numCostoUnitario.setValue(seleccionado.getCostoUnitario());
        cbUnidadMedida.setValue(seleccionado.getUnidadMedida());

        btnGuardar.setText("Actualizar Datos");
        btnGuardar.setIcon(VaadinIcon.REFRESH.create());
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        actualizarEstadoBotonEliminar();
    }

    /**
     * Limpia los componentes y resetea los estados de selección
     */
    private void limpiarFormulario() {
        insumoSeleccionado = null;
        txtNombre.clear();
        cbCategoria.clear();
        numStockActual.clear();
        numStockMinimo.clear();
        numCostoUnitario.clear();
        cbUnidadMedida.clear();

        gridInsumos.asSingleSelect().clear();
        btnGuardar.setText("Guardar Insumo");
        btnGuardar.setIcon(VaadinIcon.DISC.create());
        btnGuardar.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
        actualizarEstadoBotonEliminar();
    }
}