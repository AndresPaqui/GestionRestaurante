# 🍽️ Sistema de Gestión Gastronómica Integral

Este proyecto nace como una solución tecnológica para la digitalización de emprendimientos gastronómicos en Ecuador. El sistema automatiza el control de inventarios y lo vincula directamente con las ventas diarias para evitar pérdidas por desabastecimiento o productos caducados.

## 🎯 Objetivos del Proyecto
* **Automatización de Inventario**: Seguimiento de materia prima por unidad de medida y costo unitario.
* **Ingeniería de Menú**: Creación de recetas vinculadas a insumos con cálculo automático de costos de producción y sugerencia de PVP.
* **Inteligencia de Negocio**: Dashboard para visualizar la rentabilidad por plato y márgenes de ganancia en tiempo real.
* **Control Crítico**: Sistema de alertas visuales cuando el stock alcanza niveles mínimos de seguridad.

## 👥 Equipo y Roles
El desarrollo se gestiona bajo una estructura de roles definida para garantizar la calidad y el cumplimiento de los requerimientos:

* **Andrés Paqui (Líder de Proyecto & Arquitecto)**: Responsable de la coordinación general y planificación. Definición de la arquitectura base del sistema mediante la creación de los Modelos de Datos iniciales y el diseño del Diagrama de Clases UML para asegurar la coherencia técnica del proyecto.
* **Joel Tapia (Gestión de Datos)**: Responsable del paquete data. Encargado del diseño y gestión de la base de datos SQLite, asegurando la persistencia de la información y la implementación del patrón DAO.
* **José Méndez (Interfaz de Usuario)**: Responsable del paquete ui. Encargado del diseño visual y la experiencia de usuario utilizando Java Swing y el plugin WindowBuilder, integrando FlatLaf para una estética moderna.
* **Helen Benalcazar (Lógica de Negocio & QA)**: Responsable del paquete logic. Encargada de implementar las reglas de negocio (cálculos, validaciones de stock) y realizar las pruebas funcionales para garantizar la calidad del software.

## 🛠️ Tecnologías y Metodología
* **Lenguaje**: Java (Programación Orientada a Objetos).
* **Interfaz**: Java Swing con navegación dinámica por paneles.
* **Gestión**: Control de versiones con Git/GitHub y flujo de trabajo colaborativo.
* **Diseño**: Modelado mediante Diagramas de Clases UML y Entidad-Relación.

---
*Proyecto académico desarrollado para la asignatura de Programación II - Ingeniería de Software (UDLA)*
