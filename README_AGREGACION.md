# Filtro de Agregación de Respuestas - Gateway

## Descripción

Se ha implementado un filtro de agregación de respuestas en el gateway de Spring que permite obtener información resumida de un miembro incluyendo sus clases inscritas y pagos realizados.

## Endpoint Implementado

### GET /api/aggregation/members/{id}/summary

Obtiene el resumen completo de un miembro con información agregada de múltiples microservicios.

**Parámetros:**

- `id` (Long): ID del miembro

**Respuesta:**

```json
{
  "id": 1,
  "name": "Ana López",
  "email": "ana.lopez@email.com",
  "registrationDate": "2024-01-15",
  "enrolledClasses": [
    {
      "id": 1,
      "name": "Yoga Matutino",
      "schedule": "2024-01-15T10:00:00",
      "maxCapacity": 20,
      "coachId": 1,
      "enrolled": true
    }
  ],
  "payments": [
    {
      "id": 1,
      "memberId": 1,
      "amount": 50.0,
      "paymentDate": "2024-01-15T10:00:00Z"
    }
  ],
  "totalPayments": 50.0,
  "totalClasses": 1
}
```

## Componentes Implementados

### 1. DTOs (Data Transfer Objects)

- `MemberSummaryDTO`: DTO principal que contiene toda la información agregada
- `ClassSummaryDTO`: DTO para información de clases
- `PaymentSummaryDTO`: DTO para información de pagos

### 2. Servicios

- `MemberAggregationService`: Servicio que se encarga de obtener y combinar datos de múltiples microservicios

### 3. Controlador

- `MemberAggregationController`: Controlador REST que maneja el endpoint de agregación

### 4. Configuración

- `WebFluxConfig`: Configuración de WebFlux para el gateway
- `SecurityConfig`: Configuración de seguridad compatible con WebFlux

## Endpoints Adicionales Creados

### Microservicio de Pagos

- `GET /api/payment/all`: Obtener todos los pagos
- `GET /api/payment/member/{memberId}`: Obtener pagos por miembro
- `GET /api/payment/{id}`: Obtener pago por ID

### Microservicio de Clases

- `GET /api/classes/member/{memberId}`: Obtener clases por miembro

## Configuración

### Rutas del Gateway

Se agregó la siguiente ruta en `application.properties`:

```properties
spring.cloud.gateway.routes[6].id=member-aggregation
spring.cloud.gateway.routes[6].uri=lb://gateway
spring.cloud.gateway.routes[6].predicates[0]=Path=/api/members/*/summary
spring.cloud.gateway.routes[6].filters[0]=RewritePath=/api/members/(?<memberId>\\d+)/summary, /api/members/$\{memberId}/summary
```

### Seguridad

El endpoint está configurado para permitir acceso a usuarios autenticados con roles:

- `ROLE_ADMIN`
- `ROLE_COACH`
- `ROLE_MEMBER`

## Uso

1. **Autenticación**: Asegúrate de tener un token JWT válido de Keycloak
2. **Solicitud**: Realiza una petición GET a `http://localhost:8087/api/aggregation/members/{id}/summary`
3. **Headers**: Incluye el header `Authorization: Bearer {token}`

### Ejemplo de Uso con cURL

```bash
curl -X GET \
  http://localhost:8087/api/aggregation/members/1/summary \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

## Características

- **Agregación de Datos**: Combina información de 3 microservicios diferentes
- **Manejo de Errores**: Retorna respuestas apropiadas en caso de errores
- **Performance**: Utiliza WebClient para llamadas asíncronas
- **Seguridad**: Integrado con OAuth2/JWT
- **Documentación**: Incluye anotaciones Swagger/OpenAPI

## Dependencias Agregadas

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
</dependency>
```
