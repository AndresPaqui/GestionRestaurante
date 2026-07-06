package com.restaurante.views;

import com.restaurante.logic.VentasService;
import com.restaurante.model.ItemVenta;
import com.restaurante.model.Venta;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "reportes", layout = MainLayout.class)
public class VistaReportes extends VerticalLayout {

    private final VentasService ventasService = new VentasService();

    public VistaReportes() {
        setSizeFull();
        setSpacing(true);
        setPadding(true);

        H2 tituloPagina = new H2("Cuadro de Mando Financiero (Dashboard)");
        add(tituloPagina);

        // =========================================================================
        // FILA SUPERIOR: Tarjetas de Rendimiento Económico (KPIs)
        // =========================================================================
        Double ingresosTotalesRaw = ventasService.calcularTotalVentasFacturadas();
        Double costosMateriaPrimaRaw = ventasService.calcularCostoTotalInvertido();

        double ingresosTotales = valueOrZero(ingresosTotalesRaw);
        double costosMateriaPrima = valueOrZero(costosMateriaPrimaRaw);
        double utilidadNeta = ingresosTotales - costosMateriaPrima;

        HorizontalLayout filaKpis = new HorizontalLayout();
        filaKpis.setWidthFull();
        filaKpis.setSpacing(true);

        // Tarjeta 1: Ingresos
        VerticalLayout cardIngresos = crearTarjetaKpi("Ingresos Totales (Con IVA)",
                String.format("$%.2f", ingresosTotales), VaadinIcon.MONEY.create(), "var(--lumo-success-text-color)");

        // Tarjeta 2: Costos
        VerticalLayout cardCostos = crearTarjetaKpi("Costo de Producción",
                String.format("$%.2f", costosMateriaPrima), VaadinIcon.CART.create(), "var(--lumo-error-text-color)");

        // Tarjeta 3: Utilidad Real con lógica visual de signo
        String colorGanancia = utilidadNeta < 0
                ? "var(--lumo-error-text-color)"
                : utilidadNeta > 0
                    ? "var(--lumo-success-text-color)"
                    : "var(--lumo-secondary-text-color)";

        VerticalLayout cardNeto = crearTarjetaKpi("Ganancia Neta Real",
                String.format("$%.2f", utilidadNeta), VaadinIcon.TRENDING_UP.create(), colorGanancia);

        filaKpis.add(cardIngresos, cardCostos, cardNeto);
        add(filaKpis);

        // =========================================================================
        // SECCIÓN INFERIOR: Top de Platos Más Vendidos (Simulación de Gráfico Analítico)
        // =========================================================================
        VerticalLayout panelGrafico = new VerticalLayout();
        panelGrafico.setWidthFull();
        panelGrafico.getStyle().set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "25px")
                .set("margin-top", "15px");

        H3 tituloGrafico = new H3("Ranking de Platos más Solicitados");
        panelGrafico.add(tituloGrafico);

        // Agrupamos y sumamos las cantidades vendidas por plato desde el historial real
        Map<String, Integer> rankingPlatos = new HashMap<>();
        List<Venta> historial = ventasService.obtenerHistorialVentas();
        for (Venta v : historial) {
            List<ItemVenta> detalles = ventasService.obtenerDetallesPorVenta(v.getIdVenta());
            for (ItemVenta item : detalles) {
                String nombrePlato = item.getPlato().getNombre();
                rankingPlatos.put(nombrePlato, rankingPlatos.getOrDefault(nombrePlato, 0) + item.getCantidad());
            }
        }

        if (rankingPlatos.isEmpty()) {
            panelGrafico.add(new Span("No hay suficientes transacciones comerciales para procesar estadísticas."));
        } else {
            // Buscamos cuál es el plato con más ventas para sacar el proporcional del 100% de la barra
            int maxVentas = rankingPlatos.values().stream().mapToInt(Integer::intValue).max().orElse(1);

            for (Map.Entry<String, Integer> entry : rankingPlatos.entrySet()) {
                String plato = entry.getKey();
                int cantidad = entry.getValue();

                // Calculamos el porcentaje elástico de la barra (Regla de tres)
                int porcentajeAncho = (cantidad * 100) / maxVentas;

                HorizontalLayout filaBarra = new HorizontalLayout();
                filaBarra.setWidthFull();
                filaBarra.setAlignItems(Alignment.CENTER);

                Span lblPlato = new Span(plato);
                lblPlato.setWidth("200px"); // Columna fija de texto para que las barras queden alineadas
                lblPlato.getStyle().set("font-weight", "500");

                // La barra gráfica pintada dinámicamente con estilos CSS nativos
                HorizontalLayout barraColor = new HorizontalLayout();
                barraColor.setHeight("24px");
                barraColor.setWidth(porcentajeAncho + "%"); // Crece según la cantidad vendida
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

        add(panelGrafico);
    }

    private double valueOrZero(Double amount) {
        return amount != null ? amount : 0.00;
    }

    /**
     * Módulo helper para construir tarjetas métricas elegantes (Estilo Dashboard)
     */
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