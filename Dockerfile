# Usar imagen base de Java 17
FROM openjdk:17-jdk-slim

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el archivo JAR del microservicio
COPY target/gateway-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto 8761
EXPOSE 8086

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]