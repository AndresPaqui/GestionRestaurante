package com.restaurante.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {
    public MainLayout() {
        H1 title = new H1("Gestión de Restaurante");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "var(--lumo-space-m)");

        getUI().ifPresent(ui -> {
            ui.getElement().getThemeList().add(Lumo.DARK);
            ui.getElement().getStyle()
                    .set("--lumo-primary-color", "#CCFF00")
                    .set("--lumo-primary-color-10pct", "rgba(204, 255, 0, 0.1)");

            ui.getPage().executeJs(
                    "const styleId = 'main-layout-sidebar-style';" +
                            "if (!document.getElementById(styleId)) {" +
                            "  const style = document.createElement('style');" +
                            "  style.id = styleId;" +
                            "  style.textContent = `" +
                            "    vaadin-app-layout::part(drawer) { height: 100vh; }" +
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
                            "      border-radius: var(--lumo-border-radius-l);" +
                            "      border: 1px solid var(--lumo-contrast-20pct);" +
                            "      background: var(--lumo-base-color);" +
                            "      transition: background-color 120ms ease, border-color 120ms ease;" +
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
                            "    .pos-side-nav vaadin-side-nav-item[current]::part(link) {" +
                            "      font-weight: var(--lumo-font-weight-semibold);" +
                            "      background: var(--lumo-primary-color-10pct);" +
                            "      border-color: var(--lumo-primary-color-50pct);" +
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
                            "}");
        });

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