package com.restaurante.model;

public class ItemVenta {
    //Representa una venta.

    private Plato plato;
    private int cantidad;
    private double subtotal; // cantidad * palto.getPrecioVenta()

    public ItemVenta(Plato plato, int cantidad, double subtotal) {
        this.plato = plato;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    public Plato getPlato() {
        return plato;
    }

    public void setPlato(Plato plato) {
        this.plato = plato;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
