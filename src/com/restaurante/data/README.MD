# 🗄️ Módulo de Persistencia y Datos
**Responsable:** Joel Tapia (Gestión de Datos)

Este paquete es el corazón de la persistencia del sistema. Su objetivo es asegurar que la información del restaurante no se pierda al cerrar la aplicación.

## 🛠️ Configuración de JDBC
Para que Java reconozca la base de datos, es obligatorio vincular el Driver:
1. El archivo `.jar` ya está en la carpeta `/lib` del repositorio.
2. En tu IDE, ve a **Project Structure > Modules > Dependencies**.
3. Añade el archivo desde `/lib` y selecciona la categoría **Classes**.

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

## 🚀 Implementación del Patrón DAO
Cada entidad debe tener su propia clase de acceso para que Helen (Lógica) y José (UI) puedan solicitar datos:

- **`Conexion.java`**: Gestiona la apertura y cierre del archivo `.db`.
- **`InsumoDAO.java`**: Métodos `insertar()`, `listar()`, `actualizarStock()` y `eliminar()`.
- **`PlatoDAO.java`**: Métodos para gestionar platos y sus recetas vinculadas.

## 🧠 ¿Por qué lo hacemos así?
1. **Persistencia Real:** A diferencia de los `ArrayList` que hemos usado, SQLite guarda los datos en el disco duro. Si hay un apagón, los datos están seguros.
2. **Desacoplamiento:** Al usar DAOs, si mañana decidimos cambiar SQLite por MySQL, solo tendremos que tocar este paquete. El resto del equipo (UI y Lógica) no notará el cambio.
3. **Escalabilidad:** Separar las recetas de los platos permite que un solo plato tenga decenas de ingredientes sin complicar el código de la interfaz.

---
*Nota para el equipo: Cualquier cambio en los nombres de las columnas debe ser notificado a (Andrés Paqui) para actualizar el UML.*