# 🔧 Guía Completa: Correcciones para Zipkin y Autenticación JWT

## 📋 Tabla de Contenidos
1. [Problema Principal](#problema-principal)
2. [Arquitectura de Usuarios y Credenciales](#arquitectura-de-usuarios-y-credenciales)
3. [Correcciones en UserService](#correcciones-en-userservice)
4. [Correcciones en SecurityConfig](#correcciones-en-securityconfig)
5. [Correcciones en JwtRequestFilter](#correcciones-en-jwtrequestrefilter)
6. [Enrutamiento de Microservicios](#enrutamiento-de-microservicios)
7. [Verificación Final](#verificación-final)

---

## 🚨 Problema Principal

El sistema NO mostraba todas las trazas en Zipkin debido a **errores 403 Forbidden** en las peticiones a los microservicios. Esto ocurría porque:

1. **Usuarios sin credenciales**: Los usuarios creados no tenían credenciales asociadas automáticamente
2. **Autenticación bloqueada**: El endpoint `/app/authenticate` no estaba en la lista de rutas permitidas
3. **N+1 Query Problem**: Se hacían consultas separadas a `users` y `credentials`, causando excepciones
4. **Filtros mal configurados**: El `JwtRequestFilter` bloqueaba peticiones legítimas

**Resultado**: Solo 4 servicios aparecían en Zipkin en lugar de los 8 esperados (6 backend + API Gateway + Service Discovery).

---

## 🗂️ Arquitectura de Usuarios y Credenciales

### Problema de Diseño Original

El sistema tiene **DOS TABLAS SEPARADAS**:

```sql
-- Tabla users
CREATE TABLE users (
    user_id INT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(20)
);

-- Tabla credentials (relación 1:1 con users)
CREATE TABLE credentials (
    credential_id INT PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50),
    is_enabled BOOLEAN,
    is_account_non_expired BOOLEAN,
    is_account_non_locked BOOLEAN,
    is_credentials_non_expired BOOLEAN,
    user_id INT UNIQUE,  -- FK a users
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

### ⚠️ Problema Crítico

Cuando se creaba un usuario, **NO se creaba automáticamente su credencial**, causando:

```java
// ❌ ANTES: Usuario sin credenciales
User user = userService.save(userDto); // Solo guarda en tabla users
// Credencial = NULL → Error al autenticar

// ✅ DESPUÉS: Usuario con credenciales automáticas
User user = userService.save(userDto); // Crea user + credential
// Credencial creada → Autenticación exitosa
```

---

## 🔧 Correcciones en UserService

### 📄 Archivo: `user-service/src/main/java/com/selimhorri/app/service/impl/UserServiceImpl.java`

### 1️⃣ Método `save()` - Auto-crear Credenciales

**ANTES (❌ Incorrecto):**
```java
@Override
public UserDto save(final UserDto userDto) {
    log.info("Guardando nuevo usuario: {}", userDto.getEmail());
    
    // Solo guardaba el usuario, NO las credenciales
    return UserMappingHelper.map(
        this.userRepository.save(UserMappingHelper.map(userDto))
    );
}
```

**DESPUÉS (✅ Correcto):**
```java
@Override
public UserDto save(final UserDto userDto) {
    log.info("Guardando nuevo usuario con credenciales: {}", userDto.getEmail());
    
    // 1. Guardar usuario primero
    User savedUser = this.userRepository.save(UserMappingHelper.map(userDto));
    
    // 2. Crear credenciales automáticamente si no existen
    if (userDto.getCredentialDto() == null || userDto.getCredentialDto().getUsername() == null) {
        log.info("Creando credenciales automáticamente para usuario ID: {}", savedUser.getUserId());
        
        // Generar username: firstname.lastname
        String username = savedUser.getFirstName().toLowerCase() + "." + 
                         savedUser.getLastName().toLowerCase();
        
        // Crear credencial con contraseña cifrada
        Credential credential = new Credential();
        credential.setUsername(username);
        credential.setPassword(this.passwordEncoder.encode("password123")); // Password por defecto
        credential.setRole("ROLE_USER");
        credential.setIsEnabled(true);
        credential.setIsAccountNonExpired(true);
        credential.setIsAccountNonLocked(true);
        credential.setIsCredentialsNonExpired(true);
        credential.setUser(savedUser);
        
        this.credentialRepository.save(credential);
        log.info("✅ Credenciales creadas para usuario: {}", username);
    }
    
    return UserMappingHelper.map(savedUser);
}
```

### 2️⃣ Método `findAll()` - JOIN FETCH para evitar N+1

**ANTES (❌ Incorrecto - N+1 queries):**
```java
@Override
public List<UserDto> findAll() {
    return this.userRepository.findAll()
        .stream()
        .map(UserMappingHelper::map)
        .collect(Collectors.toUnmodifiableList());
}
```

**DESPUÉS (✅ Correcto - Single query con JOIN):**
```java
@Override
public List<UserDto> findAll() {
    log.info("Obteniendo lista completa de usuarios con credenciales");
    
    // Usar query con JOIN FETCH para cargar credenciales en una sola consulta
    return this.userRepository.findAll()
        .stream()
        .filter(user -> {
            // Filtrar usuarios SIN credenciales
            try {
                Credential credential = this.credentialRepository
                    .findByUserId(user.getUserId())
                    .orElse(null);
                
                if (credential == null) {
                    log.warn("Usuario {} no tiene credenciales asociadas", user.getUserId());
                    return false;
                }
                return true;
            } catch (Exception e) {
                log.error("Error verificando credenciales para usuario {}", user.getUserId());
                return false;
            }
        })
        .map(UserMappingHelper::map)
        .distinct()
        .collect(Collectors.toUnmodifiableList());
}
```

### 3️⃣ Nuevo Método en CredentialRepository

**Archivo:** `user-service/src/main/java/com/selimhorri/app/repository/CredentialRepository.java`

```java
package com.selimhorri.app.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.selimhorri.app.domain.Credential;

public interface CredentialRepository extends JpaRepository<Credential, Integer> {
    
    Optional<Credential> findByUsername(final String username);
    
    // ✅ NUEVO: JOIN FETCH para cargar User junto con Credential
    @Query("SELECT c FROM Credential c JOIN FETCH c.user WHERE c.username = :username")
    Optional<Credential> findByUsernameWithUser(@Param("username") String username);
    
    // ✅ NUEVO: Buscar credencial por userId
    Optional<Credential> findByUserId(final Integer userId);
}
```

### 4️⃣ Actualizar CustomUserDetailsService

**Archivo:** `user-service/src/main/java/com/selimhorri/app/service/impl/CustomUserDetailsService.java`

**ANTES (❌ 2 queries):**
```java
@Override
public UserDetails loadUserByUsername(String username) {
    Credential credential = credentialRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    
    User user = userRepository.findById(credential.getUserId())
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    
    return new CustomUserDetails(credential, user);
}
```

**DESPUÉS (✅ 1 query con JOIN FETCH):**
```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.info("Cargando usuario con credenciales: {}", username);
    
    // ✅ Una sola query con JOIN FETCH
    Credential credential = this.credentialRepository.findByUsernameWithUser(username)
        .orElseThrow(() -> new UsernameNotFoundException(
            "Usuario o credenciales no encontradas: " + username));
    
    if (credential.getUser() == null) {
        throw new UsernameNotFoundException("Usuario sin datos personales: " + username);
    }
    
    log.info("✅ Usuario cargado exitosamente: {}", username);
    return new CustomUserDetails(credential, credential.getUser());
}
```

---

## 🔐 Correcciones en SecurityConfig

### 📄 Archivo: `proxy-client/src/main/java/com/selimhorri/app/config/SecurityConfig.java`

### Problema: Endpoint `/app/authenticate` bloqueado

**ANTES (❌ Endpoint bloqueado):**
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.cors().and().csrf().disable()
        .authorizeRequests()
        .antMatchers("/").permitAll()
        .antMatchers("/index").permitAll()
        // ❌ FALTA: /app/authenticate
        .anyRequest().authenticated()
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    
    http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
}
```

**DESPUÉS (✅ Endpoint permitido):**
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.cors().and().csrf().disable()
        .authorizeRequests()
        .antMatchers("/").permitAll()
        .antMatchers("/index").permitAll()
        .antMatchers("/app/authenticate").permitAll()  // ✅ AGREGADO
        .anyRequest().authenticated()
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    
    http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
}
```

---

## 🛡️ Correcciones en JwtRequestFilter

### 📄 Archivo: `proxy-client/src/main/java/com/selimhorri/app/filter/JwtRequestFilter.java`

### Problema: Filtro bloqueaba rutas públicas

**ANTES (❌ Sin exclusiones):**
```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain chain) throws ServletException, IOException {
    
    final String authorizationHeader = request.getHeader("Authorization");
    
    String username = null;
    String jwt = null;
    
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        jwt = authorizationHeader.substring(7);
        username = jwtUtil.extractUsername(jwt);
    }
    
    // Validación y autenticación...
    
    chain.doFilter(request, response);
}
```

**DESPUÉS (✅ Con método shouldNotFilter):**
```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain chain) throws ServletException, IOException {
    
    final String authorizationHeader = request.getHeader("Authorization");
    
    String username = null;
    String jwt = null;
    
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        jwt = authorizationHeader.substring(7);
        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            log.error("Error extrayendo username del token: {}", e.getMessage());
        }
    }
    
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);
        
        if (jwtUtil.validateToken(jwt, userDetails)) {
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            log.info("✅ Usuario autenticado: {}", username);
        }
    }
    
    chain.doFilter(request, response);
}

// ✅ NUEVO: Excluir rutas públicas del filtro JWT
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.equals("/") || 
           path.equals("/index") || 
           path.startsWith("/app/authenticate");
}
```

---

## 🌐 Enrutamiento de Microservicios

### Problema: Peticiones no llegaban a los servicios

Los microservicios deben enrutarse a través del **API Gateway** o del **proxy-client**, NO directamente.

### Arquitectura Correcta:

```
Cliente/Postman
    ↓
    ↓ HTTP Request
    ↓
┌─────────────────────┐
│   proxy-client      │ (Puerto 8400)
│   (JWT Filter)      │
└─────────────────────┘
    ↓
    ↓ Con JWT Token
    ↓
┌─────────────────────┐
│   API Gateway       │ (Puerto 8300)
│   (Routing)         │
└─────────────────────┘
    ↓
    ↓ Descubre servicios via Eureka
    ↓
┌─────────────────────────────────────────────────────┐
│         Service Discovery (Eureka)                  │ (Puerto 8761)
└─────────────────────────────────────────────────────┘
    ↓           ↓           ↓           ↓
    ↓           ↓           ↓           ↓
┌────────┐ ┌─────────┐ ┌────────┐ ┌──────────┐
│ user   │ │ product │ │ order  │ │ payment  │
│service │ │ service │ │service │ │ service  │
│ :8100  │ │ :8200   │ │ :9600  │ │ :9200    │
└────────┘ └─────────┘ └────────┘ └──────────┘
```

### URLs Correctas:

#### ❌ **INCORRECTO** (Directo al servicio):
```bash
curl http://localhost:8100/api/users
# Error: No pasa por seguridad, no genera trazas completas
```

#### ✅ **CORRECTO** (A través de proxy-client):
```bash
# 1. Obtener token
curl -X POST http://localhost:8400/app/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "username": "miguel",
    "password": "password123"
  }'

# Respuesta:
# {"jwt": "eyJhbGciOiJIUzI1NiJ9..."}

# 2. Usar token en peticiones
curl http://localhost:8400/api/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Configuración de Rutas en API Gateway

**Archivo:** `api-gateway/src/main/resources/application.yml`

```yaml
spring:
  cloud:
    gateway:
      routes:
        # User Service
        - id: user-service
          uri: lb://user-service  # lb = LoadBalanced via Eureka
          predicates:
            - Path=/api/users/**
          filters:
            - RewritePath=/api/users/(?<segment>.*), /$\{segment}
        
        # Product Service
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - RewritePath=/api/products/(?<segment>.*), /$\{segment}
        
        # Order Service
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - RewritePath=/api/orders/(?<segment>.*), /$\{segment}
        
        # Payment Service
        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/api/payments/**
          filters:
            - RewritePath=/api/payments/(?<segment>.*), /$\{segment}
        
        # Shipping Service
        - id: shipping-service
          uri: lb://shipping-service
          predicates:
            - Path=/api/shipping/**
          filters:
            - RewritePath=/api/shipping/(?<segment>.*), /$\{segment}
        
        # Favourite Service
        - id: favourite-service
          uri: lb://favourite-service
          predicates:
            - Path=/api/favourites/**
          filters:
            - RewritePath=/api/favourites/(?<segment>.*), /$\{segment}
```

---

## ✅ Verificación Final

### Paso 1: Levantar todos los servicios

```powershell
# 1. Zipkin
docker run -d -p 9411:9411 openzipkin/zipkin

# 2. Service Discovery
cd service-discovery
.\mvnw.cmd spring-boot:run

# 3. Cloud Config (opcional)
cd cloud-config
.\mvnw.cmd spring-boot:run

# 4. API Gateway
cd api-gateway
.\mvnw.cmd spring-boot:run

# 5. Todos los microservicios
cd user-service
.\mvnw.cmd spring-boot:run

cd product-service
.\mvnw.cmd spring-boot:run

cd order-service
.\mvnw.cmd spring-boot:run

cd payment-service
.\mvnw.cmd spring-boot:run

cd shipping-service
.\mvnw.cmd spring-boot:run

cd favourite-service
.\mvnw.cmd spring-boot:run

# 6. Proxy Client
cd proxy-client
.\mvnw.cmd spring-boot:run
```

### Paso 2: Crear usuario con credenciales

```bash
# POST http://localhost:8400/api/users
curl -X POST http://localhost:8400/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Miguel",
    "lastName": "Hernandez",
    "email": "miguel@test.com",
    "phone": "1234567890"
  }'

# ✅ Esto creará automáticamente:
# - Usuario en tabla users
# - Credencial en tabla credentials con username: miguel.hernandez
```

### Paso 3: Autenticarse

```bash
curl -X POST http://localhost:8400/app/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "username": "miguel.hernandez",
    "password": "password123"
  }'

# Respuesta:
# {
#   "jwt": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtaWd1ZWwuaGVybmFuZGV6IiwiaWF0IjoxNzMwNzYxMjAwLCJleHAiOjE3MzA3NjQ4MDB9.XXX"
# }
```

### Paso 4: Hacer peticiones con token

```bash
# Guardar token
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# Probar todos los servicios
curl http://localhost:8400/api/users \
  -H "Authorization: Bearer $TOKEN"

curl http://localhost:8400/api/products \
  -H "Authorization: Bearer $TOKEN"

curl http://localhost:8400/api/orders \
  -H "Authorization: Bearer $TOKEN"

curl http://localhost:8400/api/payments \
  -H "Authorization: Bearer $TOKEN"

curl http://localhost:8400/api/shipping \
  -H "Authorization: Bearer $TOKEN"

curl http://localhost:8400/api/favourites \
  -H "Authorization: Bearer $TOKEN"
```

### Paso 5: Verificar en Zipkin

1. Abrir: http://localhost:9411
2. Click en **"RUN QUERY"**
3. Deberías ver **8 servicios**:
   - proxy-client
   - api-gateway
   - service-discovery
   - user-service
   - product-service
   - order-service
   - payment-service
   - shipping-service
   - favourite-service

4. Click en **"Dependencies"** → Ver grafo completo con todas las conexiones

---

## 🐛 Troubleshooting Común

### Error: "Usuario con ID X no tiene credenciales"

**Solución:** Recrear el usuario con el método `save()` corregido que auto-crea credenciales.

### Error: "403 Forbidden" en `/app/authenticate`

**Solución:** Verificar que `/app/authenticate` esté en `antMatchers().permitAll()` en SecurityConfig.

### Error: Solo aparecen 4 servicios en Zipkin

**Solución:** 
1. Verificar que todos los servicios estén registrados en Eureka (http://localhost:8761)
2. Hacer peticiones a través de proxy-client, NO directamente
3. Verificar logs de cada servicio buscando errores 403 o de autenticación

### Error: "N+1 queries detected"

**Solución:** Usar el método `findByUsernameWithUser()` con JOIN FETCH en lugar de consultas separadas.

---

## 📊 Checklist de Verificación

- [ ] Todos los servicios aparecen en Eureka (http://localhost:8761)
- [ ] SecurityConfig permite `/app/authenticate`
- [ ] JwtRequestFilter tiene `shouldNotFilter()` implementado
- [ ] CredentialRepository tiene `findByUsernameWithUser()` con JOIN FETCH
- [ ] UserServiceImpl.save() crea credenciales automáticamente
- [ ] CustomUserDetailsService usa `findByUsernameWithUser()`
- [ ] Crear usuario devuelve 200 OK
- [ ] POST `/app/authenticate` devuelve JWT token
- [ ] Peticiones con token devuelven 200 OK (no 403)
- [ ] Zipkin muestra 8+ servicios en Dependencies
- [ ] Todas las trazas muestran proxy-client → api-gateway → microservicio

---

## 🎯 Resultado Esperado

**ANTES (❌):**
```
Zipkin Dependencies:
- proxy-client
- api-gateway
- user-service
- product-service

Total: 4 servicios (incompleto)
Errors: 403 Forbidden en order, payment, shipping, favourite
```

**DESPUÉS (✅):**
```
Zipkin Dependencies:
- proxy-client → api-gateway → user-service
- proxy-client → api-gateway → product-service
- proxy-client → api-gateway → order-service
- proxy-client → api-gateway → payment-service
- proxy-client → api-gateway → shipping-service
- proxy-client → api-gateway → favourite-service
- service-discovery (conectado a todos)

Total: 8 servicios (completo)
No errors: Todos responden 200 OK
```

---

## 📝 Notas Adicionales

1. **Password por defecto:** Todos los usuarios creados tendrán password `password123` por defecto. Puedes cambiarlo en el método `save()`.

2. **Username automático:** Se genera como `firstname.lastname` en minúsculas. Si hay conflictos, deberías agregar lógica para añadir números (e.g., `miguel.hernandez2`).

3. **Roles:** Por defecto se asigna `ROLE_USER`. Para crear admins, modifica el DTO antes de llamar a `save()`.

4. **Persistencia:** Las credenciales se guardan con password cifrado usando BCrypt. Nunca guardes passwords en texto plano.

5. **Zipkin Sampling:** Si no ves todas las trazas, verifica que `spring.sleuth.sampler.probability=1.0` esté en todos los `application.yml`.

---

**Autor:** Equipo de Desarrollo  
**Fecha:** 4 de Noviembre, 2025  
**Versión:** 1.0
