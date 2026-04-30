package com.restaurante.model;

public class Insumo {
    //"Clase POJO que representa la materia prima. Contiene solo datos: nombre, stock, costo unitario y unidad de medida."
    //Define los atributos básicos de la materia prima (ID, nombre, stock actual, stock mínimo, costo).
    //Caracteristicas de la clase Insumo: Atributos, Constructor, Métodos getter/setter, debe cumplir con ecapsulamiento.

    private int id; //Identificador unico del insumo
    private String nombre; //Nombre del insumo
    private String categoria; //Carne, Lacteos,etc
    private double stockActual;
    private double stockMinimo;
    private double costoUnitario;
    private String unidadMedida; //kg,lt,unidad


    public Insumo(int id, String nombre, String categoria, double stockActual, double stockMinimo, double costoUnitario, String unidadMedida) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.costoUnitario = costoUnitario;
        this.unidadMedida = unidadMedida;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getStockActual() {
        return stockActual;
    }

    public void setStockActual(double stockActual) {
        this.stockActual = stockActual;
    }

    public double getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(double stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
}
