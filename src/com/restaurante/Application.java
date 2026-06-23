package com.restaurante;

import com.restaurante.data.DataInjector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
// Al no poner un 'value', Vaadin inicializa automáticamente el hermoso tema "Lumo" por defecto
@Theme
public class Application implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}