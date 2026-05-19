package com.restaurante.ui;

import javax.swing.*;

public class MainFrame extends JFrame {
    //El JFrame principal que contiene la barra lateral y hace el "swapping" (cambio) de paneles.

public MainFrame(){
    try {
        com.formdev.flatlaf.FlatDarkLaf.setup();
    } catch( Exception ex ) {
        System.err.println( "Error al iniciar FlatLaf" );
    }
}

}
