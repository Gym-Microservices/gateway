# 🚪 Gateway

## 📋 Descripción

API Gateway que actúa como punto de entrada único para todos los microservicios del gimnasio (Puerto 8087).
Maneja autenticación JWT con Keycloak y proporciona enrutamiento automático a los microservicios.

## 🔐 Autenticación

- **Keycloak JWT**: Validación automática de tokens
- **Rutas protegidas**: Requieren `Authorization: Bearer <token>`
- **Rutas públicas**: `/test` endpoints para verificación básica

## 🛤️ Rutas Configuradas

### 🔗 Microservicios (Autenticación Requerida)

- `/member-microservice/api/members/**` → Member Service (Puerto 8081)
- `/coach-microservice/api/coaches/**` → Coach Service (Puerto 8082)
- `/equipment-microservice/api/equipment/**` → Equipment Service (Puerto 8083)
- `/class-microservice/api/classes/**` → Class Service (Puerto 8084)
- `/notification-microservice/api/notifications/**` → Notification Service (Puerto 8085)
- `/payment-microservice/api/payment/**` → Payment Service (Puerto 8086)

### 🌐 Acceso Público

- Swagger UI en cada microservicio: `http://localhost:808X/swagger-ui.html`
- Health checks: `/actuator/health`

## ✨ Características

- Autenticación JWT centralizada con Keycloak
- Descubrimiento automático de servicios (Eureka)
- Propagación de información de usuario a microservicios
- Balanceeo de carga automático
- Endpoints de monitoreo

## 🛠️ Tecnologías

- Spring Boot
- Spring Cloud Gateway
- Spring Security (OAuth2 Resource Server)
- Eureka Client
