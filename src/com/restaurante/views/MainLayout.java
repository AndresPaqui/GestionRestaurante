package com.restaurante.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

public class MainLayout extends AppLayout {
    public MainLayout() {
        H1 title = new H1("Gestión de Restaurante");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "var(--lumo-space-m)");

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Punto de Venta", VistaPuntoVenta.class));
        nav.addItem(new SideNavItem("Inventario", VistaInventario.class));
        nav.addItem(new SideNavItem("Recetas", VistaRecetas.class));
        nav.addItem(new SideNavItem("Reportes", VistaReportes.class));

        addToNavbar(new DrawerToggle(), title);
        addToDrawer(new Scroller(nav));
    }
}