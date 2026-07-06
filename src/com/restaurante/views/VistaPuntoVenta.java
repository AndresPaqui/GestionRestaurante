package com.restaurante.views;

import com.restaurante.data.PlatoDAO;
import com.restaurante.data.RecetaDAO;
import com.restaurante.logic.InventarioService;
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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Route(value = "", layout = MainLayout.class)
public class VistaPuntoVenta extends HorizontalLayout {

    private final PlatoDAO platoDAO = new PlatoDAO();
    private final VentasService ventasService = new VentasService();
    private final RecetaDAO recetaDAO = new RecetaDAO(); // Para validar si el plato tiene receta
    private final InventarioService inventarioService = new InventarioService(); // Para el indicador de stock

    private final List<ItemVenta> carrito = new ArrayList<>();

    private final VerticalLayout contenedorPlatos = new VerticalLayout();
    private final Grid<ItemVenta> gridPedido = new Grid<>(ItemVenta.class, false);
    private final Span lblSubtotal = new Span("Subtotal: $0.00");
    private final Span lblIva = new Span("IVA (15%): $0.00");
    private final Span lblTotal = new Span("TOTAL: $0.00");

    // El nuevo indicador visual de stock global
    private final Span badgeStockGlobal = new Span();

    private final RadioButtonGroup<String> rgTipoDocumento = new RadioButtonGroup<>();
    private final TextField txtDocumento = new TextField("Número de Documento");
    private final TextField txtCliente = new TextField("Nombre del Cliente");
    private final ComboBox<String> cbMetodoPago = new ComboBox<>("Método de Pago");
    private final Button btnCobrar = new Button("Procesar Venta", VaadinIcon.CASH.create());

    public VistaPuntoVenta() {
        setSizeFull();
        setSpacing(true);

        // =========================================================================
        // COLUMNA IZQUIERDA
        // =========================================================================
        VerticalLayout colIzquierda = new VerticalLayout();
        colIzquierda.setWidth("60%");
        colIzquierda.setHeightFull();

        HorizontalLayout barraAcciones = new HorizontalLayout();
        barraAcciones.setWidthFull();
        barraAcciones.setAlignItems(Alignment.BASELINE);

        TextField txtBuscar = new TextField();
        txtBuscar.setPlaceholder("Buscar plato por nombre...");
        txtBuscar.setPrefixComponent(VaadinIcon.SEARCH.create());
        txtBuscar.setClearButtonVisible(true);
        barraAcciones.setFlexGrow(1, txtBuscar);
        txtBuscar.addValueChangeListener(e -> cargarCatalogoPlatos(e.getValue()));

        Button btnVerHistorial = new Button("Ventas Realizadas", VaadinIcon.FILE_TEXT.create());
        btnVerHistorial.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnVerHistorial.addClickListener(e -> abrirModalHistorial());

        barraAcciones.add(txtBuscar, btnVerHistorial);

        H2 tituloPlatos = new H2("Listado de Platos");

        contenedorPlatos.setWidthFull();
        contenedorPlatos.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "1fr 1fr")
                .set("gap", "15px")

                // ── AQUÍ PONEMOS LA PATA DE POLLO AL FONDO DE TODO EL CONTENEDOR ──
                // Usamos un degradado sutil encima para que el fondo sea claro y no tape las letras
                .set("background-image", "linear-gradient(rgba(245, 245, 245, 0.9), rgba(245, 245, 245, 0.9)), url('https://img.icons8.com/color/500/chicken-thigh.png')")
                .set("background-repeat", "no-repeat")
                .set("background-position", "center") // Centrada en medio de todo el catálogo
                .set("background-size", "300px")       // Un tamaño grande para que abarque varias recetas
                .set("padding", "15px")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("background-color", "var(--lumo-contrast-5pct)");

        colIzquierda.add(barraAcciones, tituloPlatos, contenedorPlatos);

        // =========================================================================
        // COLUMNA DERECHA
        // =========================================================================
        VerticalLayout colDerecha = new VerticalLayout();
        colDerecha.setWidth("40%");
        colDerecha.setHeightFull();
        colDerecha.getStyle().set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "20px");

        // NUEVO: Cabecera del pedido con indicador de stock integrado
        H3 tituloPedido = new H3("Resumen del Pedido");
        tituloPedido.getStyle().set("margin", "0");
        HorizontalLayout cabeceraPedido = new HorizontalLayout(tituloPedido, badgeStockGlobal);
        cabeceraPedido.setAlignItems(Alignment.CENTER);
        cabeceraPedido.setJustifyContentMode(JustifyContentMode.BETWEEN);
        cabeceraPedido.setWidthFull();

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

        H3 tituloFactura = new H3("Datos de Facturación");

        rgTipoDocumento.setLabel("Tipo de Documento");
        rgTipoDocumento.setItems("Cédula", "RUC");
        rgTipoDocumento.setValue("Cédula");

        txtDocumento.setWidthFull();
        txtDocumento.setAllowedCharPattern("[0-9]");

        // --- CONEXIÓN DE ALERTAS FLOTANTES OPTIMIZADAS (UX FEEDBACK) ---

        // Alerta instantánea si presionan una letra en el documento
        txtDocumento.getElement().addEventListener("keydown", event -> {
            Notification.show("⚠️ Formato incorrecto: Solo se permiten números [0-9] en este campo.",
                            2000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }).setFilter("event.key.length === 1 && !/[0-9]/.test(event.key) && event.key !== 'Enter'");

        // Alerta instantánea si presionan un número en el nombre del cliente
        txtCliente.getElement().addEventListener("keydown", event -> {
            Notification.show("⚠️ Formato incorrecto: No ingrese números en el nombre del cliente.",
                            2000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }).setFilter("/[0-9]/.test(event.key)");

        txtDocumento.setPlaceholder("9999999999");
        configurarMaximoDocumento(rgTipoDocumento.getValue());

        rgTipoDocumento.addValueChangeListener(event -> {
            String tipo = event.getValue() == null ? "Cédula" : event.getValue();
            configurarMaximoDocumento(tipo);

            String valorActual = txtDocumento.getValue();
            int maxLen = "RUC".equals(tipo) ? 13 : 10;
            if (valorActual != null && valorActual.length() > maxLen) {
                txtDocumento.setValue(valorActual.substring(0, maxLen));
            }
        });

        txtCliente.setWidthFull();
        txtCliente.setPlaceholder("Consumidor Final");
        txtCliente.setAllowedCharPattern("[A-Za-zÁÉÍÓÚáéíóúÑñüÜ ]");

        cbMetodoPago.setItems("Efectivo", "Tarjeta de Crédito", "Transferencia");
        cbMetodoPago.setValue("Efectivo");
        cbMetodoPago.setWidthFull();

        btnCobrar.setWidthFull();
        btnCobrar.addClickListener(e -> procesarVenta());

        colDerecha.add(cabeceraPedido, gridPedido, seccionTotales, tituloFactura, rgTipoDocumento, txtDocumento, txtCliente, cbMetodoPago, btnCobrar);

        add(colIzquierda, colDerecha);

        cargarCatalogoPlatos("");
        actualizarResumenPedido();
        actualizarIndicadorStockGlobal(); // Revisa el stock al cargar la página
    }

    /**
     * Revisa si hay insumos en peligro y actualiza la etiqueta visual junto a "Resumen del pedido"
     */
    private void actualizarIndicadorStockGlobal() {
        List<Insumo> alertas = inventarioService.obtenerAlertasStock();
        badgeStockGlobal.getElement().getThemeList().clear();

        if (alertas.isEmpty()) {
            badgeStockGlobal.setText("Stock Óptimo");
            badgeStockGlobal.getElement().getThemeList().add("badge success");
        } else {
            badgeStockGlobal.setText("Stock Peligroso (" + alertas.size() + " insumos)");
            badgeStockGlobal.getElement().getThemeList().add("badge error");
        }
    }

    private void cargarCatalogoPlatos(String filtro) {
        contenedorPlatos.removeAll();
        List<Plato> platosBD = platoDAO.listarTodos();

        List<Plato> platosFiltrados = platosBD.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(filtro.toLowerCase()))
                .toList();

        for (Plato plato : platosFiltrados) {
            HorizontalLayout tarjetaPlato = new HorizontalLayout();
            tarjetaPlato.setWidthFull();
            tarjetaPlato.getStyle()
                    .set("padding", "10px")
                    .set("border", "1px solid var(--lumo-contrast-20pct)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("align-items", "center")
                    .set("justify-content", "space-between")

                    // ── FONDO TRANSLÚCIDO PARA QUE SE VEA EL FONDO GENERAL ABAJO ──
                    .set("background-color", "rgba(255, 255, 255, 0.85)")
                    .set("box-shadow", "var(--lumo-box-shadow-xs)");

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
        // --- PARTE A: RESTRICCIÓN DE RECETA VACÍA ---
        Map<Insumo, Double> receta = recetaDAO.obtenerIngredientesPorPlato(plato.getId());
        if (receta == null || receta.isEmpty()) {
            Notification.show("❌ El plato '" + plato.getNombre() + "' no tiene receta configurada. No se puede vender.", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return; // Bloqueamos la acción, no se agrega al carrito
        }

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

    private void actualizarResumenPedido() {
        gridPedido.setItems(carrito);
        gridPedido.getDataProvider().refreshAll();

        double subtotalSinIva = carrito.stream().mapToDouble(ItemVenta::getSubtotal).sum();
        double iva = subtotalSinIva * 0.15;
        double total = subtotalSinIva + iva;

        lblSubtotal.setText(String.format("Subtotal: $%.2f", subtotalSinIva));
        lblIva.setText(String.format("IVA (15%%): $%.2f", iva));
        lblTotal.setText(String.format("TOTAL: $%.2f", total));

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

        String tipoDocumento = rgTipoDocumento.getValue() == null ? "Cédula" : rgTipoDocumento.getValue();
        String documento = txtDocumento.getValue() == null ? "" : txtDocumento.getValue().trim();

        if (!documento.isEmpty()) {
            int longitudEsperada = "RUC".equals(tipoDocumento) ? 13 : 10;
            if (documento.length() != longitudEsperada) {
                Notification.show("El " + tipoDocumento + " debe tener exactamente " + longitudEsperada + " dígitos.",
                                3500, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
        }

        // Validación final preventiva por si usaron "Copiar y Pegar" con el mouse
        String clienteNombre = txtCliente.getValue() == null ? "" : txtCliente.getValue().trim();
        if (clienteNombre.matches(".*\\d.*")) { // Si contiene cualquier dígito del 0 al 9
            Notification.show("❌ Error: El nombre del cliente no puede contener números.",
                            3500, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Venta nuevaVenta = new Venta(0, LocalDateTime.now(), new ArrayList<>(carrito), total, cbMetodoPago.getValue());

        String mensajeFactura;
        if (documento.isEmpty()) {
            nuevaVenta.setClienteNombre("Consumidor Final");
            nuevaVenta.setClienteCedula("9999999999");
            mensajeFactura = "Factura guardada con consumidor final";
        } else {
            nuevaVenta.setClienteNombre(txtCliente.getValue().isEmpty() ? "Consumidor Final" : txtCliente.getValue());
            nuevaVenta.setClienteCedula(documento);
            mensajeFactura = "RUC".equals(tipoDocumento)
                    ? "Factura guardada con RUC"
                    : "Factura guardada con cédula";
        }

        String validacionStock = ventasService.validarDisponibilidadStock(nuevaVenta);
        if (!validacionStock.equals("OK")) {
            Notification.show("❌ VENTA RECHAZADA: " + validacionStock, 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (ventasService.registrarVentaCompleta(nuevaVenta)) {
            Notification.show(mensajeFactura, 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            carrito.clear();
            txtCliente.clear();
            txtDocumento.clear();
            rgTipoDocumento.setValue("Cédula");
            configurarMaximoDocumento("Cédula");
            actualizarResumenPedido();
            actualizarIndicadorStockGlobal(); // Forzamos actualización visual tras descontar la bodega
        } else {
            Notification.show("Error crítico al procesar la venta a través del servicio", 3000, Notification.Position.MIDDLE);
        }
    }

    private void configurarMaximoDocumento(String tipoDocumento) {
        boolean esRuc = "RUC".equals(tipoDocumento);
        txtDocumento.setMaxLength(esRuc ? 13 : 10);
        txtDocumento.setPlaceholder(esRuc ? "9999999999999" : "9999999999");
    }

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
                lineaCabecera.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
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
                                filaItem.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
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
