# Order Service

Microservicio responsable de gestionar órdenes y carritos de compra en el sistema de e-commerce.

## Configuración

- **Puerto**: 8300
- **Context-path**: `/order-service`
- **Base de datos**: H2 (en memoria para desarrollo)
- **Migración**: Flyway (V1-V5)

## Componentes Principales

### Domain Models

#### Order
- **Atributos clave**:
  - `orderId`: Identificador único (generado automáticamente)
  - `orderDate`: Fecha de creación de la orden (LocalDateTime)
  - `orderDesc`: Descripción de la orden
  - `orderFee`: Monto total de la orden
  - `status`: Estado de la orden (OrderStatus enum)
  - `cart`: Relación ManyToOne con Cart
  - `isActive`: Bandera de soft delete

- **Estados de Orden** (OrderStatus enum):
  - `CREATED`: Orden creada pero no procesada
  - `ORDERED`: Orden lista para pago (enviada a shipping-service)
  - `IN_PAYMENT`: Orden en proceso de pago

- **Validaciones en guardar**:
  - La orden DEBE estar asociada a un carrito válido
  - El cartId del DTO debe existir en la base de datos
  - Se valida mediante `cartRepository.findById()`

- **Restricciones en actualización de estado**:
  - Transición automática: CREATED → ORDERED → IN_PAYMENT
  - No se puede actualizar si está en IN_PAYMENT (ya pagado)
  - Se usa switch-case para transiciones de estado

- **Soft delete en eliminación**:
  - Solo se puede eliminar si estado es CREATED o ORDERED
  - No se puede eliminar si está IN_PAYMENT
  - Establece `isActive = false` en lugar de borrar

#### Cart
- **Atributos clave**:
  - `cartId`: Identificador único
  - `userId`: Referencia al usuario propietario
  - `orders`: Colección OneToMany de Orders
  - `isActive`: Bandera de soft delete

- **Relación con User**:
  - Se consulta a user-service mediante RestTemplate
  - URL: `http://USER-SERVICE/user-service/api/users/{userId}`
  - Se maneja excepción `HttpClientErrorException.NotFound` si usuario no existe

- **Consultas activas**:
  - `findAllByIsActiveTrue()`: Solo carritos activos
  - `findByCartIdAndIsActiveTrue()`: Búsqueda por ID con validación de estado

### REST Endpoints

#### OrderResource (`/api/orders`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/orders` | Obtiene todas las órdenes activas |
| GET | `/api/orders/{orderId}` | Obtiene orden por ID (solo activas) |
| POST | `/api/orders` | Crea nueva orden |
| PATCH | `/api/orders/{orderId}/status` | Avanza el estado de la orden (CREATED→ORDERED→IN_PAYMENT) |
| PUT | `/api/orders/{orderId}` | Actualiza orden existente |
| DELETE | `/api/orders/{orderId}` | Soft delete de orden (cambia isActive a false) |

**Validaciones de entrada**:
- `orderId`: No puede estar en blanco (NotBlank)
- `orderDto`: No puede ser null (NotNull)

#### CartResource (`/api/carts`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/carts` | Obtiene todos los carritos activos |
| GET | `/api/carts/{cartId}` | Obtiene carrito por ID (solo activos) |
| POST | `/api/carts` | Crea nuevo carrito |
| DELETE | `/api/carts/{cartId}` | Soft delete de carrito |

**Validaciones en CartService**:
- Al guardar: Valida que `userId` no sea null y que el usuario exista en user-service
- Al buscar por ID: Recupera datos del usuario de user-service
- Manejo de excepciones: Si user-service no responde, registra warning pero devuelve carrito sin datos de usuario
- Al borrar: Soft delete estableciendo `isActive = false`

### Services

#### OrderServiceImpl
Implementa lógica de negocios para órdenes:

- **findAll()**: 
  - Consulta solo órdenes activas desde la BD
  - Mapea a OrderDto
  - Retorna lista inmutable

- **findById(Integer orderId)**:
  - Busca orden activa por ID
  - Lanza OrderNotFoundException si no existe o está inactiva

- **save(OrderDto orderDto)**:
  - Limpia IDs para crear nuevo registro
  - Valida que orden tenga cartId asociado
  - Verifica que el carrito exista en BD
  - Lanza CartNotFoundException si carrito no existe

- **updateStatus(int orderId)**:
  - Transiciona estado de orden automáticamente
  - CREATED → ORDERED (compatible con shipping-service)
  - ORDERED → IN_PAYMENT (para payment-service)
  - Lanza IllegalStateException si ya está en IN_PAYMENT
  - Registra log con transición realizada

- **update(Integer orderId, OrderDto orderDto)**:
  - Actualiza orden existente preservando carrito y fecha original
  - Mantiene el estado actual (no lo modifica)
  - Valida que orden exista y esté activa

- **deleteById(Integer orderId)**:
  - Soft delete: establece `isActive = false`
  - Solo permite si estado es CREATED o ORDERED
  - Bloquea eliminación si está IN_PAYMENT

#### CartServiceImpl
Implementa lógica de negocios para carritos:

- **findAll()**:
  - Consulta carritos activos desde BD
  - Para cada carrito, consulta datos del usuario a user-service
  - Maneja excepciones si usuario no existe (log warning, devuelve carrito)
  - Usa RestTemplate con @LoadBalanced para Kubernetes service discovery

- **findById(Integer cartId)**:
  - Busca carrito activo por ID
  - Enriquece con datos del usuario desde user-service
  - Lanza CartNotFoundException si no existe o está inactivo

- **save(CartDto cartDto)**:
  - Valida que userId no sea null
  - Consulta usuario en user-service
  - Valida que usuario exista (UserNotFoundException si no)
  - Limpia cartId y orderDtos antes de guardar
  - Maneja excepciones de RestTemplate (HttpClientErrorException.NotFound, RestClientException)

- **deleteById(Integer cartId)**:
  - Soft delete estableciendo `isActive = false`
  - Lanza CartNotFoundException si carrito no existe

### Repositories

- **OrderRepository**: JpaRepository para entidad Order
  - `findAllByIsActiveTrue()`: Todas las órdenes activas
  - `findByOrderIdAndIsActiveTrue(Integer orderId)`: Orden activa por ID

- **CartRepository**: JpaRepository para entidad Cart
  - `findAllByIsActiveTrue()`: Todos los carritos activos
  - `findByCartIdAndIsActiveTrue(Integer cartId)`: Carrito activo por ID

## Comunicación con Otros Microservicios

### Servicios que Order-Service consulta:

1. **user-service**:
   - URL: `http://USER-SERVICE/user-service/api/users/{userId}`
   - Propósito: Validar existencia del usuario al crear/actualizar carrito
   - Tipo: RestTemplate con @LoadBalanced
   - Manejo de error: HttpClientErrorException.NotFound → UserNotFoundException

2. **shipping-service**:
   - Consumidor: shipping-service consulta order-service
   - URL que usa shipping-service: `http://ORDER-SERVICE/order-service/api/orders/{orderId}`
   - Validación: shipping-service verifica que `orderStatus == ORDERED`
   - Integración: Cuando estado pasa a ORDERED, está listo para envío

3. **payment-service** (futuro):
   - Integración pendiente
   - Responsable de recibir órdenes en estado ORDERED
   - Transiciona a IN_PAYMENT

## Rutas de Integración

```
POST /api/orders
  └─ Valida cartId → Busca en CartRepository
       └─ Si es nuevo carrito, pasa por CartService.save()
            └─ Consulta user-service para validar usuario
                 └─ Crea orden en estado CREATED

PATCH /api/orders/{orderId}/status
  └─ CREATED → ORDERED (orden lista para envío)
       └─ shipping-service puede consultarla
       └─ Valida que exista orden activa

GET /api/orders/{orderId}
  └─ shipping-service consulta esta ruta
       └─ Valida que orderStatus sea ORDERED
       └─ Procede con creación de shipment
```

## Características Especiales

### Soft Delete
Todas las operaciones de eliminación marcan registros como inactivos en lugar de borrar físicamente:
- `DELETE /api/carts/{cartId}` → `cart.isActive = false`
- `DELETE /api/orders/{orderId}` → `order.isActive = false` (si estado permite)

### Validación de Estados de Orden
- Las consultas filtran por estado `isActive` automáticamente
- Las transiciones de estado son unidireccionales e irreversibles
- No se permite retroceso de estados

### Transaccionalidad
- `@Transactional` a nivel de servicio
- Todas las operaciones se ejecutan en contexto de transacción
- Rollback automático en excepciones

### Logging
- Información de operaciones CRUD en nivel INFO
- Errores en nivel ERROR con contexto
- Debug de SQL en application-dev.yml

### Service Discovery en Kubernetes
- RestTemplate configurado con `@LoadBalanced`
- Consultas usan nombre de servicio Kubernetes (ej: USER-SERVICE)
- LoadBalancer automáticamente resuelve el DNS

## DTOs y Responses

### OrderDto
- `orderId`: Integer
- `orderDate`: LocalDateTime
- `orderDesc`: String
- `orderFee`: Double
- `status`: OrderStatus (como String en JSON)
- `cartDto`: CartDto (nested)

### CartDto
- `cartId`: Integer
- `userId`: Integer
- `orderDtos`: Set<OrderDto> (nested, nullable)
- `userDto`: UserDto (enriquecido desde user-service, nullable)

### DtoCollectionResponse
- `data`: List<T> (órdenes o carritos)

## Exceptiones Personalizadas

- **OrderNotFoundException**: Cuando orden no existe o está inactiva
- **CartNotFoundException**: Cuando carrito no existe o está inactivo
- **UserNotFoundException**: Cuando usuario no existe en user-service
- **IllegalArgumentException**: Cuando validaciones de negocio fallan
- **IllegalStateException**: Cuando transiciones de estado no son válidas
