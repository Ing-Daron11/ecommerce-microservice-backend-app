# Patrones de Diseño en la Arquitectura Actual

## 1. Patrones de Arquitectura de Microservicios

### Service Discovery (Descubrimiento de Servicios)
- **Implementación**: Netflix Eureka (`service-discovery`).
- **Descripción**: Permite que los microservicios se registren y se descubran entre sí dinámicamente sin conocer sus direcciones IP físicas.

### API Gateway (Puerta de Enlace)
- **Implementación**: Spring Cloud Gateway (`api-gateway`).
- **Descripción**: Actúa como punto de entrada único para todas las peticiones externas, enrutando el tráfico a los microservicios correspondientes.

### External Configuration (Configuración Externa)
- **Implementación**: Spring Cloud Config (`cloud-config`).
- **Descripción**: Centraliza la gestión de la configuración de todos los microservicios en un repositorio externo (Git).

### Distributed Tracing (Rastreo Distribuido)
- **Implementación**: Zipkin + Spring Cloud Sleuth.
- **Descripción**: Permite rastrear peticiones a través de múltiples microservicios para monitoreo y depuración.

### Backend for Frontend (BFF) / Aggregator
- **Implementación**: `proxy-client`.
- **Descripción**: Un servicio que actúa como intermediario para agregar o adaptar datos de múltiples microservicios antes de enviarlos al cliente.

### Circuit Breaker (Cortacircuitos)
- **Implementación**: Resilience4j (`proxy-client`).
- **Descripción**: Evita fallos en cascada deteniendo las llamadas a un servicio que falla repetidamente y proporcionando una respuesta de fallback.

### Bulkhead (Mamparos)
- **Implementación**: Resilience4j (`proxy-client`).
- **Descripción**: Aísla recursos (como hilos de ejecución) para que el fallo o saturación de un servicio no agote los recursos de toda la aplicación.

## 2. Patrones de Diseño de Software (GoF y otros)

### Data Transfer Object (DTO)
- **Uso**: Extensivo en todos los servicios (ej. `ProductDto`, `OrderDto`).
- **Descripción**: Objetos que transportan datos entre procesos para reducir el número de llamadas a métodos.

### Repository Pattern
- **Uso**: Interfaces que extienden `JpaRepository` (ej. `OrderRepository`).
- **Descripción**: Abstrae el acceso a datos, ocultando los detalles de la persistencia.

### Builder Pattern
- **Uso**: Anotación `@Builder` de Lombok.
- **Descripción**: Permite la construcción de objetos complejos paso a paso.

### Singleton
- **Uso**: Beans de Spring (`@Service`, `@Component`, `@RestController`).
- **Descripción**: Garantiza que una clase tenga una única instancia y proporciona un punto de acceso global a ella.

### MVC (Model-View-Controller)
- **Uso**: Spring Web MVC en todos los servicios.
- **Descripción**: Separa la lógica de negocio (Controller/Service) de la representación (View/JSON) y los datos (Model).

### Strategy Pattern (Estrategia)
- **Uso**: `ProductSortStrategy` en `proxy-client`.
- **Descripción**: Permite seleccionar dinámicamente el algoritmo de ordenamiento de productos en tiempo de ejecución.

### Feature Toggle (Interruptor de Funcionalidad)
- **Uso**: `ProductController` en `proxy-client`.
- **Descripción**: Permite activar o desactivar funcionalidades (como el modo "Solo Lectura") mediante configuración sin modificar el código.
