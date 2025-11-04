# Shipping-Service - Correcciones Realizadas

## Resumen

Se implementó el servicio de shipping para gestionar items de envio, con integracion a order-service y product-service mediante RestTemplate con LoadBalancer para Kubernetes.

## Cambios Implementados

### Configuración Base
- Context-path: /shipping-service
- Puerto (dev): 8600
- Base de datos: H2 en memoria
- Zipkin: Configurado para trazas distribuidas
- Eureka: Registrado como SHIPPING-SERVICE

### OrderItemResource.java (Controlador REST)
- GET /api/shippings - Lista todos los items de envio activos
- GET /api/shippings/{orderId} - Obtiene item por ID
- POST /api/shippings - Crea nuevo item de envio
- DELETE /api/shippings/{orderId} - Elimina item

Todos los endpoints requieren autenticacion JWT (delegada a proxy-client).

### OrderItemServiceImpl.java (Lógica de Negocio)
- findAll(): Valida que el producto existe en product-service y la orden tiene estado ORDERED en order-service
- findById(): Obtiene item especifico por ID
- save(): Guarda nuevo item de envio
- deleteById(): Elimina item de envio
- update(): Actualiza item existente

Integración con servicios externos:
- Llamadas a ORDER_SERVICE_API_URL para validar ordenes
- Llamadas a PRODUCT_SERVICE_API_URL para validar productos
- RestTemplate con LoadBalancer para descubrimiento de servicios

### AppConstant.java
- URLs correctas con context-paths incluidos
- USER_SERVICE_API_URL
- PRODUCT_SERVICE_API_URL
- ORDER_SERVICE_API_URL
- FAVOURITE_SERVICE_API_URL
- PAYMENT_SERVICE_API_URL
- SHIPPING_SERVICE_API_URL

### ClientConfig.java
- RestTemplate configurado con LoadBalancer
- HttpClient con connection pooling
- Listo para comunicacion intra-microservicios en Kubernetes

## Arquitectura

El servicio recibe solicitudes del proxy-client (autenticadas), valida que la orden y el producto existan en sus respectivos servicios, y gestiona el ciclo de vida del envio.

Flujo de validacion:
1. Proxy-Client autentica JWT
2. Shipping-Service recibe solicitud
3. Valida orden en order-service
4. Valida producto en product-service
5. Procesa la logica de negocio

## Compilación

```bash
mvn clean package -pl shipping-service -DskipTests
```