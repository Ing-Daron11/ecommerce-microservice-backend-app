# Product Service

Microservicio responsable de gestionar productos y categorías en el catálogo de e-commerce.

## Configuración

- **Puerto**: 8500
- **Context-path**: `/product-service`
- **Base de datos**: H2 (en memoria para desarrollo)
- **Migración**: Flyway (V1-V6)

## Componentes Principales

### Domain Models

#### Product
- **Atributos clave**:
  - `productId`: Identificador único (generado automáticamente)
  - `productTitle`: Nombre del producto
  - `imageUrl`: URL de imagen del producto
  - `sku`: Stock Keeping Unit (único)
  - `priceUnit`: Precio unitario (decimal)
  - `quantity`: Cantidad disponible en inventario
  - `category`: Referencia ManyToOne a Category (FetchType.EAGER)

- **Relaciones**:
  - ManyToOne con Category (relación inversa: Category.products)
  - Category es eagerly loaded para disponibilidad inmediata

- **Validaciones**:
  - SKU es único en la BD
  - ProductId es generado automáticamente

#### Category
- **Atributos clave**:
  - `categoryId`: Identificador único
  - `categoryTitle`: Nombre de la categoría
  - `imageUrl`: URL de imagen de la categoría
  - `parentCategory`: Referencia a categoría padre (relación jerárquica)
  - `subCategories`: Colección de subcategorías (OneToMany, LAZY)
  - `products`: Colección de productos (OneToMany, LAZY, CASCADE)

- **Relaciones**:
  - ManyToOne con Category (auto-referencia para jerarquía)
  - OneToMany con Category (subcategorías, LAZY loading)
  - OneToMany con Product (productos en categoría, LAZY loading, CASCADE)

- **Estructura jerárquica**:
  - Soporta categorías padre-hijo (subcategorías)
  - Cada categoría puede contener múltiples productos
  - Eliminación de categoría elimina sus subcategorías y productos (CASCADE)

### REST Endpoints

#### ProductResource (`/api/products`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/products` | Obtiene todos los productos |
| GET | `/api/products/{productId}` | Obtiene producto por ID |
| POST | `/api/products` | Crea nuevo producto |
| PUT | `/api/products` | Actualiza producto (full update) |
| PUT | `/api/products/{productId}` | Actualiza producto por ID |
| DELETE | `/api/products/{productId}` | Elimina producto |

#### CategoryResource (`/api/categories`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/categories` | Obtiene todas las categorías |
| GET | `/api/categories/{categoryId}` | Obtiene categoría por ID |
| POST | `/api/categories` | Crea nueva categoría |
| PUT | `/api/categories` | Actualiza categoría (full update) |
| PUT | `/api/categories/{categoryId}` | Actualiza categoría por ID |
| DELETE | `/api/categories/{categoryId}` | Elimina categoría |

**Validaciones de entrada**:
- `productId` / `categoryId`: No pueden estar en blanco (NotBlank)
- `productDto` / `categoryDto`: No pueden ser null (NotNull)

### Services

#### ProductServiceImpl
Implementa lógica de negocios para productos:

- **findAll()**:
  - Consulta todos los productos desde la BD
  - Mapea a ProductDto con información de categoría
  - Retorna lista inmutable sin duplicados

- **findById(Integer productId)**:
  - Busca producto por ID
  - Mapea a ProductDto con categoría (EAGER loading)
  - Lanza `ProductNotFoundException` si no existe

- **save(ProductDto productDto)**:
  - Valida datos del DTO
  - Mapea a entidad Product
  - Guarda en BD y retorna DTO

- **update(ProductDto productDto)**:
  - Actualización full del producto
  - Acepta ProductDto con todos los campos
  - Mapea y guarda

- **update(Integer productId, ProductDto productDto)**:
  - Actualización por ID
  - Valida que producto exista primero
  - Preserva datos existentes

- **deleteById(Integer productId)**:
  - Hard delete del producto
  - Mapea a entidad y elimina
  - Lanza ProductNotFoundException si no existe

#### CategoryServiceImpl
Implementa lógica de negocios para categorías:

- **findAll()**:
  - Consulta todas las categorías
  - Mapea a CategoryDto (subcategorías y productos lazy-loaded)
  - Retorna lista inmutable sin duplicados

- **findById(Integer categoryId)**:
  - Busca categoría por ID
  - Retorna CategoryDto con relaciones
  - Lanza `CategoryNotFoundException` si no existe

- **save(CategoryDto categoryDto)**:
  - Mapea DTO a entidad
  - Soporta categorías padre-hijo (si parentCategoryId está presente)
  - Guarda y retorna DTO

- **update(CategoryDto categoryDto)**:
  - Actualización full de categoría
  - Puede cambiar categoría padre

- **update(Integer categoryId, CategoryDto categoryDto)**:
  - Actualización por ID
  - Valida existencia primero
  - Retorna DTO actualizado

- **deleteById(Integer categoryId)**:
  - Hard delete con cascada
  - Elimina subcategorías y productos (CASCADE)
  - Lanza CategoryNotFoundException si no existe

### Repositories

- **ProductRepository**: JpaRepository para entidad Product
  - Métodos básicos (findAll, findById, save, delete)

- **CategoryRepository**: JpaRepository para entidad Category
  - Métodos básicos (findAll, findById, save, deleteById)

## Características Especiales

### Estructura Jerárquica de Categorías
- Soporta subcategorías mediante auto-referencia
- ManyToOne apuntando a parentCategory
- OneToMany apuntando a subCategories (LAZY)
- Permite estructuras multinivel de categorías

### Lazy Loading Estratégico
- Subcategorías: LAZY loading (se cargan bajo demanda)
- Productos: LAZY loading (se cargan bajo demanda)
- Categoría de producto: EAGER loading (disponible inmediatamente)

### Cascade Delete
- Eliminar categoría elimina sus subcategorías (CASCADE)
- Eliminar categoría elimina sus productos (CASCADE)
- Operación en cascada y recursiva

### Mappings con DTOs
- ProductDto incluye CategoryDto anidado
- CategoryDto puede incluir Set<CategoryDto> (subcategorías)
- CategoryDto puede incluir Set<ProductDto> (productos)

### Transaccionalidad
- `@Transactional` a nivel de servicio
- Todas las operaciones en contexto de transacción
- Rollback automático en excepciones

### Service Discovery
- No requiere comunicación inter-servicios
- Servicio standalone de catálogo
- Consultado por: shipping-service, favourite-service, order-service

## DTOs y Responses

### ProductDto
- `productId`: Integer
- `productTitle`: String
- `imageUrl`: String
- `sku`: String (único)
- `priceUnit`: Double
- `quantity`: Integer
- `categoryDto`: CategoryDto (nested)

### CategoryDto
- `categoryId`: Integer
- `categoryTitle`: String
- `imageUrl`: String
- `subCategories`: Set<CategoryDto> (nullable)
- `parentCategoryDto`: CategoryDto (nullable)
- `productDtos`: Set<ProductDto> (nullable)

### DtoCollectionResponse
- `data`: List<ProductDto> o List<CategoryDto>

## Excepciones Personalizadas

- **ProductNotFoundException**: Cuando producto no existe
- **CategoryNotFoundException**: Cuando categoría no existe
