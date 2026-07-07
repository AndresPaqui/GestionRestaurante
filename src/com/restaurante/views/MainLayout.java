package com.restaurante.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.dependency.StyleSheet;

@StyleSheet("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap")
public class MainLayout extends AppLayout {

    public MainLayout() {
        H1 title = new H1("Gestión de Restaurante");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "var(--lumo-space-m)");

        // --- SOLUCIÓN: Usamos addAttachListener para garantizar que la UI ya exista en el cliente ---
        addAttachListener(event -> {
            // Aplicamos las variables directamente sobre el elemento raíz del documento
            com.vaadin.flow.component.UI.getCurrent().getElement().getStyle()
                    .set("--lumo-primary-color", "#E65100")
                    .set("--lumo-primary-color-10pct", "rgba(230, 81, 0, 0.08)")
                    .set("--lumo-primary-color-50pct", "rgba(230, 81, 0, 0.4)")
                    .set("--lumo-primary-text-color", "#E65100")
                    .set("--lumo-primary-contrast-color", "#FFFFFF")
                    .set("--lumo-background-color", "#F9F9FA");

            // Inyectamos de forma segura el CSS para las tarjetas grandes y cuadradas
            com.vaadin.flow.component.UI.getCurrent().getPage().executeJs(
                    "const styleId = 'main-layout-sidebar-style';" +
                            "if (!document.getElementById(styleId)) {" +
                            "  const style = document.createElement('style');" +
                            "  style.id = styleId;" +
                            "  style.textContent = `" +
                            "    vaadin-app-layout::part(drawer) { height: 100vh; background-color: #F5F5F7; border-right: 1px solid var(--lumo-contrast-10pct); }" +
                            "    .pos-side-nav {" +
                            "      display: flex;" +
                            "      flex-direction: column;" +
                            "      gap: var(--lumo-space-s);" +
                            "      padding: var(--lumo-space-s);" +
                            "      min-height: 100%;" +
                            "      box-sizing: border-box;" +
                            "    }" +
                            "    .pos-side-nav vaadin-side-nav-item::part(link) {" +
                            "      min-height: 8.5rem;" +
                            "      padding: var(--lumo-space-xl) var(--lumo-space-l);" +
                            "      border-radius: 12px;" +
                            "      border: 1px solid var(--lumo-contrast-10pct);" +
                            "      background: #FFFFFF;" +
                            "      box-shadow: 0 2px 6px rgba(0,0,0,0.03);" +
                            "      transition: background-color 120ms ease, border-color 120ms ease, box-shadow 120ms ease;" +
                            "    }" +
                            "    .pos-side-nav vaadin-side-nav-item::part(content) {" +
                            "      display: flex;" +
                            "      flex-direction: column;" +
                            "      align-items: center;" +
                            "      justify-content: center;" +
                            "      text-align: center;" +
                            "      gap: var(--lumo-space-xs);" +
                            "      width: 100%;" +
                            "    }" +
                            "    .pos-side-nav vaadin-side-nav-item[current] {" +
                            "      background: transparent !important;" +                         // SOLUCIÓN: Elimina las franjas claras de los extremos
                            "    }" +
                            "    .pos-side-nav vaadin-side-nav-item[current]::part(link) {" +
                            "      font-weight: var(--lumo-font-weight-semibold);" +
                            "      background-color: var(--lumo-primary-color-10pct) !important;" + // Fondo uniforme limpio
                            "      background-image: none !important;" +
                            "      border-color: var(--lumo-primary-color);" +
                            "      box-shadow: 0 2px 8px rgba(230, 81, 0, 0.1);" +
                            "    }" +
                            "    .pos-side-nav vaadin-side-nav-item:hover::part(link) {" +
                            "      background: var(--lumo-contrast-5pct);" +
                            "    }" +
                            "    @media (max-height: 750px), (max-width: 900px) {" +
                            "      .pos-side-nav vaadin-side-nav-item::part(link) {" +
                            "        min-height: 6.5rem;" +
                            "        padding: var(--lumo-space-l) var(--lumo-space-m);" +
                            "      }" +
                            "    }" +
                            "  `;" +
                            "  document.head.appendChild(style);" +
                            "}"
            );
        });

        // Configuración estricta del menú
        SideNav nav = new SideNav();
        nav.addClassName("pos-side-nav");
        nav.addItem(createNavButton("Punto de Venta", VistaPuntoVenta.class, VaadinIcon.CASH));
        nav.addItem(createNavButton("Inventario", VistaInventario.class, VaadinIcon.PACKAGE));
        nav.addItem(createNavButton("Recetas", VistaRecetas.class, VaadinIcon.CLIPBOARD_TEXT));
        nav.addItem(createNavButton("Reportes", VistaReportes.class, VaadinIcon.BAR_CHART));

        Scroller drawerScroller = new Scroller(nav);
        drawerScroller.setSizeFull();
        drawerScroller.getStyle()
                .set("height", "100vh")
                .set("overflow", "auto");

        addToNavbar(new DrawerToggle(), title);
        addToDrawer(drawerScroller);
    }

    private SideNavItem createNavButton(String label, Class<? extends com.vaadin.flow.component.Component> navigationTarget,
                                        VaadinIcon iconType) {
        Icon icon = iconType.create();
        icon.setSize("30px");

        SideNavItem item = new SideNavItem(label, navigationTarget, icon);
        item.addClassNames(LumoUtility.Padding.Vertical.LARGE, LumoUtility.Padding.Horizontal.LARGE);
        return item;
    }
}