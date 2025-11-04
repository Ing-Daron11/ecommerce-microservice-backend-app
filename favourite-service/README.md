# Favourite Service

Microservicio para gestionar favoritos de usuarios en el sistema de e-commerce.

## Configuración

- **Puerto**: 8800
- **Context-path**: `/favourite-service`
- **Base de datos**: H2 (en memoria)

## Componentes Principales

### Domain Model - Favourite

Entidad con **clave compuesta** (userId, productId, likeDate):
- `userId`: Identificador del usuario que marcó favorito
- `productId`: Identificador del producto marcado como favorito
- `likeDate`: Fecha y hora cuando se marcó como favorito (parte de la clave primaria)

### REST Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/favourites` | Obtiene todos los favoritos (enriquecidos con datos de user y product) |
| GET | `/api/favourites/{userId}/{productId}` | Obtiene un favorito específico |
| POST | `/api/favourites` | Crea nuevo favorito |
| DELETE | `/api/favourites/{userId}/{productId}` | Elimina un favorito |

### FavouriteServiceImpl

**findAll()**:
- Consulta todos los favoritos de BD
- Para cada uno: busca UserDto en user-service y ProductDto en product-service
- Filtra registros nulos (si user o product no existen)
- Manejo de excepciones: Log warning y exclusión si falla

**findById(FavouriteId)**:
- Busca por userId + productId
- Enriquece con datos de user-service y product-service
- Lanza FavouriteNotFoundException si favorito no existe o si user/product no se encuentran

**save(FavouriteDto)**:
- Valida que usuario exista en user-service → UserNotFoundException si no
- Valida que producto exista en product-service → ProductNotFoundException si no
- **Valida duplicados**: Lanza DuplicateEntityException si ya existe ese favorito
- Guarda con likeDate = ahora

**deleteById(FavouriteId)**:
- Verifica existencia antes de eliminar
- Lanza FavouriteNotFoundException si no existe
- Elimina por userId + productId

### Repositories

**FavouriteRepository** (JpaRepository<Favourite, FavouriteId>):
- `findByUserIdAndProductId(Integer, Integer)`: Búsqueda por clave compuesta
- `deleteByUserIdAndProductId(Integer, Integer)`: Eliminación por clave compuesta
- `existsByUserIdAndProductId(Integer, Integer)`: Verifica existencia

### Excepciones Personalizadas

1. **FavouriteNotFoundException**: 
   - Se lanza cuando el favorito no existe
   - Status HTTP: 404 NOT_FOUND

2. **UserNotFoundException**: 
   - Se lanza cuando usuario no existe en user-service
   - Status HTTP: 404 NOT_FOUND

3. **ProductNotFoundException**: 
   - Se lanza cuando producto no existe en product-service
   - Status HTTP: 404 NOT_FOUND

4. **DuplicateEntityException**: 
   - Se lanza cuando intenta crear un favorito que ya existe
   - Status HTTP: 409 CONFLICT

5. **InvalidFavouriteDataException** (nueva):
   - Se lanza para datos inválidos de favorito
   - Status HTTP: 400 BAD_REQUEST
   - Disponible para futuras validaciones

## Comunicación Inter-Microservicios

**Consulta a user-service**:
- URL: `http://USER-SERVICE/user-service/api/users/{userId}`
- Valida existencia del usuario antes de crear favorito
- Enriquece datos en GET (recupera username, email, etc.)

**Consulta a product-service**:
- URL: `http://PRODUCT-SERVICE/product-service/api/products/{productId}`
- Valida existencia del producto antes de crear favorito
- Enriquece datos en GET (recupera nombre, precio, categoría, etc.)

## Características Especiales

### Clave Primaria Compuesta
La tabla `favourites` tiene clave primaria de 3 campos:
- userId + productId: Identifica qué usuario marcó qué producto
- likeDate: Registra cuándo se marcó (permite auditoría temporal)
- Evita duplicados automáticamente a nivel BD

### Enriquecimiento de Datos
Los GET devoluelven no solo IDs, sino DTOs completos de user y product desde sus servicios respectivos.

### Manejo de Errores Robusto
- En findAll(): Tolerante, filtra nulos si algo falla
- En findById(): Estricto, lanza excepción si algo falta
- En save(): Valida ambos recursos antes de crear

### Service Discovery en Kubernetes
RestTemplate configurado con `@LoadBalanced` para resolver nombres de servicios Kubernetes automáticamente.

## DTOs

**FavouriteDto**:
- `userId`: Integer
- `productId`: Integer
- `likeDate`: LocalDateTime
- `userDto`: UserDto (enriquecido)
- `productDto`: ProductDto (enriquecido)
