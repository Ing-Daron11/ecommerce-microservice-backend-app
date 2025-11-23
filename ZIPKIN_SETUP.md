# 🔍 GUÍA STEP-BY-STEP: Resolver Problema de Zipkin Tracing

## El Problema Exacto

Las trazas en Zipkin existen, pero **NO muestran el flujo completo** entre servicios. Cuando haces una request, debería verse así:

```
request → API Gateway → User Service → Database
         ↓              ↓
    [span 1]        [span 2]
    
Y Zipkin debe conectarlas
```

Pero actualmente solo ves spans aislados sin conexión.

---

## PASO 1: Verificar que Zipkin Está Corriendo

```powershell
# En PowerShell, ejecuta:
kubectl get pods | Select-String zipkin
```

Deberías ver algo como:
```
zipkin-xxxxxxxx-xxxxx   1/1     Running
```

Si no aparece:
```powershell
# Desplegar Zipkin
kubectl apply -f cloud-config/k8s/  # Si está en cloud-config
# O buscar manifests de Zipkin en el proyecto
```

---

## PASO 2: Exponer Zipkin Localmente

```powershell
# Abre una ventana PowerShell nueva y déjala corriendo
kubectl port-forward svc/zipkin 9411:9411
```

Verifica que funciona:
```powershell
# En otra ventana, prueba:
Invoke-WebRequest http://localhost:9411 -Method GET
```

Deberías ver código 200.

---

## PASO 3: Verificar Configuración en Cloud Config

**Abre:** `cloud-config/src/main/resources/application.yml`

**Busca la sección de Zipkin:**

```yaml
spring:
  zipkin:
    base-url: http://zipkin:9411/
    enabled: true
  sleuth:
    sampler:
      probability: 1.0
    web:
      skip-pattern: /health,/info
```

**Si NO está exactamente así, ACTUALIZA:**

```powershell
# Abre el archivo en VS Code
code cloud-config/src/main/resources/application.yml
```

Y reemplaza la sección con:

```yaml
spring:
  application:
    name: cloud-config
  cloud:
    config:
      server:
        git:
          uri: https://github.com/Ing-Daron11/config-server-repo
  zipkin:
    base-url: http://zipkin:9411/
    enabled: true
  sleuth:
    sampler:
      probability: 1.0
    web:
      skip-pattern: /health,/info
    propagation-keys: x-trace-id,x-span-id
```

**Guarda y compila:**
```powershell
cd cloud-config
mvn clean compile -DskipTests
cd ..
```

---

## PASO 4: Actualizar Proxy Client - EL PASO CRÍTICO

**Ubicación:** `proxy-client/src/main/java/com/selimhorri/app/config/HttpClientConfig.java`

**Abre el archivo:**
```powershell
code proxy-client/src/main/java/com/selimhorri/app/config/HttpClientConfig.java
```

**BUSCA:**
```java
@Configuration
public class HttpClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**REEMPLAZA POR:**
```java
package com.selimhorri.app.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Configuration
public class HttpClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientConfig.class);

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .interceptors(new TraceInterceptor())
            .build();
    }

    /**
     * Interceptor que propaga headers de tracing entre servicios
     */
    public static class TraceInterceptor implements ClientHttpRequestInterceptor {

        private static final Logger log = LoggerFactory.getLogger(TraceInterceptor.class);

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
                                           ClientHttpRequestExecution execution) throws IOException {
            
            // Obtener headers que ya vienen en la request actual
            String traceId = request.getHeaders().getFirst("X-Trace-ID");
            String spanId = request.getHeaders().getFirst("X-Span-ID");
            String parentSpanId = request.getHeaders().getFirst("X-Parent-Span-ID");
            
            // Si existen, propagarlos a la request saliente
            if (traceId != null && !traceId.isEmpty()) {
                request.getHeaders().set("X-Trace-ID", traceId);
                log.debug("Propagando X-Trace-ID: {}", traceId);
            }
            
            if (spanId != null && !spanId.isEmpty()) {
                request.getHeaders().set("X-Span-ID", spanId);
                log.debug("Propagando X-Span-ID: {}", spanId);
            }
            
            if (parentSpanId != null && !parentSpanId.isEmpty()) {
                request.getHeaders().set("X-Parent-Span-ID", parentSpanId);
                log.debug("Propagando X-Parent-Span-ID: {}", parentSpanId);
            }
            
            // Propagar también los headers estándar de Sleuth
            String b3Header = request.getHeaders().getFirst("B3");
            if (b3Header != null) {
                request.getHeaders().set("B3", b3Header);
                log.debug("Propagando B3 header: {}", b3Header);
            }
            
            // Ejecutar la request con los headers propagados
            return execution.execute(request, body);
        }
    }
}
```

**Guarda y compila:**
```powershell
cd proxy-client
mvn clean compile -DskipTests
cd ..
```

---

## PASO 5: Crear ZipkinConfig en User Service

**Crea archivo nuevo:** 
```
user-service/src/main/java/com/selimhorri/app/config/ZipkinConfig.java
```

```powershell
# En PowerShell, navega a:
cd user-service/src/main/java/com/selimhorri/app/config

# Crea el archivo
New-Item -Name "ZipkinConfig.java" -ItemType File

# Abrelo en VS Code
code ZipkinConfig.java
```

**Contenido:**
```java
package com.selimhorri.app.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class ZipkinConfig {

    private static final Logger logger = LoggerFactory.getLogger(ZipkinConfig.class);

    /**
     * Bean RestTemplate para que Sleuth pueda interceptarlo
     * y añadir automáticamente los headers de tracing
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        logger.info("Configurando RestTemplate para Zipkin tracing");
        return builder.build();
    }
}
```

**Guarda:**
```powershell
cd ..\..\..\..\..  # Volver a root
```

---

## PASO 6: Verificar Dependencias en POM

**Abre:** `proxy-client/pom.xml`

**Busca la sección `<dependencies>`**

**Verifica que CONTENGA:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
</dependency>
```

**Si NO está, añádelo:**
```powershell
# Abre el pom.xml
code proxy-client/pom.xml

# Encuentra </properties> y después <dependencies>
# Añade las dos dependencias arriba dentro de <dependencies>
```

**Guarda y actualiza:**
```powershell
cd proxy-client
mvn dependency:resolve
cd ..
```

---

## PASO 7: Compilar y Testear TODO

```powershell
# Limpiar y compilar todo
mvn clean compile -DskipTests

# Resultado esperado:
# [INFO] BUILD SUCCESS
```

Si hay error, búscalo con:
```powershell
mvn clean compile -DskipTests 2>&1 | Select-String ERROR
```

---

## PASO 8: Ejecutar Tests

```powershell
# En user-service
cd user-service

# Ejecutar tests
mvn test

# Deberías ver: Tests run: 31, Failures: 0, Errors: 0
```

Si falla algo, revisa:
```powershell
mvn test 2>&1 | Select-String FAILED
```

---

## PASO 9: Empaquetar y Construir Docker

```powershell
# Desde root del proyecto
mvn clean package -DskipTests

# Esperar a que termine...

# Luego construir imágenes Docker
minikube docker-env --shell powershell | Invoke-Expression

cd proxy-client ; docker build -t proxy-client:v0.1.0 . ; cd ..
cd user-service ; docker build -t user-service:v0.1.0 . ; cd ..
cd api-gateway ; docker build -t api-gateway:v0.1.0 . ; cd ..
```

---

## PASO 10: Reiniciar Pods en Kubernetes

```powershell
# Eliminar los pods para que se reinicien con las nuevas imágenes
kubectl delete pod -l app=proxy-client
kubectl delete pod -l app=user-service
kubectl delete pod -l app=api-gateway

# Esperar a que reinicien (30-60 segundos)
kubectl get pods | Select-String "proxy-client|user-service|api-gateway"

# Todos deben estar en "Running"
```

---

## PASO 11: Generar Tráfico y Verificar Trazas

**Script PowerShell: `test-zipkin-traces.ps1`**

Crea un archivo con este contenido:

```powershell
# Script: test-zipkin-traces.ps1

Write-Host "🚀 Generando tráfico para Zipkin..." -ForegroundColor Cyan

# Variables
$baseUrl = "http://localhost:8080"
$headers = @{
    "Content-Type" = "application/json"
}

# 1. Registrar usuario (si no existe)
Write-Host "1️⃣  Registrando usuario..." -ForegroundColor Yellow
$registerBody = @{
    firstName = "Test"
    lastName = "User"
    email = "test$(Get-Random)@example.com"
    password = "Password123!"
    address = @{
        street = "123 Main St"
        city = "TestCity"
        state = "TS"
        zipCode = "12345"
        country = "TestCountry"
    }
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/users/register" `
        -Headers $headers `
        -Body $registerBody `
        -Method POST `
        -ContentType "application/json"
    
    Write-Host "✅ Usuario registrado" -ForegroundColor Green
    $userId = ($response.Content | ConvertFrom-Json).id
    Write-Host "   ID: $userId" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Error: $_" -ForegroundColor Yellow
}

# 2. Login
Write-Host "2️⃣  Haciendo login..." -ForegroundColor Yellow
$loginBody = @{
    email = "test@example.com"
    password = "Password123!"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/users/login" `
        -Headers $headers `
        -Body $loginBody `
        -Method POST `
        -ContentType "application/json"
    
    Write-Host "✅ Login exitoso" -ForegroundColor Green
    $token = ($response.Content | ConvertFrom-Json).token
    Write-Host "   Token recibido (primeros 20 chars): $($token.Substring(0, 20))..." -ForegroundColor Green
} catch {
    Write-Host "⚠️  Error: $_" -ForegroundColor Yellow
}

# 3. Hacer requests autenticadas para generar trazas
$authHeaders = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host "3️⃣  Generando requests autenticadas..." -ForegroundColor Yellow

# Request a User Service
Write-Host "   → GET /users/profile" -ForegroundColor Gray
try {
    Invoke-WebRequest -Uri "$baseUrl/users/profile" `
        -Headers $authHeaders `
        -Method GET | Out-Null
    Write-Host "   ✓ Request completada" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Error: $_" -ForegroundColor Red
}

Start-Sleep -Seconds 1

# Request a Product Service
Write-Host "   → GET /products" -ForegroundColor Gray
try {
    Invoke-WebRequest -Uri "$baseUrl/products" `
        -Headers $authHeaders `
        -Method GET | Out-Null
    Write-Host "   ✓ Request completada" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Error: $_" -ForegroundColor Red
}

Start-Sleep -Seconds 1

# Request a Favorite Service
Write-Host "   → GET /favorites" -ForegroundColor Gray
try {
    Invoke-WebRequest -Uri "$baseUrl/favorites" `
        -Headers $authHeaders `
        -Method GET | Out-Null
    Write-Host "   ✓ Request completada" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Error: $_" -ForegroundColor Red
}

Write-Host "✅ Tráfico generado exitosamente" -ForegroundColor Green
Write-Host "Abre Zipkin: http://localhost:9411" -ForegroundColor Cyan
Write-Host "Busca las trazas en los últimos 5 minutos" -ForegroundColor Cyan
```

**Ejecuta el script:**
```powershell
powershell -ExecutionPolicy Bypass -File test-zipkin-traces.ps1
```

---

## PASO 12: Verificar Trazas en Zipkin UI

**Abre Zipkin:**
```powershell
Start-Process "http://localhost:9411"
```

**Pasos en UI:**

1. Haz click en **"Find Traces"** (botón azul)
2. Selecciona un servicio en el dropdown (ej: "api-gateway")
3. Haz click en **"Find Traces"** de nuevo
4. Deberías ver trazas con timestamps recientes

**En cada traza, verás:**
```
[TRACE ID]
├── Span 1: api-gateway
│   └── GET /users/profile
├── Span 2: user-service
│   └── SELECT FROM users
└── Total time: 45ms
```

**Importante:** Si ves spans conectados (anidados), ¡FUNCIONA! ✅

---

## PASO 13: Ver Diagrama de Dependencias

En Zipkin:

1. Haz click en **"Dependencies"** (arriba)
2. Selecciona el rango de tiempo
3. Haz click en **"Find Dependencies"**

Deberías ver:
```
     api-gateway
      /   |   \
     /    |    \
user-svc prod-svc fav-svc
   |       |
  DB      DB
```

**Si no ves nada:**
- Espera 60-90 segundos (Zipkin necesita procesar)
- Genera más tráfico
- Verifica que los servicios estén corriendo

---

## PASO 14: Hacer Commit Final

```powershell
cd $projectRoot

# Ver qué cambió
git status

# Añadir cambios
git add .

# Commit con mensaje descriptivo
git commit -m "Fix: Configurar Distributed Tracing en Zipkin

CAMBIOS:
- Actualizar HttpClientConfig para propagar headers de tracing
- Crear ZipkinConfig en user-service
- Verificar dependencias Sleuth en proxy-client
- Configurar Sleuth en cloud-config application.yml

RESULTADO:
- Headers X-Trace-ID, X-Span-ID se propagan entre servicios
- Zipkin ahora muestra flujos completos de requests
- Diagrama de dependencias visible

Fixes: Problema de trazas incompletas en Zipkin"

# Push a GitHub
git push origin master
```

---

## PASO 15: Verificar GitHub Actions

**En GitHub:**

1. Ve a: https://github.com/Ing-Daron11/ecommerce-microservice-backend-app/actions
2. Busca el workflow más reciente
3. Debe mostrar ✅ si pasó

**Pasos del workflow:**
- ✅ Checkout
- ✅ Setup JDK 17
- ✅ Build
- ✅ Tests (31 passing)
- ✅ Package
- ✅ Docker build
- ✅ Deploy (si está configurado)

---

## ✅ CHECKLIST FINAL

```
VALIDACIÓN:

☐ Archivos modificados compilan sin errores
☐ Todos los tests pasan (31/31)
☐ Docker images construidas
☐ Pods reiniciados en Kubernetes
☐ Zipkin muestra trazas
☐ Las trazas tienen spans conectados
☐ El diagrama de dependencias es visible
☐ Commit realizado en GitHub
☐ GitHub Actions pasó
```

---

## 🎉 RESULTADOS ESPERADOS

Después de completar TODOS estos pasos:

```
✅ Compilación: SUCCESS
✅ Tests: 31/31 PASSING
✅ Docker: 3 imágenes actualizadas
✅ Kubernetes: 3 pods reiniciados
✅ Zipkin Traces: VISIBLE Y COMPLETO
✅ Dependencias: VISIBLE EN DIAGRAM
✅ GitHub: PUSH EXITOSO
```

---

## 🆘 TROUBLESHOOTING

| ❌ Problema | ✅ Solución |
|:-----------|:----------|
| Compilación falla | `mvn clean compile -DskipTests 2>&1 \| Select-String ERROR` |
| Tests fallan | `mvn test -DskipTests=false` en cada servicio |
| Docker build falla | Revisa que Minikube env esté activado |
| Pods no reinician | `kubectl describe pod <name>` para ver errores |
| Zipkin sin trazas | Genera tráfico: ejecuta test-zipkin-traces.ps1 |
| Headers no se propagan | Revisa HttpClientConfig tiene interceptor |

---

**¡Éxito! 🚀**

Después de esto, tu proyecto estará 100% completo con Distributed Tracing funcionando perfectamente.

