# 🗄️ Módulo de Persistencia y Datos 
**Responsable:** Joel Tapia (Gestión de Datos)

Tiempo  estimado (7-10 dias) **Desarrollo a la par de UI**

Entregables:
- Clases, Conexion.java, InsumoDAO.java, PlatoDAO, RecetaDAO, VentasDAO.

Este paquete es el corazón de la persistencia del sistema. Su objetivo es asegurar que la información del restaurante no se pierda al cerrar la aplicación.

## 🛠️ Configuración del Entorno (JDBC)
Para que las pruebas de lógica funcionen, debes tener vinculado el Driver de SQLite:
1. El archivo `.jar` se encuentra en la carpeta `/lib` del proyecto.
2. En tu IDE, ve a **Project Structure > Modules > Dependencies**.
3. Haz clic en **+**, selecciona el archivo JAR de la carpeta `/lib` y elige la categoría **Classes**.
4. Haz clic en **Apply** y **OK**.

## 🏗️ Estructura de la Base de Datos
Debes crear las tablas siguiendo este esquema exacto para mantener la coherencia con los modelos base:

### 1. Tabla: `insumos`
| Columna | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | INTEGER (PK) | Auto-incremental. |
| `nombre` | TEXT | Nombre del insumo (ej. Carne). |
| `categoria` | TEXT | Categoría (Carnes, Lácteos, etc.). |
| `stock_actual` | REAL | Cantidad en inventario. |
| `stock_minimo` | REAL | Límite para alertas de reabastecimiento. |
| `costo_unitario`| REAL | Precio por unidad. |
| `unidad` | TEXT | kg, lt, unidad. |

### 2. Tabla: `platos`
| Columna | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | INTEGER (PK) | Identificador único del plato. |
| `nombre` | TEXT | Nombre del plato (ej. Hornado). |
| `precio_venta` | REAL | Precio al público. |

### 3. Tabla: `recetas` (Tabla Intermedia)
| Columna | Tipo | Descripción |
| :--- | :--- | :--- |
| `id_plato` | INTEGER (FK) | Referencia a `platos(id)`. |
| `id_insumo` | INTEGER (FK) | Referencia a `insumos(id)`. |
| `cantidad_necesaria` | REAL | Cantidad por cada ingrediente. |

## 🚀 Clases DAO a implementar
Debes crear una clase para cada entidad para que el equipo pueda solicitar datos:
- **`Conexion.java`**: Gestión de la conexión a SQLite.
- **`InsumoDAO.java`**: Gestión de stock y materia prima.
- **`PlatoDAO.java`**: Gestión de los nombres y precios del menú.
- **`RecetaDAO.java`**: Consultar qué ingredientes lleva un plato específico (Vital para Helen).
- **`VentaDAO.java`**: Registro histórico de ventas realizadas.

## 🧠 ¿Por qué lo hacemos así?
1. **Persistencia Real:** A diferencia de los `ArrayList` que hemos usado, SQLite guarda los datos en el disco duro. Si hay un apagón, los datos están seguros.
2. **Desacoplamiento:** Al usar DAOs, si mañana decidimos cambiar SQLite por MySQL, solo tendremos que tocar este paquete. El resto del equipo (UI y Lógica) no notará el cambio.
3. **Escalabilidad:** Separar las recetas de los platos permite que un solo plato tenga decenas de ingredientes sin complicar el código de la interfaz.

## ⚠️ Reglas de Oro
- **Seguridad:** Usa siempre `PreparedStatement` para evitar inyecciones de código.
- **Eficiencia:** No leas toda la base de datos cada vez; crea métodos de consulta específicos.

---
*Nota para el equipo: Cualquier cambio en los nombres de las columnas debe ser notificado a (Andrés Paqui) para actualizar el UML.*