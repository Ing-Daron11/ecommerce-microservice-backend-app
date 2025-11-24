# Documentación de Nuevos Patrones de Diseño Implementados

Este documento detalla los tres patrones de diseño adicionales implementados en el microservicio `proxy-client` como parte de la mejora de la arquitectura del sistema.

## 1. Patrón de Resiliencia: Circuit Breaker (Cortacircuitos)

### Descripción
El patrón Circuit Breaker evita que un fallo en un servicio externo (en este caso, `product-service`) provoque fallos en cascada en todo el sistema. Si el servicio destino falla repetidamente, el "circuito se abre" y las llamadas posteriores fallan inmediatamente o se redirigen a un mecanismo de "fallback" sin esperar el timeout.

### Implementación
- **Tecnología**: Resilience4j + Spring Cloud Circuit Breaker.
- **Ubicación**: `com.selimhorri.app.business.product.service.ProductClientService`.
- **Fallback**: `ProductClientServiceFallback`.

### Comportamiento
Cuando el `product-service` no está disponible (caído o lento), el `proxy-client` captura la excepción y ejecuta el método de fallback, devolviendo una respuesta por defecto (ej. una lista vacía) en lugar de un error 500 Internal Server Error.

### Prueba / Endpoint
- **Endpoint**: `GET /app/api/products`
- **Escenario de Prueba (Kubernetes)**:
    1. Escalar a cero el microservicio de productos para simular una caída.
       ```bash
       kubectl scale deployment product-service --replicas=0
       ```
    2. Realizar la petición al proxy.
       ```bash
       # Desde un pod de prueba o vía Ingress/Port-forward
       curl -v http://proxy-client:8900/app/api/products
       ```
    3. **Resultado Esperado**: Código 200 OK con una lista vacía `[]` (Fallback activado).
    4. Restaurar el servicio:
       ```bash
       kubectl scale deployment product-service --replicas=1
       ```

---

## 2. Patrón de Configuración: Feature Toggle (Interruptor de Funcionalidad)

### Descripción
Este patrón permite activar o desactivar funcionalidades del sistema dinámicamente a través de la configuración, sin necesidad de modificar el código ni redesplegar la aplicación (si se usa con un servidor de configuración dinámico o variables de entorno).

### Implementación
- **Propiedad**: `feature.products.readonly` (mapeada a variable de entorno `FEATURE_PRODUCTS_READONLY`).
- **Ubicación**: `com.selimhorri.app.business.product.controller.ProductController`.

### Comportamiento
Se ha implementado un modo de "Solo Lectura" para los productos.
- Si `FEATURE_PRODUCTS_READONLY=false` (Default): Se permiten todas las operaciones.
- Si `FEATURE_PRODUCTS_READONLY=true`: Las operaciones de escritura (`POST`, `PUT`, `DELETE`) son bloqueadas y devuelven un error `403 Forbidden`.

### Prueba / Endpoint
- **Endpoints Afectados**:
    - `POST /app/api/products`
    - `PUT /app/api/products/{id}`
    - `DELETE /app/api/products/{id}`
- **Escenario de Prueba (Kubernetes)**:
    1. Activar el modo "Solo Lectura" mediante variable de entorno.
       ```bash
       kubectl set env deployment/proxy-client FEATURE_PRODUCTS_READONLY=true
       ```
    2. Esperar a que el pod se reinicie (`kubectl rollout status deployment/proxy-client`).
    3. Intentar crear un producto (POST).
       ```bash
       curl -v -X POST http://proxy-client:8900/app/api/products \
            -H "Content-Type: application/json" \
            -d '{"productTitle": "Test", "priceUnit": 100}'
       ```
    4. **Resultado Esperado**: Código `403 Forbidden`.
    5. Verificar que la lectura (GET) sigue funcionando:
       ```bash
       curl -v http://proxy-client:8900/app/api/products
       ```

---

## 3. Patrón de Comportamiento: Strategy Pattern (Estrategia)

### Descripción
El patrón Strategy permite definir una familia de algoritmos, encapsular cada uno de ellos y hacerlos intercambiables. Permite que el algoritmo varíe independientemente de los clientes que lo usan.

### Implementación
- **Interfaz**: `ProductSortStrategy`.
- **Estrategias Concretas**:
    1. `DefaultSortStrategy`: Devuelve la lista tal cual viene del servicio origen.
    2. `PriceSortStrategy`: Ordena la lista de productos por precio ascendente.
- **Ubicación**: `com.selimhorri.app.business.product.strategy`.

### Comportamiento
El controlador selecciona dinámicamente qué estrategia de ordenamiento usar basándose en el parámetro de consulta `sort`.

### Prueba / Endpoint
- **Endpoint**: `GET /app/api/products?sort={estrategia}`
- **Valores válidos**: `default`, `price`.

### Escenarios de Prueba:

**Caso A: Ordenamiento por Defecto**
```bash
curl -v "http://proxy-client:8900/app/api/products"
# Resultado: Lista en el orden original.
```

**Caso B: Ordenamiento por Precio**
```bash
curl -v "http://proxy-client:8900/app/api/products?sort=price"
# Resultado: Lista ordenada por el campo 'priceUnit' de menor a mayor.
```

---

## 4. Registro de Verificación (Pruebas en Cluster)

**Fecha**: 24 de Noviembre de 2025
**Entorno**: Kubernetes (Minikube)

Se han realizado pruebas exhaustivas de los tres patrones implementados para validar su correcto funcionamiento en un entorno real. A continuación se detallan los resultados obtenidos:

### ✅ Validación Circuit Breaker
- **Prueba**: Se escaló el despliegue `product-service` a 0 réplicas (`kubectl scale ... --replicas=0`) para simular una caída total del servicio backend.
- **Resultado**: El `proxy-client` detectó el fallo de conexión y ejecutó el método `fallback` definido.
- **Respuesta Observada**: Código `200 OK` con cuerpo JSON `{"collection": []}` (Lista vacía), evitando el error 500 que ocurriría sin el patrón.
- **Recuperación**: Al restaurar las réplicas a 1, el servicio volvió a mostrar la lista de productos automáticamente tras el periodo de refresco.

### ✅ Validación Feature Toggle
- **Prueba**: Se activó la variable de entorno `FEATURE_PRODUCTS_READONLY=true` en el despliegue `proxy-client`.
- **Resultado (Escritura)**: Las peticiones `POST` para crear productos fueron rechazadas inmediatamente con `403 Forbidden` por el controlador, confirmando el bloqueo de funcionalidad.
- **Resultado (Lectura)**: Las peticiones `GET` siguieron funcionando correctamente (`200 OK`), confirmando que el toggle es granular.
- **Reversión**: Al establecer la variable a `false`, el bloqueo desapareció y el flujo continuó normalmente.

### ✅ Validación Strategy Pattern
- **Prueba**: Se realizaron peticiones al endpoint `/app/api/products` con y sin el parámetro `sort`.
- **Resultado**:
    - Con `?sort=price`: El sistema inyectó y ejecutó `PriceSortStrategy`.
    - Sin parámetro: El sistema utilizó `DefaultSortStrategy`.
- **Observación**: El endpoint respondió correctamente (`200 OK`) en ambos casos, validando la selección dinámica de algoritmos en tiempo de ejecución.
