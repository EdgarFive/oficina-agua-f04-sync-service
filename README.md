# Oficina de Agua - Feature 04

## Microservicio de replicación asíncrona

Microservicio desarrollado en Java 17 y Spring Boot para replicar información desde MariaDB hacia DynamoDB.

Forma parte de la Fase 2 del proyecto Oficina de Agua.

## Funcionalidades

- Sincronización asíncrona mediante jobs.
- Replicación de contadores y lecturas.
- Comparación de registros (diff).
- Inserción y actualización de registros (upsert).
- No elimina registros existentes en DynamoDB.
- Persistencia del estado de los jobs.
- Manejo de errores de sincronización.

## Tecnologías

- Java 17
- Spring Boot
- MariaDB
- DynamoDB Local
- Docker y Docker Compose
- Maven
- OpenAPI

## Endpoints

### Iniciar sincronización

POST /api/sync/resumen

Respuesta HTTP 202 Accepted:

{
  "jobId": "identificador-del-job",
  "estado": "PENDIENTE"
}

### Consultar estado

GET /api/sync/jobs/{jobId}

Estados posibles:

- PENDIENTE
- EN_PROGRESO
- COMPLETADO
- FALLIDO

Si el job no existe, devuelve HTTP 404.

## Ejecutar mediante Docker Compose

Requisitos:

- Docker Desktop
- Docker Compose

Crear un archivo local `.env.docker` con estas variables:

F04_DOCKER_DB_PASSWORD=colocar_clave_segura
F04_DOCKER_ROOT_PASSWORD=colocar_otra_clave_segura

Las contraseñas deben ser diferentes y no deben publicarse en GitHub.

Ejecutar:

    docker compose --env-file .env.docker up -d --build

Consultar servicios:

    docker compose --env-file .env.docker ps

Consultar registros:

    docker compose --env-file .env.docker logs --tail=80 sync-service

Detener servicios:

    docker compose --env-file .env.docker down

El comando anterior conserva los volúmenes de datos.

## Puertos locales

- API Spring Boot: http://localhost:8081
- DynamoDB Local: localhost:8001
- MariaDB: accesible internamente desde Docker

## Base de datos

El entorno Docker incorpora una MariaDB independiente con
registros de ejemplo definidos en docker/mariadb/init.sql.

Los datos iniciales se cargan únicamente cuando se crea por
primera vez el volumen de MariaDB.

Este entorno no modifica la base de datos del sistema principal.

## Variables de conexión

El servicio utiliza las siguientes variables:

- F04_DB_URL
- F04_DB_USER
- F04_DB_PASSWORD
- F04_DYNAMODB_ENDPOINT

Docker Compose configura las direcciones internas de los servicios.

## Prueba de funcionamiento

Iniciar una sincronización:

    curl -X POST http://localhost:8081/api/sync/resumen

Copiar el jobId recibido y consultar:

    curl http://localhost:8081/api/sync/jobs/ID_DEL_JOB

Comprobar que el estado sea COMPLETADO.

## Pruebas automatizadas

Ejecutar:

    ./mvnw -Dtest=AsyncSyncProcessorTest test

## Contrato API

El contrato OpenAPI se encuentra en:

openapi/sync-api.yaml

## Consideraciones

- La sincronización se ejecuta en segundo plano.
- Solo realiza inserciones y actualizaciones.
- Los jobs se almacenan en DynamoDB.
- No se incluyen credenciales reales en el repositorio.
- El despliegue en AWS se abordará por separado.
