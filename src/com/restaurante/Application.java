package com.restaurante;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Theme(themeClass = Lumo.class) // <-- En Vaadin 24, esto carga el Modo Claro automáticamente
public class Application implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}