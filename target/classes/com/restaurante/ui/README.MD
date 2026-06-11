# 🎨 Módulo de Interfaz de Usuario (Frontend)
**Responsable:** José Méndez

Tiempo  estimado (7-12 dias) **Desarrollo a la par de data**

Entregables:
- MainFrame con su menú lateral, instalar FlatLaf y diseñar los paneles vacíos (Inventario, Ventas, etc.).

Este paquete es la cara visible del sistema. Tu objetivo es transformar los datos técnicos en una herramienta fácil de usar para los empleados del restaurante.

## 🛠️ Herramientas de Diseño

### 1. Swing UI Designer (IntelliJ)
Dado que usas IntelliJ IDEA, este plugin será tu mesa de trabajo.
- **¿Cómo se usa?**: Al crear una nueva clase, elige **"Swing UI Designer > GUI Form"**. Esto creará un archivo `.java` y un archivo `.form`.
- **Ventaja**: Puedes arrastrar botones, tablas y campos de texto. El código se genera automáticamente en segundo plano.

### 2. Modernización con FlatLaf (¡Adiós a los 90!)
Para que el proyecto se vea moderno y profesional (estilo modo oscuro), usaremos la librería **FlatLaf**.
- **Instalación**:
    1. El archivo `flatlaf.jar` ya está en la carpeta `/lib`. Asegúrate de vincularlo a tu proyecto (Project Structure > Modules > Dependencies).
    2. En la clase principal de la aplicación (donde esté el método `main`), añade estas líneas al principio:
       ```java
       try {
           com.formdev.flatlaf.FlatDarkLaf.setup();
       } catch( Exception ex ) {
           System.err.println( "Error al iniciar FlatLaf" );
       }
       ```
- **Resultado**: Tus botones pasarán de ser grises y cuadrados a ser planos y elegantes con solo esa línea de código.

## 🏗️ Estructura Base Obligatoria (Guía del Líder)
Para mantener la coherencia con el resto del equipo, la interfaz debe seguir esta arquitectura de **Ventana Única**:

### A. El Marco Principal (`MainFrame`)
No abras ventanas nuevas para cada cosa. Diseña un marco principal dividido en dos secciones:
1. **Menú Lateral (Navegación):** Debe contener 4 botones fijos:
    - **Inventario:** Gestión de insumos (tabla, botones agregar/editar).
    - **Platos/Recetas:** Creación de menú y costos.
    - **Ventas (POS):** Pantalla para procesar pedidos.
    - **Reportes:** Dashboard con estadísticas de ganancias.
2. **Panel Central (Contenido):** Usa un `CardLayout`. Este espacio cambiará su contenido según el botón que el usuario presione en el menú lateral.

## 🔗 Conexión con el "Cerebro" (Helen)
**Regla de Oro:** La UI no hace cálculos ni habla con la base de datos.
- **Tu flujo**: Cuando un usuario haga clic en "Guardar Insumo", tú capturas los datos de los campos de texto y se los pasas a **Helen (Logic)**.
- **Ejemplo**: `InventarioService.registrar(nuevoInsumo);`.
- Ella te devolverá una respuesta. Si algo está mal, muestra un aviso al usuario (ej. `JOptionPane.showMessageDialog`).

## 🎨 Consejos de Diseño
- **Iconos**: Usa iconos simples (puedes buscar librerías como FlatLaf Extras).
- **Tablas**: Usa `JTable` para mostrar el inventario. Asegúrate de que las filas tengan suficiente espacio (padding).
- **Colores**: Mantente en la paleta de oscuros (gris oscuro, negro) con acentos en verde lima o azul para resaltar botones importantes.