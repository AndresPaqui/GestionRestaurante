package com.restaurante.model;

import java.util.Map;

public class Plato {
    //"Representa un ítem del menú. Contiene el nombre, el PVP definido y una lista de objetos Insumo con sus cantidades (Receta)."
    //Representa un ítem del menú. Contiene su nombre, PVP y una List<Insumo> (la receta).
    //Caracteristicas de la clase Plato: Atributos, Constructor, Métodos getter/setter, debe cumplir con ecapsulamiento.

    private int idPlato; //Identificador unico
    private String nombre;
    private double precioVenta; //PVP
    private double costoProduccion; //Calculado en logic
    private Map<Insumo, Double> ingredientes;

    public Plato(int idPlato, String nombre, double precioVenta, double costoProduccion, Map<Insumo, Double> ingredientes) {
        this.idPlato = idPlato;
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.costoProduccion = costoProduccion;
        this.ingredientes = ingredientes;
    }

    public int getId() {
        return idPlato;
    }

    public void setId(int id) {
        this.idPlato = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public double getCostoProduccion() {
        return costoProduccion;
    }

    public void setCostoProduccion(double costoProduccion) {
        this.costoProduccion = costoProduccion;
    }

    public Map<Insumo, Double> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(Map<Insumo, Double> ingredientes) {
        this.ingredientes = ingredientes;
    }
}
