package com.restaurante.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "inventario", layout = MainLayout.class)
public class VistaInventario extends VerticalLayout {
    public VistaInventario() {
        add(new H2("Gestión de Inventario"));
    }
}