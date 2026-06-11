package com.restaurante.views;

import com.restaurante.data.PlatoDAO;
import com.restaurante.data.VentasDAO;
import com.restaurante.model.ItemVenta;
import com.restaurante.model.Plato;
import com.restaurante.model.Venta;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "", layout = MainLayout.class)
public class VistaPuntoVenta extends HorizontalLayout {

    private final PlatoDAO platoDAO = new PlatoDAO();
    private final VentasDAO ventasDAO = new VentasDAO();

    // Lista del carrito actual en memoria
    private final List<ItemVenta> carrito = new ArrayList<>();

    // Componentes visuales que necesitamos actualizar dinámicamente
    private final VerticalLayout contenedorPlatos = new VerticalLayout();
    private final Grid<ItemVenta> gridPedido = new Grid<>(ItemVenta.class, false);
    private final Span lblSubtotal = new Span("Subtotal: $0.00");
    private final Span lblIva = new Span("IVA (15%): $0.00");
    private final Span lblTotal = new Span("TOTAL: $0.00");

    // Campos de facturación
    private final TextField txtCedula = new TextField("Cédula / RUC");
    private final TextField txtCliente = new TextField("Nombre del Cliente");
    private final ComboBox<String> cbMetodoPago = new ComboBox<>("Método de Pago");

    public VistaPuntoVenta() {
        setSizeFull();
        setSpacing(true);

        // =========================================================================
        // COLUMNA IZQUIERDA: Buscador y Catálogo de Platos (60%)
        // =========================================================================
        VerticalLayout colIzquierda = new VerticalLayout();
        colIzquierda.setWidth("60%");
        colIzquierda.setHeightFull();

        TextField txtBuscar = new TextField();
        txtBuscar.setPlaceholder("Buscar plato por nombre...");
        txtBuscar.setPrefixComponent(VaadinIcon.SEARCH.create());
        txtBuscar.setWidthFull();
        txtBuscar.setClearButtonVisible(true);
        // Filtrado en tiempo real al escribir
        txtBuscar.addValueChangeListener(e -> cargarCatalogoPlatos(e.getValue()));

        H2 tituloPlatos = new H2("Listado de Platos");

        // Estilo CSS Grid nativo para pintar los platos en DOS columnas limpias
        contenedorPlatos.setWidthFull();
        contenedorPlatos.getStyle().set("display", "grid")
                .set("grid-template-columns", "1fr 1fr")
                .set("gap", "15px");

        colIzquierda.add(txtBuscar, tituloPlatos, contenedorPlatos);

        // =========================================================================
        // COLUMNA DERECHA: Detalle del Pedido y Facturación (40%)
        // =========================================================================
        VerticalLayout colDerecha = new VerticalLayout();
        colDerecha.setWidth("40%");
        colDerecha.setHeightFull();
        colDerecha.getStyle().set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "20px");

        H3 tituloPedido = new H3("Resumen del Pedido");

        // Configurar tabla de ítems del pedido: Cantidad | Item | Subtotal
        gridPedido.setHeight("250px");
        gridPedido.addColumn(ItemVenta::getCantidad).setHeader("Cant.").setWidth("60px").setFlexGrow(0);
        gridPedido.addColumn(item -> item.getPlato().getNombre()).setHeader("Plato");
        gridPedido.addColumn(item -> String.format("$%.2f", item.getSubtotal())).setHeader("Subtotal").setWidth("100px").setFlexGrow(0);

        // NUEVA COLUMNA: Botón de eliminar ítem
        gridPedido.addColumn(new ComponentRenderer<>(item -> {
            Button btnEliminar = new Button(VaadinIcon.TRASH.create());
            btnEliminar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            btnEliminar.addClickListener(e -> eliminarDelPedido(item));
            return btnEliminar;
        })).setHeader("Quitar").setWidth("70px").setFlexGrow(0);

        // Sección de desglose de costos (Subtotal, IVA, Total)
        VerticalLayout seccionTotales = new VerticalLayout(lblSubtotal, lblIva, lblTotal);
        seccionTotales.setPadding(false);
        seccionTotales.setSpacing(false);
        seccionTotales.getStyle().set("font-weight", "bold").set("border-top", "1px solid var(--lumo-contrast-20pct)");
        lblTotal.getStyle().set("font-size", "var(--lumo-font-size-xl)").set("color", "var(--lumo-primary-text-color)");

        // Sección de Facturación
        H3 tituloFactura = new H3("Datos de Facturación");
        txtCedula.setWidthFull();
        txtCliente.setWidthFull();

        cbMetodoPago.setItems("Efectivo", "Tarjeta de Crédito", "Transferencia");
        cbMetodoPago.setValue("Efectivo");
        cbMetodoPago.setWidthFull();

        Button btnCobrar = new Button("Procesar Venta", VaadinIcon.CASH.create());
        btnCobrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnCobrar.setWidthFull();
        btnCobrar.addClickListener(e -> procesarVenta());

        colDerecha.add(tituloPedido, gridPedido, seccionTotales, tituloFactura, txtCedula, txtCliente, cbMetodoPago, btnCobrar);

        // Añadir las dos columnas principales a la pantalla
        add(colIzquierda, colDerecha);

        // Cargar los platos iniciales de la base de datos
        cargarCatalogoPlatos("");
    }

    /**
     * Dibuja dinámicamente las tarjetas de los platos en un formato de 2 columnas
     */
    private void cargarCatalogoPlatos(String filtro) {
        contenedorPlatos.removeAll();

        List<Plato> platosBD = platoDAO.listarTodos();

        // Si la base de datos está vacía, creamos platos ficticios temporales para probar la interfaz
        if (platosBD.isEmpty()) {
            platosBD.add(new Plato(1, "Encebollado Mixto", 5.50, 0, null));
            platosBD.add(new Plato(2, "Seco de Pollo", 4.00, 0, null));
            platosBD.add(new Plato(3, "Locro de Papa", 3.50, 0, null));
            platosBD.add(new Plato(4, "Churrasco Ecuatoriano", 6.50, 0, null));
        }

        // Filtrar por nombre si el usuario escribe en la barra superior
        List<Plato> platosFiltrados = platosBD.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(filtro.toLowerCase()))
                .collect(Collectors.toList());

        for (Plato plato : platosFiltrados) {
            // Fila horizontal que representa la tarjeta del plato
            HorizontalLayout tarjetaPlato = new HorizontalLayout();
            tarjetaPlato.setWidthFull();
            tarjetaPlato.getStyle().set("padding", "10px")
                    .set("border", "1px solid var(--lumo-contrast-20pct)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("align-items", "center")
                    .set("justify-content", "space-between");

            VerticalLayout infoPlato = new VerticalLayout(new Span(plato.getNombre()), new Span(String.format("$%.2f", plato.getPrecioVenta())));
            infoPlato.setSpacing(false);
            infoPlato.setPadding(false);

            Button btnAgregar = new Button(VaadinIcon.PLUS.create());
            btnAgregar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            btnAgregar.addClickListener(e -> agregarAlPedido(plato));

            tarjetaPlato.add(infoPlato, btnAgregar);
            contenedorPlatos.add(tarjetaPlato);
        }
    }

    /**
     * Gestiona la lógica del carrito de compras en el Punto de Venta
     */
    private void agregarAlPedido(Plato plato) {
        // Verificar si el plato ya estaba en el pedido
        ItemVenta itemExistente = carrito.stream()
                .filter(item -> item.getPlato().getId() == plato.getId())
                .findFirst().orElse(null);

        if (itemExistente != null) {
            itemExistente.setCantidad(itemExistente.getCantidad() + 1);
            itemExistente.setSubtotal(itemExistente.getCantidad() * plato.getPrecioVenta());
        } else {
            carrito.add(new ItemVenta(plato, 1, plato.getPrecioVenta()));
        }

        actualizarResumenPedido();
    }

    /**
     * Remueve un ítem completo o descuenta su cantidad del carrito de compras
     */
    private void eliminarDelPedido(ItemVenta item) {
        if (item.getCantidad() > 1) {
            // Si hay más de uno, solo restamos una unidad y recalculamos su subtotal
            item.setCantidad(item.getCantidad() - 1);
            item.setSubtotal(item.getCantidad() * item.getPlato().getPrecioVenta());
        } else {
            // Si solo queda uno, lo sacamos por completo de la lista
            carrito.remove(item);
        }
        // Refrescamos los costos en pantalla
        actualizarResumenPedido();
    }

    /**
     * Calcula los subtotales, IVA 15% y totales del resumen operativo
     */
    private void actualizarResumenPedido() {
        gridPedido.setItems(carrito);
        gridPedido.getDataProvider().refreshAll();

        double subtotalSinIva = carrito.stream().mapToDouble(ItemVenta::getSubtotal).sum();
        double iva = subtotalSinIva * 0.15; // 15% de IVA ecuatoriano
        double total = subtotalSinIva + iva;

        // CORRECCIÓN: Seteamos el texto formateado usando las variables correspondientes
        lblSubtotal.setText(String.format("Subtotal: $%.2f", subtotalSinIva));
        lblIva.setText(String.format("IVA (15%): $%.2f", iva));
        lblTotal.setText(String.format("TOTAL: $%.2f", total));
    }

    /**
     * Empaqueta el objeto Venta y lo manda a guardar a través de VentasDAO
     */
    private void procesarVenta() {
        if (carrito.isEmpty()) {
            Notification.show("Error: El pedido está vacío", 3000, Notification.Position.MIDDLE);
            return;
        }

        double total = carrito.stream().mapToDouble(ItemVenta::getSubtotal).sum() * 1.15;

        // Instanciamos el objeto de negocio unificado con los datos de facturación
        Venta nuevaVenta = new Venta(0, LocalDateTime.now(), new ArrayList<>(), total, cbMetodoPago.getValue());
        nuevaVenta.setClienteNombre(txtCliente.getValue().isEmpty() ? "Consumidor Final" : txtCliente.getValue());
        nuevaVenta.setClienteCedula(txtCedula.getValue().isEmpty() ? "9999999999" : txtCedula.getValue());

        // 1. Registrar cabecera de la venta
        if (ventasDAO.registrarVenta(nuevaVenta)) {
            // 2. Registrar los detalles correlacionales de la venta
            for (ItemVenta item : carrito) {
                ventasDAO.registrarDetalleVenta(item, nuevaVenta.getIdVenta());
            }

            Notification.show("¡Venta procesada con éxito con ID: " + nuevaVenta.getIdVenta() + "!", 3000, Notification.Position.MIDDLE);

            // Limpiar formulario para la siguiente orden
            carrito.clear();
            txtCliente.clear();
            txtCedula.clear();
            actualizarResumenPedido();
        } else {
            Notification.show("Error crítico al guardar la venta en SQLite", 3000, Notification.Position.MIDDLE);
        }
    }
}