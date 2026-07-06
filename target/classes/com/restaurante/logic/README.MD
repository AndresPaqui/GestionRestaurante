# 🧠 Módulo de Lógica de Negocio (Backend)
**Responsable:** Helen Benalcazar (Lógica y QA)

Tiempo  estimado (12 - 20 dias) **Desarrollo luego de data y ui**

Entregables:
- Implementar los Services (Lógica). Tomar los datos que José captura en la UI y pasarlos a los DAOs de Joel.

Este paquete actúa como el "cerebro" del sistema. Es el mediador entre la Interfaz de Usuario (José) y la Persistencia de Datos (Joel). Ningún dato llega a la base de datos sin antes ser procesado y validado aquí.

## 🛠️ Configuración del Entorno (JDBC)
Para que las pruebas de lógica funcionen, debes tener vinculado el Driver de SQLite:
1. El archivo `.jar` se encuentra en la carpeta `/lib` del proyecto.
2. En tu IDE, ve a **Project Structure > Modules > Dependencies**.
3. Haz clic en **+**, selecciona el archivo JAR de la carpeta `/lib` y elige la categoría **Classes**.
4. Haz clic en **Apply** y **OK**.

## 🏗️ Estructura de Servicios
El paquete se divide en cuatro clases principales, cada una con una responsabilidad específica:

### 1. `InventarioService`
Gestiona la validación y el estado de la materia prima.
- **Validación:** Asegurar que no se registren insumos con valores negativos o nombres vacíos.
- **Alertas (RF02):** Método para identificar insumos cuyo `stock_actual` sea menor o igual al `stock_minimo`.

### 2. `RecetaService`
Gestiona la composición técnica de los platos.
- **Costo de Producción (RF03):** Calcular el costo total sumando `(cantidad * costo_unitario)` de todos los ingredientes de un plato.
- **Integridad:** Verificar que existan todos los insumos necesarios antes de permitir la creación de una receta.

### 3. `VentaService`
Motor de transacciones y Punto de Venta (POS).
- **Explosión de Inventario (RF06):** Al procesar una venta, buscar la receta y solicitar al paquete `data` la reducción proporcional del stock de cada ingrediente.
- **Disponibilidad:** Impedir ventas si el stock de algún ingrediente es insuficiente.

### 4. `ReporteService`
Transformación de datos en información estratégica.
- **Rentabilidad (RF11):** Calcular la ganancia neta restando el costo de producción al precio de venta.
- **Estadísticas:** Generar resúmenes de platos más vendidos y gastos por insumo.

## 📝 Estándares de Implementación
- **Cero Swing:** No debe haber ni una línea de código de interfaz aquí.
- **Excepciones:** Usa `try-catch` para manejar errores lógicos (ej. "Stock insuficiente") y devolver mensajes claros a la UI.
- **Relación con Data:** Todos los servicios deben usar los objetos DAO creados por Joel para obtener o guardar información.