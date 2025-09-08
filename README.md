# 🚪 Gateway

## 📋 Descripción

API Gateway que actúa como punto de entrada único para todos los microservicios del gimnasio (Puerto 8087). Proporciona enrutamiento automático y balanceeo de carga a través de Eureka.

## 🛤️ Rutas Configuradas

### 🔗 Microservicios

- `/api/members/**` → `member-microservice` (Puerto 8081)
- `/api/coaches/**` → `coach-microservice` (Puerto 8082)
- `/api/equipment/**` → `equipment-microservice` (Puerto 8083)
- `/api/classes/**` → `class-microservice` (Puerto 8084)
- `/api/notifications/**` → `notification-microservice` (Puerto 8085)
- `/api/payment/**` → `payment-microservice` (Puerto 8086)

## ✨ Características

- Descubrimiento automático de servicios a través de Eureka
- Balanceeo de carga automático
- Reescritura de rutas transparente
- Endpoints de monitoreo disponibles

## 🛠️ Tecnologías

- Spring Boot
- Spring Cloud Gateway
- Eureka Client
