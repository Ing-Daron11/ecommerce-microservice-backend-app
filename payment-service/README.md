# Payment Service

Microservicio responsable de gestionar pagos y procesar transacciones en el sistema de e-commerce.

## Configuración

- **Puerto**: 8400
- **Context-path**: `/payment-service`
- **Base de datos**: H2 (en memoria para desarrollo)
- **Migración**: Flyway (V1-V2)

## Componentes Principales

### Domain Models

#### Payment
- **Atributos clave**:
  - `paymentId`: Identificador único (generado automáticamente)
  - `orderId`: Referencia a la orden a pagar
  - `isPayed`: Bandera booleana de pago completado
  - `paymentStatus`: Estado del pago (PaymentStatus enum)

- **Estados de Pago** (PaymentStatus enum):
  - `NOT_STARTED`: Pago creado pero no iniciado
  - `IN_PROGRESS`: Pago en proceso
  - `COMPLETED`: Pago finalizado exitosamente
  - `CANCELED`: Pago cancelado

- **Validaciones al guardar**:
  - El orderId DEBE existir y estar en estado ORDERED (no CREATED ni IN_PAYMENT)
  - Se valida mediante consulta a order-service
  - Lanza `PaymentServiceException` si la orden no existe

- **Máquina de estados**:
  - Transición automática: NOT_STARTED → IN_PROGRESS → COMPLETED
  - No se puede actualizar si está COMPLETED
  - No se puede actualizar si está CANCELED
  - Lanza `InvalidPaymentStatusException` para transiciones inválidas

- **Eliminación (Cancelación)**:
  - Soft cancel: establece estado a CANCELED en lugar de borrar
  - No permite cancelar si está COMPLETED
  - No permite cancelar si ya está CANCELED

### REST Endpoints

#### PaymentResource (`/api/payments`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/payments` | Obtiene todos los pagos con órdenes en estado IN_PAYMENT |
| GET | `/api/payments/{paymentId}` | Obtiene pago por ID |
| POST | `/api/payments` | Crea nuevo pago |
| PATCH | `/api/payments/{paymentId}` | Avanza el estado del pago |
| PUT | `/api/payments/{paymentId}` | Actualiza estado del pago (alias PATCH) |
| DELETE | `/api/payments/{paymentId}` | Cancela pago |

**Validaciones de entrada**:
- `paymentId`: No puede estar en blanco (NotBlank)
- `paymentDto`: No puede ser null (NotNull)

### Services

#### PaymentServiceImpl
Implementa lógica de negocios para pagos:

- **findAll()**:
  - Consulta todos los pagos desde la BD
  - Para cada pago, valida que orden esté en estado IN_PAYMENT
  - Filtra consultando order-service por cada pago
  - Maneja excepciones si order-service no responde (log error, filtra)
  - Retorna lista inmutable de pagos válidos

- **findById(Integer paymentId)**:
  - Busca pago por ID
  - Enriquece con datos de la orden desde order-service
  - Lanza `PaymentServiceException` si no existe o no puede obtener datos de orden

- **save(PaymentDto paymentDto)**:
  - Valida que orderId no sea null
  - Consulta orden en order-service
  - Verifica que orden exista
  - **Validación crítica**: Verifica que orden esté en estado ORDERED
  - Guarda el pago en BD
  - **IMPORTANTE**: Realiza PATCH a order-service para actualizar estado de orden a IN_PAYMENT
  - Si PATCH falla, lanza excepción pero pago ya fue guardado
  - Maneja excepciones de RestTemplate

- **updateStatus(int paymentId)**:
  - Transiciona estado del pago automáticamente
  - NOT_STARTED → IN_PROGRESS → COMPLETED
  - Lanza `InvalidPaymentStatusException` si ya está COMPLETED o CANCELED
  - Registra log con transición realizada

- **deleteById(Integer paymentId)**:
  - Cancela pago estableciendo estado a CANCELED
  - Solo permite si estado es NOT_STARTED o IN_PROGRESS
  - Bloquea cancelación si está COMPLETED
  - Bloquea cancelación si ya está CANCELED

### Repositories

- **PaymentRepository**: JpaRepository para entidad Payment
  - Métodos básicos (findAll, findById, save, delete)

## Comunicación con Otros Microservicios

### Servicios que Payment-Service consulta:

1. **order-service**:
   - URL: `http://ORDER-SERVICE/order-service/api/orders/{orderId}`
   - Propósito: Validar que orden existe y está en estado ORDERED
   - Tipo: RestTemplate con @LoadBalanced
   - Manejo de error: HttpClientErrorException.NotFound → PaymentServiceException

2. **order-service (actualización de estado)**:
   - URL: `http://ORDER-SERVICE/order-service/api/orders/{orderId}/status` (PATCH)
   - Propósito: Cambiar estado de orden a IN_PAYMENT cuando pago es creado
   - Tipo: RestTemplate.patchForObject()
   - Manejo de error: Log error y lanza PaymentServiceException

## Rutas de Integración

```
POST /api/payments (desde proxy-client)
  └─ Valida orderId existe en order-service
       └─ Verifica orden está en estado ORDERED
            └─ Crea pago en estado NOT_STARTED
            └─ PATCH order-service para actualizar a IN_PAYMENT
                 └─ Orden ahora lista para shipping-service

PATCH /api/payments/{paymentId}
  └─ NOT_STARTED → IN_PROGRESS → COMPLETED
       └─ Máquina de estados unidireccional

GET /api/payments
  └─ Obtiene pagos filtrando por órdenes en IN_PAYMENT
       └─ Consulta order-service para validar estado
```

## Características Especiales

### Soft Cancel
- DELETE establece estado a CANCELED en lugar de borrar
- Mantiene historial de pagos cancelados
- No permite retroceso de estado

### Integración Bidireccional con Order-Service
- Lectura: Consulta estado de orden antes de crear pago
- Escritura: Actualiza orden a IN_PAYMENT después de crear pago
- Sincronización crítica: Si PATCH falla, pago ya está guardado

### Validación de Estados
- Las consultas filtran por estado de orden válido (IN_PAYMENT)
- Las transiciones son unidireccionales
- Restricciones en cancelación según estado actual

### Transaccionalidad
- `@Transactional` a nivel de servicio
- Save con transacción pero PATCH fuera (puede fallar independientemente)
- Rollback automático en excepciones de negocio

### Service Discovery en Kubernetes
- RestTemplate configurado con `@LoadBalanced`
- Consultas usan nombre de servicio Kubernetes (ORDER-SERVICE)
- LoadBalancer automáticamente resuelve DNS

## DTOs y Responses

### PaymentDto
- `paymentId`: Integer
- `orderId`: Integer (dentro de orderDto)
- `isPayed`: Boolean
- `paymentStatus`: PaymentStatus (como String en JSON)
- `orderDto`: OrderDto (enriquecido desde order-service)

### DtoCollectionResponse
- `data`: List<PaymentDto>

## Excepciones Personalizadas

- **PaymentServiceException**: Errores al comunicarse con order-service o validaciones de negocio
- **PaymentNotFoundException**: Cuando pago no existe
- **InvalidPaymentStatusException**: Cuando transición de estado no es válida (HTTP 422 UNPROCESSABLE_ENTITY)
