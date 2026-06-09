package com.restaurante.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainframeGUI extends JFrame {
    private JButton inventarioButton;
    private JButton platosButton;
    private JButton ventasPOSButton;
    private JButton reportesButton;
    private JPanel Layout;

    public MainframeGUI() {

        setTitle("Sistema de Restaurante");

        setContentPane(Layout);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }

    public JPanel getPanelPrincipal() {
        return Layout;
    }
}


