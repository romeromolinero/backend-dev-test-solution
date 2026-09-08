# Similar Products API

A production-minded solution to the [backend development technical test](https://github.com/dalogax/backendDevTest).
It exposes the agreed composition endpoint on port `5000`, resolving the ordered product IDs from
the provided catalog and fetching their details concurrently.

## Quick start

Requirements: Java 17 or newer. Maven is downloaded automatically by the included wrapper.

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The catalog mock must be reachable at `http://localhost:3001` (the default from the supplied
contract). Then call:

```bash
curl http://localhost:5000/product/1/similar
```

Opening <http://localhost:5000> now returns a small discovery response with the example endpoint
and health-check URL. This root response works even when the catalog mock is not running; product
requests still need the mock on port `3001`.

The response preserves the similarity order:

```json
[
  { "id": "2", "name": "Dress", "price": 19.99, "availability": true },
  { "id": "3", "name": "Blazer", "price": 29.99, "availability": false },
  { "id": "4", "name": "Boots", "price": 39.99, "availability": true }
]
```

## Run everything with Docker

```bash
docker compose up --build -d simulado yourapp influxdb grafana
docker compose run --rm k6 run scripts/test.js
```

- API: <http://localhost:5000/product/1/similar>
- Service guide: <http://localhost:5000>
- Health: <http://localhost:5000/actuator/health>
- Metrics: <http://localhost:5000/actuator/metrics>
- Grafana: <http://localhost:3000/d/Le2Ku9NMk/k6-performance-test>

## Design

```text
HTTP controller
      |
      v
FindSimilarProductsService -----> ProductCatalog port
                                      |
                                      v
                           WebClient adapter / mock API
```

The application is fully reactive. Detail requests are subscribed concurrently through a pooled
Reactor Netty client; `flatMapSequential` keeps the source ordering while avoiding sequential
network latency. Concurrency is bounded, duplicate IDs are ignored, and no request thread blocks
waiting for I/O.

The default response timeout is two seconds. It prevents the supplied 5- and 50-second mock
responses from exhausting resources under the 200-virtual-user workload. The behavior is explicit:

| Situation | Response |
| --- | --- |
| Requested data is returned | `200` with ordered product details |
| A referenced product does not exist | `404 PRODUCT_NOT_FOUND` |
| The catalog exceeds the deadline | `504 CATALOG_TIMEOUT` |
| The catalog fails or cannot be reached | `502 CATALOG_UNAVAILABLE` |
| The path identifier is unsafe | `400 INVALID_PRODUCT_ID` |

Retries are intentionally omitted. Retrying a saturated dependency multiplies load, while a GET
caller or edge proxy can safely retry according to its own budget. Timeouts and pool limits are
externalized instead.

## Configuration

Every value has a local default and can be overridden with an environment variable:

| Variable | Default |
| --- | --- |
| `PRODUCT_CATALOG_BASE_URL` | `http://localhost:3001` |
| `PRODUCT_CATALOG_CONNECT_TIMEOUT` | `500ms` |
| `PRODUCT_CATALOG_RESPONSE_TIMEOUT` | `2s` |
| `PRODUCT_CATALOG_MAX_CONNECTIONS` | `500` |
| `PRODUCT_CATALOG_PENDING_ACQUIRE_MAX_COUNT` | `1000` |
| `PRODUCT_CATALOG_PENDING_ACQUIRE_TIMEOUT` | `1s` |

## Verification

```bash
./mvnw verify
```

The tests cover input validation, empty responses, parallel detail subscription, deterministic
ordering, numeric IDs from the supplied mock, JSON decoding, downstream 404/500 handling, and both
single and concurrent response deadlines. GitHub Actions runs the same verification on every push
and pull request.

## Project layout

- `domain`: validated values, response model, and domain-specific failures.
- `application`: use case and outbound catalog port.
- `infrastructure/client`: pooled WebClient adapter and external configuration.
- `infrastructure/web`: REST controller and stable error mapping.
- `shared`: unchanged mocks, k6 workload, and Grafana provisioning supplied by the challenge.

The original OpenAPI contracts remain available in [existingApis.yaml](existingApis.yaml) and
[similarProducts.yaml](similarProducts.yaml).
