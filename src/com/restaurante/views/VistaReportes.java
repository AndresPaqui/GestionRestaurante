package com.restaurante.views;

import com.restaurante.logic.VentasService;
import com.restaurante.model.ItemVenta;
import com.restaurante.model.Venta;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "reportes", layout = MainLayout.class)
public class VistaReportes extends VerticalLayout {

    private final VentasService ventasService = new VentasService();

    // Componentes del Filtro Superior
    private final DatePicker dateInicio = new DatePicker("Fecha de Inicio");
    private final DatePicker dateFin = new DatePicker("Fecha de Fin");
    private final Button btnFiltrar = new Button("Filtrar Rango", VaadinIcon.FILTER.create());

    // Contenedores dinámicos que se refrescarán al filtrar
    private final HorizontalLayout contenedorKpis = new HorizontalLayout();
    private final VerticalLayout panelGrafico = new VerticalLayout();
    private final H3 tituloGrafico = new H3("Ranking de Platos más Solicitados");

    public VistaReportes() {
        setSizeFull();
        setSpacing(true);
        setPadding(true);

        H2 tituloPagina = new H2("Cuadro de Mando Financiero (Dashboard)");
        add(tituloPagina);

        // =========================================================================
        // BARRA DE HERRAMIENTAS: Filtro de Fechas (UX Limpia)
        // =========================================================================
        HorizontalLayout toolbarFiltros = new HorizontalLayout();
        toolbarFiltros.setWidthFull();
        toolbarFiltros.setAlignItems(Alignment.BASELINE);
        toolbarFiltros.setSpacing(true);

        // Configuramos los selectores de fecha en español de forma nativa
        dateInicio.setPlaceholder("Seleccione inicio");
        dateFin.setPlaceholder("Seleccione fin");

        // Inicializamos por defecto con el mes actual para no abrumar la vista inicial
        dateInicio.setValue(LocalDate.now().withDayOfMonth(1));
        dateFin.setValue(LocalDate.now());

        btnFiltrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnFiltrar.addClickListener(e -> cargarDatosDashboard(dateInicio.getValue(), dateFin.getValue()));

        toolbarFiltros.add(dateInicio, dateFin, btnFiltrar);
        add(toolbarFiltros);

        // =========================================================================
        // INICIALIZACIÓN DE CONTENEDORES VISUALES
        // =========================================================================
        contenedorKpis.setWidthFull();
        contenedorKpis.setSpacing(true);
        add(contenedorKpis);

        panelGrafico.setWidthFull();
        panelGrafico.getStyle().set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "25px")
                .set("margin-top", "15px");
        add(panelGrafico);

        // Carga inicial automática de datos basados en las fechas por defecto
        cargarDatosDashboard(dateInicio.getValue(), dateFin.getValue());
    }

    /**
     * El motor del Dashboard. Consulta al servicio basándose en las fechas
     * e inyecta dinámicamente los componentes en el DOM de Vaadin.
     */
    private void cargarDatosDashboard(LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            Notification.show("La fecha de inicio no puede ser posterior a la fecha de fin.", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Limpiamos los contenedores antes de re-dibujar
        contenedorKpis.removeAll();
        panelGrafico.removeAll();
        panelGrafico.add(tituloGrafico);

        // 1. OBTENCIÓN DE DATOS FILTRADOS DESDE EL SERVICE
        double ingresosTotales = valueOrZero(ventasService.calcularTotalVentasFacturadasPorFecha(inicio, fin));
        double costosMateriaPrima = valueOrZero(ventasService.calcularCostoTotalInvertidoPorFecha(inicio, fin));
        double utilidadNeta = ingresosTotales - costosMateriaPrima;

        // 2. RENDERIZADO DE LAS TARJETAS KPI
        VerticalLayout cardIngresos = crearTarjetaKpi("Ingresos Totales (Con IVA)",
                String.format("$%.2f", ingresosTotales), VaadinIcon.MONEY.create(), "var(--lumo-success-text-color)");

        VerticalLayout cardCostos = crearTarjetaKpi("Costo de Producción",
                String.format("$%.2f", costosMateriaPrima), VaadinIcon.CART.create(), "var(--lumo-error-text-color)");

        String colorGanancia = utilidadNeta < 0
                ? "var(--lumo-error-text-color)"
                : utilidadNeta > 0
                  ? "var(--lumo-success-text-color)"
                  : "var(--lumo-secondary-text-color)";

        VerticalLayout cardNeto = crearTarjetaKpi("Ganancia Neta Real",
                String.format("$%.2f", utilidadNeta), VaadinIcon.TRENDING_UP.create(), colorGanancia);

        contenedorKpis.add(cardIngresos, cardCostos, cardNeto);

        // 3. RENDERIZADO DEL GRÁFICO DE BARRAS FILTRADO
        Map<String, Integer> rankingPlatos = new HashMap<>();
        List<Venta> historialPeriodo = ventasService.obtenerHistorialVentasPorFecha(inicio, fin);

        for (Venta v : historialPeriodo) {
            List<ItemVenta> detalles = ventasService.obtenerDetallesPorVenta(v.getIdVenta());
            if (detalles != null) {
                for (ItemVenta item : detalles) {
                    String nombrePlato = item.getPlato().getNombre();
                    rankingPlatos.put(nombrePlato, rankingPlatos.getOrDefault(nombrePlato, 0) + item.getCantidad());
                }
            }
        }

        if (rankingPlatos.isEmpty()) {
            panelGrafico.add(new Span("No existen transacciones comerciales registradas en el rango de fechas seleccionado."));
        } else {
            int maxVentas = rankingPlatos.values().stream().mapToInt(Integer::intValue).max().orElse(1);

            for (Map.Entry<String, Integer> entry : rankingPlatos.entrySet()) {
                String plato = entry.getKey();
                int cantidad = entry.getValue();
                int porcentajeAncho = (cantidad * 100) / maxVentas;

                HorizontalLayout filaBarra = new HorizontalLayout();
                filaBarra.setWidthFull();
                filaBarra.setAlignItems(Alignment.CENTER);

                Span lblPlato = new Span(plato);
                lblPlato.setWidth("200px");
                lblPlato.getStyle().set("font-weight", "500");

                HorizontalLayout barraColor = new HorizontalLayout();
                barraColor.setHeight("24px");
                barraColor.setWidth(porcentajeAncho + "%");
                barraColor.getStyle().set("background-color", "var(--lumo-primary-color-50pct)")
                        .set("border-radius", "var(--lumo-border-radius-s)")
                        .set("padding-left", "10px")
                        .set("align-items", "center");

                Span lblCant = new Span(cantidad + " uds.");
                lblCant.getStyle().set("font-size", "var(--lumo-font-size-s)")
                        .set("color", "var(--lumo-primary-text-color)")
                        .set("font-weight", "bold");

                barraColor.add(lblCant);
                filaBarra.add(lblPlato, barraColor);
                panelGrafico.add(filaBarra);
            }
        }
    }

    private double valueOrZero(Double amount) {
        return amount != null ? amount : 0.00;
    }

    private VerticalLayout crearTarjetaKpi(String titulo, String valor, com.vaadin.flow.component.icon.Icon icono, String colorTexto) {
        VerticalLayout tarjeta = new VerticalLayout();
        tarjeta.setWidth("33%");
        tarjeta.addClassNames(LumoUtility.BorderRadius.MEDIUM, LumoUtility.BoxShadow.SMALL);
        tarjeta.getStyle().set("background-color", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("padding", "20px");

        HorizontalLayout cabecera = new HorizontalLayout(icono, new Span(titulo));
        cabecera.setAlignItems(Alignment.CENTER);
        cabecera.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        Span txtValor = new Span(valor);
        txtValor.getStyle().set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "bold")
                .set("color", colorTexto);

        tarjeta.add(cabecera, txtValor);
        return tarjeta;
    }
}