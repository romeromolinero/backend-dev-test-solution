# API de productos similares

Solución para la [prueba técnica de backend](https://github.com/dalogax/backendDevTest). La
aplicación expone el endpoint solicitado en el puerto `5000`, consulta el catálogo simulado y
devuelve los productos similares respetando su orden original.

## Qué hace la aplicación

Al recibir `GET /product/{productId}/similar`:

1. Solicita los identificadores similares a `GET /product/{productId}/similarids`.
2. Consulta en paralelo el detalle de cada producto.
3. Elimina identificadores duplicados sin alterar su posición.
4. Devuelve los detalles en el mismo orden de relevancia recibido.

La implementación usa Spring WebFlux y `WebClient`, por lo que no bloquea un hilo mientras espera
las respuestas del catálogo.

## Inicio rápido sin Docker

Necesitas Java 17 o superior. El wrapper incluido descarga Maven automáticamente.

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

El catálogo simulado debe estar disponible en `http://localhost:3001`. Después puedes consultar:

```bash
curl http://localhost:5000/product/1/similar
```

También puedes abrir <http://localhost:5000>. Esa ruta muestra una breve guía del servicio y
funciona aunque el catálogo simulado no esté arrancado.

## Respuesta de ejemplo

```json
[
  { "id": "2", "name": "Dress", "price": 19.99, "availability": true },
  { "id": "3", "name": "Blazer", "price": 29.99, "availability": false },
  { "id": "4", "name": "Boots", "price": 39.99, "availability": true }
]
```

Los nombres de los campos se mantienen como los define el contrato original.

## Arranque con Docker

El repositorio conserva la configuración oficial para ejecutar la aplicación, el catálogo simulado
y las herramientas de carga:

```bash
docker compose up --build -d simulado yourapp influxdb grafana
docker compose run --rm k6 run scripts/test.js
```

Servicios disponibles:

- API: <http://localhost:5000/product/1/similar>
- Guía del servicio: <http://localhost:5000>
- Salud: <http://localhost:5000/actuator/health>
- Métricas: <http://localhost:5000/actuator/metrics>
- Grafana: <http://localhost:3000/d/Le2Ku9NMk/k6-performance-test>

## Diseño

```text
Controlador HTTP
      |
      v
FindSimilarProductsService -----> puerto ProductCatalog
                                      |
                                      v
                         adaptador WebClient / API simulada
```

Las consultas de detalle se ejecutan simultáneamente mediante un cliente Reactor Netty con pool de
conexiones. `flatMapSequential` conserva el orden original sin renunciar al paralelismo. La
concurrencia está limitada, se ignoran los identificadores repetidos y ninguna petición HTTP bloquea
un hilo mientras espera entrada o salida.

## Errores y tiempos de espera

El tiempo máximo de respuesta del catálogo es de dos segundos. Esto evita que las respuestas
simuladas de 5 y 50 segundos agoten los recursos durante la prueba de 200 usuarios virtuales.

| Situación | Respuesta pública |
| --- | --- |
| Datos obtenidos correctamente | `200` con los productos ordenados |
| Un producto referenciado no existe | `404 PRODUCT_NOT_FOUND` |
| El catálogo supera el tiempo máximo | `504 CATALOG_TIMEOUT` |
| El catálogo falla o no está disponible | `502 CATALOG_UNAVAILABLE` |
| El identificador de la ruta no es seguro | `400 INVALID_PRODUCT_ID` |

No se realizan reintentos automáticos. Reintentar contra una dependencia saturada multiplicaría la
carga; el cliente o un proxy de entrada pueden repetir un `GET` de acuerdo con su propio presupuesto
de tiempo.

Si varias consultas fallan al mismo tiempo, Reactor puede agrupar sus excepciones. La aplicación
recupera el error de catálogo original para mantener estable la respuesta HTTP documentada.

## Configuración

Todos los valores tienen un valor local predeterminado y se pueden sobrescribir mediante variables
de entorno:

| Variable | Valor predeterminado |
| --- | --- |
| `PRODUCT_CATALOG_BASE_URL` | `http://localhost:3001` |
| `PRODUCT_CATALOG_CONNECT_TIMEOUT` | `500ms` |
| `PRODUCT_CATALOG_RESPONSE_TIMEOUT` | `2s` |
| `PRODUCT_CATALOG_MAX_CONNECTIONS` | `500` |
| `PRODUCT_CATALOG_PENDING_ACQUIRE_MAX_COUNT` | `1000` |
| `PRODUCT_CATALOG_PENDING_ACQUIRE_TIMEOUT` | `1s` |

## Verificación

```bash
./mvnw verify
```

Actualmente hay 13 pruebas automatizadas. Cubren la validación de identificadores, respuestas
vacías, paralelismo, conservación del orden, decodificación JSON, errores 404/500 del catálogo y
timeouts individuales y concurrentes. GitHub Actions ejecuta la misma verificación en cada `push` y
`pull request`.

## Estructura del proyecto

- `domain`: valores validados, modelo de respuesta y errores del dominio.
- `application`: caso de uso y puerto de salida hacia el catálogo.
- `infrastructure/client`: adaptador WebClient, pool y configuración externa.
- `infrastructure/web`: controlador REST y traducción estable de errores HTTP.
- `shared`: mocks, prueba k6 y configuración de Grafana suministrados con el enunciado.

Los contratos OpenAPI originales se conservan en [existingApis.yaml](existingApis.yaml) y
[similarProducts.yaml](similarProducts.yaml).
