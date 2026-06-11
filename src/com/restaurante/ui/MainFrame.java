package com.restaurante.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame {
    private JPanel MainFrame;
    private JButton PuntoVentaButton;
    private JButton InventarioButton;
    private JButton RecetasButton;
    private JButton ReportesButton;
    private JButton SalirButton;
    private JPanel cardPanel;
    private JPanel menuPanel;
    private JPanel VistaPuntoVenta;
    private JPanel VistaInventario;
    private JPanel VistaRecetas;
    private JPanel VistaReportes;

    public MainFrame() {




        //Logica para el boton de punto de venta (cardLyout)
        PuntoVentaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtenemos el Layout del panel contenedor y hacerte un "cast" cambio de tipo a cardLayout
                CardLayout cl = (CardLayout) (cardPanel.getLayout());
                // Llamamos al metodo show para mostar, pasando el contenedor y el cardNanem
                cl.show(cardPanel, "punto_venta");

            }
        });

        //Logica para el boton de inventario (cardLyout)
        InventarioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtenemos el Layout del panel contenedor y hacerte un "cast" cambio de tipo a cardLayout
                CardLayout cl = (CardLayout) (cardPanel.getLayout());
                // Llamamos al metodo show para mostar, pasando el contenedor y el cardNanem
                cl.show(cardPanel, "inventario");

            }
        });

        //Logica para el boton de recetas(cardLyout)
        RecetasButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtenemos el Layout del panel contenedor y hacerte un "cast" cambio de tipo a cardLayout
                CardLayout cl = (CardLayout) (cardPanel.getLayout());
                // Llamamos al metodo show para mostar, pasando el contenedor y el cardNanem
                cl.show(cardPanel, "recetas");

            }
        });

        //Logica para el boton de reportes (cardLyout)
        ReportesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtenemos el Layout del panel contenedor y hacerte un "cast" cambio de tipo a cardLayout
                CardLayout cl = (CardLayout) (cardPanel.getLayout());
                // Llamamos al metodo show para mostar, pasando el contenedor y el cardNanem
                cl.show(cardPanel, "reportes");

            }
        });
    }

    private void createUIComponents() {
        //Instanciamos el panel independiente para punto venta
        PuntoVentaForm puntoVentaForm = new PuntoVentaForm();
        // Le asignamos dicho panel a la variable que espera el mainForm
        VistaPuntoVenta = puntoVentaForm.getPuntoVentaPanel();

        //Repetimos para las otras 3 ventanas

        //Instanciamos el panel de inventario
        InventarioForm inventarioForm = new InventarioForm();
        // Le asignamos su componente dentro del mainFrom
        VistaInventario = inventarioForm.getInventarioPanel();

        //Instanciamos el panel de recetas
        RecetasFrom recetasFrom = new RecetasFrom();
        // Le asignamos su componente dentro del mainFrom
        VistaRecetas = recetasFrom.getRecetasPanel();

        // Instanciamos el panel de reportes
        ReporteForm reporteForm = new ReporteForm();
        // Le asignamos su componente dentro del mainForm
        VistaReportes = reporteForm.getReportePanel();
    }
}
