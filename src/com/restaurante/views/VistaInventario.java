package com.restaurante.views;

import com.restaurante.data.InsumoDAO;
import com.restaurante.model.Insumo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
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
    private final Button btnLimpiar = new Button("Limpiar");

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

        // Renderizador de componentes: Píldora visual verde (Seguro) o roja (Crítico)
        gridInsumos.addColumn(new ComponentRenderer<>(insumo -> {
            Span badge = new Span(String.format("%.2f %s", insumo.getStockActual(), insumo.getUnidadMedida()));
            if (insumo.getStockActual() <= insumo.getStockMinimo()) {
                badge.getElement().getThemeList().add("badge error"); // CSS Nativo de Vaadin para estados de error
            } else {
                badge.getElement().getThemeList().add("badge success"); // CSS Nativo para estados estables
            }
            return badge;
        })).setHeader("Stock Actual").setSortable(true);

        gridInsumos.addColumn(insumo -> String.format("%.2f %s", insumo.getStockMinimo(), insumo.getUnidadMedida())).setHeader("Stock Mín.");
        gridInsumos.addColumn(insumo -> String.format("$%.2f", insumo.getCostoUnitario())).setHeader("Costo Unit.");

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

        cbUnidadMedida.setItems("kg", "lt", "unidad", "porciones");
        cbUnidadMedida.setWidthFull();
        cbUnidadMedida.setRequired(true);

        // Estilos y eventos para botones
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.setWidthFull();
        btnGuardar.addClickListener(e -> guardarInsumo());

        btnLimpiar.setWidthFull();
        btnLimpiar.addClickListener(e -> limpiarFormulario());

        colFormulario.add(tituloForm, txtNombre, cbCategoria, numStockActual, numStockMinimo, numCostoUnitario, cbUnidadMedida, btnGuardar, btnLimpiar);

        // Agregamos las dos columnas estructurales a la interfaz
        add(colTabla, colFormulario);

        // Consultamos la base de datos al inicializar la pestaña
        actualizarTablaInsumos();
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
    }
}