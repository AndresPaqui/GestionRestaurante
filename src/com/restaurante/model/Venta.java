package com.restaurante.model;

import java.time.LocalDateTime;
import java.util.List;

public class Venta {
    //Objeto que agrupa los platos vendidos, la fecha, el total y el metodo de pago. (Fundamental para el RF07).

    private int idVenta;
    private LocalDateTime fecha;
    private List<ItemVenta> detalles;
    private double totalVenta;
    private String metodoPago;
    private String clienteNombre;
    private String clienteCedula;

    public Venta(int idVenta, LocalDateTime fecha, List<ItemVenta> detalles, double totalVenta, String metodoPago, String clienteNombre, String clienteCedula) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.detalles = detalles;
        this.totalVenta = totalVenta;
        this.metodoPago = metodoPago;
        this.clienteNombre = clienteNombre;
        this.clienteCedula = clienteCedula;
    }

    public Venta(int idVenta, LocalDateTime fecha, List<ItemVenta> detalles, double totalVenta, String metodoPago) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.detalles = detalles;
        this.totalVenta = totalVenta;
        this.metodoPago = metodoPago;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public List<ItemVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<ItemVenta> detalles) {
        this.detalles = detalles;
    }

    public double getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(double totalVenta) {
        this.totalVenta = totalVenta;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteCedula() {
        return clienteCedula;
    }

    public void setClienteCedula(String clienteCedula) {
        this.clienteCedula = clienteCedula;
    }


}
