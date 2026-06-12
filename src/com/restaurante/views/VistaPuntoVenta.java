package com.restaurante.views;

import com.restaurante.data.PlatoDAO;
import com.restaurante.logic.VentasService;
import com.restaurante.model.Insumo;
import com.restaurante.model.ItemVenta;
import com.restaurante.model.Plato;
import com.restaurante.model.Venta;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
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
    private final VentasService ventasService = new VentasService();
    /*private final com.restaurante.logic.InventarioService inventarioService = new com.restaurante.logic.InventarioService();*/

    // Lista del carrito actual en memoria
    private final List<ItemVenta> carrito = new ArrayList<>();

    // Componentes visuales que necesitamos actualizar dinámicamente
    private final VerticalLayout contenedorPlatos = new VerticalLayout();
    private final Grid<ItemVenta> gridPedido = new Grid<>(ItemVenta.class, false);
    private final Span lblSubtotal = new Span("Subtotal: $0.00");
    private final Span lblIva = new Span("IVA (15%): $0.00");
    private final Span lblTotal = new Span("TOTAL: $0.00");

    // Campos de facturación con marcas de agua dinámicas
    private final TextField txtCedula = new TextField("Cédula / RUC");
    private final TextField txtCliente = new TextField("Nombre del Cliente");
    private final ComboBox<String> cbMetodoPago = new ComboBox<>("Método de Pago");
    private final Button btnCobrar = new Button("Procesar Venta", VaadinIcon.CASH.create());

    public VistaPuntoVenta() {
        setSizeFull();
        setSpacing(true);

        // =========================================================================
        // COLUMNA IZQUIERDA: Buscador, Catálogo de Platos e Historial (60%)
        // =========================================================================
        VerticalLayout colIzquierda = new VerticalLayout();
        colIzquierda.setWidth("60%");
        colIzquierda.setHeightFull();

        // Barra superior con buscador y acceso al historial operativo
        HorizontalLayout barraAcciones = new HorizontalLayout();
        barraAcciones.setWidthFull();
        barraAcciones.setAlignItems(Alignment.BASELINE);

        TextField txtBuscar = new TextField();
        txtBuscar.setPlaceholder("Buscar plato por nombre...");
        txtBuscar.setPrefixComponent(VaadinIcon.SEARCH.create());
        txtBuscar.setClearButtonVisible(true);
        barraAcciones.setFlexGrow(1, txtBuscar);

        txtBuscar.addValueChangeListener(e -> cargarCatalogoPlatos(e.getValue()));

        // NUEVO BOTÓN: Ver Ventas Realizadas (Historial Emergente)
        Button btnVerHistorial = new Button("Ventas Realizadas", VaadinIcon.FILE_TEXT.create());
        btnVerHistorial.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnVerHistorial.addClickListener(e -> abrirModalHistorial());

        barraAcciones.add(txtBuscar, btnVerHistorial);

        H2 tituloPlatos = new H2("Listado de Platos");

        contenedorPlatos.setWidthFull();
        contenedorPlatos.getStyle().set("display", "grid")
                .set("grid-template-columns", "1fr 1fr")
                .set("gap", "15px");

        colIzquierda.add(barraAcciones, tituloPlatos, contenedorPlatos);

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

        gridPedido.setHeight("250px");
        gridPedido.addColumn(ItemVenta::getCantidad).setHeader("Cant.").setWidth("80px").setFlexGrow(0);
        gridPedido.addColumn(item -> item.getPlato().getNombre()).setHeader("Plato");
        gridPedido.addColumn(item -> String.format("$%.2f", item.getSubtotal())).setHeader("Subtotal").setWidth("100px").setFlexGrow(0);

        gridPedido.addColumn(new ComponentRenderer<>(item -> {
            Button btnEliminar = new Button(VaadinIcon.TRASH.create());
            btnEliminar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            btnEliminar.addClickListener(e -> eliminarDelPedido(item));
            return btnEliminar;
        })).setHeader("Quitar").setWidth("80px").setFlexGrow(0);

        VerticalLayout seccionTotales = new VerticalLayout();
        seccionTotales.setPadding(false);
        seccionTotales.setSpacing(false);
        seccionTotales.getStyle().set("font-weight", "bold").set("border-top", "1px solid var(--lumo-contrast-20pct)");
        seccionTotales.add(lblSubtotal, lblIva, lblTotal);

        lblTotal.getStyle().set("font-size", "var(--lumo-font-size-xl)").set("color", "var(--lumo-primary-text-color)");

        // Sección de Facturación con los Placeholders en Gris solicitados
        H3 tituloFactura = new H3("Datos de Facturación");

        txtCedula.setWidthFull();
        txtCedula.setPlaceholder("9999999999"); // Leyenda indicativa en gris

        txtCliente.setWidthFull();
        txtCliente.setPlaceholder("Consumidor Final"); // Leyenda indicativa en gris

        cbMetodoPago.setItems("Efectivo", "Tarjeta de Crédito", "Transferencia");
        cbMetodoPago.setValue("Efectivo");
        cbMetodoPago.setWidthFull();

        btnCobrar.setWidthFull();
        btnCobrar.addClickListener(e -> procesarVenta());

        colDerecha.add(tituloPedido, gridPedido, seccionTotales, tituloFactura, txtCedula, txtCliente, cbMetodoPago, btnCobrar);

        add(colIzquierda, colDerecha);

        cargarCatalogoPlatos("");
        actualizarResumenPedido();
    }
/*
    *//**
     * Alerta general para el cajero sobre el estado del inventario
     *//*
    private void verificarEstadoInventario() {
        List<Insumo> alertas = inventarioService.obtenerAlertasStock();
        if (alertas.size() == 1) {
            Notification.show("⚠️ Atención: Un insumo está por agotarse en bodega.", 4000, Notification.Position.TOP_END)
                    .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_WARNING);
        } else if (alertas.size() > 1) {
            Notification.show("⚠️ Atención: Múltiples insumos están por agotarse en bodega.", 4000, Notification.Position.TOP_END)
                    .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_WARNING);
        }
    }*/

    private void cargarCatalogoPlatos(String filtro) {
        contenedorPlatos.removeAll();
        List<Plato> platosBD = platoDAO.listarTodos();

        List<Plato> platosFiltrados = platosBD.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(filtro.toLowerCase()))
                .collect(Collectors.toList());

        for (Plato plato : platosFiltrados) {
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

    private void agregarAlPedido(Plato plato) {
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

    private void eliminarDelPedido(ItemVenta item) {
        if (item.getCantidad() > 1) {
            item.setCantidad(item.getCantidad() - 1);
            item.setSubtotal(item.getCantidad() * item.getPlato().getPrecioVenta());
        } else {
            carrito.remove(item);
        }
        actualizarResumenPedido();
    }

    /**
     * Ajuste Dinámico de Estado del Botón Procesar Venta
     */
    private void actualizarResumenPedido() {
        gridPedido.setItems(carrito);
        gridPedido.getDataProvider().refreshAll();

        double subtotalSinIva = carrito.stream().mapToDouble(ItemVenta::getSubtotal).sum();
        double iva = subtotalSinIva * 0.15;
        double total = subtotalSinIva + iva;

        lblSubtotal.setText(String.format("Subtotal: $%.2f", subtotalSinIva));
        lblIva.setText(String.format("IVA (15%%): $%.2f", iva));
        lblTotal.setText(String.format("TOTAL: $%.2f", total));

        // AJUSTE UX SOLICITADO: Control de opacidad y estados del botón
        if (carrito.isEmpty()) {
            btnCobrar.setEnabled(false);
            btnCobrar.removeThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            btnCobrar.getStyle().set("opacity", "0.5").set("cursor", "not-allowed");
        } else {
            btnCobrar.setEnabled(true);
            btnCobrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            btnCobrar.getStyle().set("opacity", "1.0").set("cursor", "pointer");
        }
    }

    private void procesarVenta() {
        if (carrito.isEmpty()) return;

        double total = carrito.stream().mapToDouble(ItemVenta::getSubtotal).sum() * 1.15;

        Venta nuevaVenta = new Venta(0, LocalDateTime.now(), new ArrayList<>(carrito), total, cbMetodoPago.getValue());
        nuevaVenta.setClienteNombre(txtCliente.getValue().isEmpty() ? "Consumidor Final" : txtCliente.getValue());
        nuevaVenta.setClienteCedula(txtCedula.getValue().isEmpty() ? "9999999999" : txtCedula.getValue());

        // --- NUEVA VALIDACIÓN DE STOCK ---
        String validacionStock = ventasService.validarDisponibilidadStock(nuevaVenta);
        if (!validacionStock.equals("OK")) {
            // Si falta materia prima, lanzamos alerta roja y cancelamos la venta
            Notification.show("❌ VENTA RECHAZADA: " + validacionStock, 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
            return;
        }

        if (ventasService.registrarVentaCompleta(nuevaVenta)) {
            Notification.show("¡Venta procesada con éxito con ID: " + nuevaVenta.getIdVenta() + "!", 3000, Notification.Position.MIDDLE);
            carrito.clear();
            txtCliente.clear();
            txtCedula.clear();
            actualizarResumenPedido();
        } else {
            Notification.show("Error crítico al procesar la venta a través del servicio", 3000, Notification.Position.MIDDLE);
        }
    }
    /**
     * Ventana emergente con el historial de facturación y desglose de platos comprados
     */
    private void abrirModalHistorial() {
        Dialog modal = new Dialog();
        modal.setHeaderTitle("Últimas Ventas Realizadas");
        modal.setWidth("550px");
        modal.setHeight("600px");

        VerticalLayout contenedorTarjetas = new VerticalLayout();
        contenedorTarjetas.setWidthFull();
        contenedorTarjetas.setPadding(false);

        List<Venta> historial = ventasService.obtenerHistorialVentas();

        if (historial.isEmpty()) {
            contenedorTarjetas.add(new Span("No se registran transacciones en el sistema comercial."));
        } else {
            for (Venta v : historial) {
                // Tarjeta contenedora principal
                VerticalLayout tarjetaVenta = new VerticalLayout();
                tarjetaVenta.setWidthFull();
                tarjetaVenta.getStyle()
                        .set("padding", "15px")
                        .set("border", "1px solid var(--lumo-contrast-20pct)")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("background-color", "var(--lumo-base-color)")
                        .set("cursor", "pointer")
                        .set("transition", "border-color 0.2s, box-shadow 0.2s");

                HorizontalLayout lineaCabecera = new HorizontalLayout(
                        new Span("Factura #" + v.getIdVenta()),
                        new Span(v.getClienteNombre()),
                        new Span(String.format("Total: $%.2f", v.getTotalVenta()))
                );
                lineaCabecera.setWidthFull();
                lineaCabecera.setJustifyContentMode(JustifyContentMode.BETWEEN);
                lineaCabecera.getStyle().set("font-weight", "bold");

                // Sección oculta con los detalles de facturación y el desglose de platos
                VerticalLayout panelDetalle = new VerticalLayout();
                panelDetalle.setWidthFull();
                panelDetalle.setPadding(false);
                panelDetalle.setVisible(false); // Oculto por defecto

                panelDetalle.add(new Hr());
                panelDetalle.add(new Span("Fecha/Hora: " + v.getFecha().toString().replace("T", " ")));
                panelDetalle.add(new Span("Identificación: " + v.getClienteCedula()));
                panelDetalle.add(new Span("Método de Pago: " + v.getMetodoPago()));

                // Contenedor específico para pintar los platos de la comanda
                VerticalLayout seccionItems = new VerticalLayout();
                seccionItems.setWidthFull();
                seccionItems.setPadding(true);
                seccionItems.getStyle().set("background-color", "var(--lumo-contrast-5pct)")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("margin-top", "10px");

                Span txtCargando = new Span("Cargando productos...");
                seccionItems.add(txtCargando);
                panelDetalle.add(seccionItems);

                // Evento Toggle interactivo para colapsar o expandir al dar clic en la tarjeta
                tarjetaVenta.addClickListener(e -> {
                    boolean visible = panelDetalle.isVisible();
                    panelDetalle.setVisible(!visible);

                    if (!visible) {
                        tarjetaVenta.getStyle().set("border-color", "var(--lumo-primary-color)")
                                .set("box-shadow", "var(--lumo-box-shadow-xs)");

                        // CARGA PEREZOSA (Lazy Loading): Solo va a la BD si el usuario expande la tarjeta
                        seccionItems.removeAll();
                        List<ItemVenta> productos = ventasService.obtenerDetallesPorVenta(v.getIdVenta());

                        if (productos.isEmpty()) {
                            seccionItems.add(new Span("Sin detalles de productos registrados."));
                        } else {
                            H3 subtituloPlatos = new H3("Platos Consumidos:");
                            subtituloPlatos.getStyle().set("font-size", "var(--lumo-font-size-s)").set("margin", "0");
                            seccionItems.add(subtituloPlatos);

                            for (ItemVenta item : productos) {
                                HorizontalLayout filaItem = new HorizontalLayout(
                                        new Span(item.getCantidad() + "x  " + item.getPlato().getNombre()),
                                        new Span(String.format("$%.2f", item.getSubtotal()))
                                );
                                filaItem.setWidthFull();
                                filaItem.setJustifyContentMode(JustifyContentMode.BETWEEN);
                                filaItem.getStyle().set("font-size", "var(--lumo-font-size-s)");
                                seccionItems.add(filaItem);
                            }
                        }
                    } else {
                        tarjetaVenta.getStyle().set("border-color", "var(--lumo-contrast-20pct)")
                                .set("box-shadow", "none");
                    }
                });

                tarjetaVenta.add(lineaCabecera, panelDetalle);
                contenedorTarjetas.add(tarjetaVenta);
            }
        }

        Scroller scroller = new Scroller(contenedorTarjetas);
        scroller.setSizeFull();
        modal.add(scroller);

        Button btnCerrar = new Button("Cerrar", e -> modal.close());
        modal.getFooter().add(btnCerrar);
        modal.open();
    }
}