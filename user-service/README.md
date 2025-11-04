# User Service - README de Correcciones Realizadas

---

## Resumen de Cambios

---

## 1. UserService - Correcciones Implementadas

### Archivo: `UserServiceImpl.java`

Cambios realizados:

- **findAll()**: Filtra usuarios que tengan credenciales asociadas
- **findById()**: Verifica que el usuario tenga credenciales antes de retornar
- **findByUsername()**: Implementado para buscar usuario por nombre de usuario
- **save()**: Asigna userId a null para evitar conflictos con auto-incremento
- **update(UserDto)**: Actualiza solo campos permitidos, sin modificar credenciales
- **update(Integer, UserDto)**: Sobrecarga para actualizar por userId específico

Resultado: Los usuarios se crean y actualizan correctamente, con validación de credenciales asociadas.

---

## 2. CredentialService - Correcciones Implementadas

### Archivo: `CredentialServiceImpl.java`

Cambios realizados:

- **findAll()**: Retorna todas las credenciales mapeadas correctamente
- **findById()**: Busca credencial por ID con manejo de excepciones
- **findByUsername()**: Implementado para búsqueda por nombre de usuario
- **save()**: 
  - Asigna credentialId a null para crear nuevo
  - Valida que el username no exista (evita duplicados)
  - Asocia la credencial a un usuario existente
  - Codifica la contraseña con PasswordEncoder
  - Verifica que el usuario no tenga ya credenciales
  
- **update()**: 
  - Busca la credencial existente
  - Actualiza username y contraseña
  - Codifica la nueva contraseña
  - Retorna la credencial actualizada

- **deleteById()**: Elimina la credencial por ID

Resultado: Las operaciones CRUD funcionan correctamente con encriptación de contraseñas.

---

## 3. AddressService - Correcciones Implementadas

### Archivo: `AddressServiceImpl.java`

Cambios realizados:

- **findAll()**: Retorna todas las direcciones mapeadas
- **findById()**: Busca dirección por ID con manejo de excepciones
- **save()**: Guarda una nueva dirección
- **update(AddressDto)**: 
  - Busca la dirección existente
  - Actualiza solo campos editables (fullAddress, postalCode, city)
  - No modifica la referencia del usuario

- **update(Integer, AddressDto)**: 
  - Sobrecarga para actualizar por addressId
  - Realiza la actualización sin crear duplicados

- **deleteById()**: Elimina la dirección por ID

Resultado: Las direcciones se actualizan correctamente sin crear duplicados.

---

## 4. Configuración Compatible con Minikube

La siguiente configuración se mantiene intacta y es compatible con el cluster:

```yaml
# application.yml
server:
  servlet:
    context-path: /user-service

spring:
  zipkin:
    base-url: ${SPRING_ZIPKIN_BASE_URL:http://localhost:9411/}
  sleuth:
    sampler:
      probability: 1.0
  config:
    import: ${SPRING_CONFIG_IMPORT:optional:configserver:http://localhost:9296}
  application:
    name: USER-SERVICE

# application-dev.yml
server:
  port: 8700
```

---

## 5. Endpoints Operacionales

### Users API
- GET `/user-service/api/users` - Obtener todos los usuarios
- GET `/user-service/api/users/{userId}` - Obtener usuario por ID
- GET `/user-service/api/users/username/{username}` - Obtener usuario por username
- POST `/user-service/api/users` - Crear usuario
- PUT `/user-service/api/users` - Actualizar usuario (body)
- PUT `/user-service/api/users/{userId}` - Actualizar usuario (path)
- DELETE `/user-service/api/users/{userId}` - Eliminar usuario

### Credentials API
- GET `/user-service/api/credentials` - Obtener todas las credenciales
- GET `/user-service/api/credentials/{credentialId}` - Obtener por ID
- GET `/user-service/api/credentials/username/{username}` - Obtener por username
- POST `/user-service/api/credentials` - Crear credencial
- PUT `/user-service/api/credentials` - Actualizar credencial (body)
- PUT `/user-service/api/credentials/{credentialId}` - Actualizar credencial (path)
- DELETE `/user-service/api/credentials/{credentialId}` - Eliminar credencial

### Address API
- GET `/user-service/api/address` - Obtener todas las direcciones
- GET `/user-service/api/address/{addressId}` - Obtener por ID
- POST `/user-service/api/address` - Crear dirección
- PUT `/user-service/api/address` - Actualizar dirección (body)
- PUT `/user-service/api/address/{addressId}` - Actualizar dirección (path)
- DELETE `/user-service/api/address/{addressId}` - Eliminar dirección

### Verification Tokens API
- GET `/user-service/api/verificationTokens` - Obtener todos
- GET `/user-service/api/verificationTokens/{id}` - Obtener por ID
- POST `/user-service/api/verificationTokens` - Crear token
- PUT `/user-service/api/verificationTokens` - Actualizar token (body)
- PUT `/user-service/api/verificationTokens/{id}` - Actualizar token (path)
- DELETE `/user-service/api/verificationTokens/{id}` - Eliminar token

---



