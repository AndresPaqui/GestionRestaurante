package com.restaurante;

import com.restaurante.ui.MainFrame;
import com.restaurante.ui.MainframeGUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        try {
            com.formdev.flatlaf.FlatDarkLaf.setup();
        } catch( Exception ex ) {
            System.err.println( "Error al iniciar FlatLaf" );
        }


        SwingUtilities.invokeLater(() -> {
            new MainframeGUI();
        });
    }
}