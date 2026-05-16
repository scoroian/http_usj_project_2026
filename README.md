# Refugio Felino HTTP Server

Proyecto de Redes y Comunicaciones II — Universidad San Jorge, 2026.

Cliente y servidor HTTP/1.1 implementados desde cero sobre sockets TCP (`java.net.ServerSocket` / `java.net.Socket`), sin ninguna librería de capa de aplicación.

---

## Índice / Table of Contents

- [Español](#español)
- [English](#english)

---

## Español

### Requisitos

- Java 17 o superior
- Maven 3.6 o superior

### Compilar

```bash
mvn compile
```

### Arrancar el servidor

```bash
mvn exec:java
```

Al arrancar, el servidor pregunta el puerto:

```
Puerto [3000]:
```

Pulsa Enter para usar el 3000 o escribe otro y pulsa Enter.

Para activar autenticación por API key:

```bash
API_KEY=mi-clave-secreta mvn exec:java
```

Los logs se guardan en `logs/server.log`. Para desactivarlos:

```bash
LOGGING_DISABLED=true mvn exec:java
```

### Arrancar el cliente

```bash
mvn exec:java
```

El cliente es interactivo: pregunta método, URL, cabeceras opcionales y body (para POST/PUT). Escribe `exit` para salir.

### Cómo probar el proyecto

**1. Tests automáticos (JUnit 5)**

```bash
mvn test
```

Resultado esperado: `Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`.

Cubren CRUD completo, errores HTTP (400/404/405), archivos estáticos, los 7 escenarios de `AuthMiddleware` (key vacía, paths estáticos pasan sin cabecera, paths de API exigen cabecera correcta) y nuestro `HttpClient` propio contra el servidor.

**2. Web en el navegador**

Con el servidor arrancado (`mvn exec:java`), abre:

- `http://localhost:3000/` — página de inicio
- `http://localhost:3000/cats.html` — gestión visual de gatos (CRUD)

**3. API REST con curl**

```bash
# Listar gatos
curl http://localhost:3000/cats

# Obtener uno
curl http://localhost:3000/cats/1

# Crear
curl -X POST http://localhost:3000/cats -H "Content-Type: application/json" -d "{\"name\":\"Neko\",\"breed\":\"Japonés\",\"age\":2}"

# Actualización parcial
curl -X PUT http://localhost:3000/cats/1 -H "Content-Type: application/json" -d "{\"age\":10}"

# Borrar
curl -X DELETE http://localhost:3000/cats/1

# Ruta inexistente → 404
curl http://localhost:3000/no-existe

# Método no permitido → 405
curl -X PATCH http://localhost:3000/cats
```

**4. Cliente CLI propio**

En otra terminal, con el servidor arrancado:

```bash
mvn exec:java
```

Pregunta método, URL, cabeceras y body. Escribe `EXIT` como método para salir.

**5. Autenticación por API key**

Arrancar el servidor con la key activada (PowerShell):

```bash
$env:API_KEY="secreto"; mvn exec:java
```

⚠️ **Importante:** `$env:API_KEY` persiste durante toda la sesión de PowerShell, no solo para el comando. Si después arrancas `mvn exec:java` sin prefijo, la auth seguirá activa. Para desactivarla:

```bash
Remove-Item Env:API_KEY    # o:  $env:API_KEY=""
```

Para confirmar el estado:

```bash
$env:API_KEY               # vacío = auth desactivada
```

Las rutas estáticas (`/`, `/cats.html`, `/style.css`, `/app.js`) siguen siendo públicas para que el navegador pueda cargar la página. Solo `/cats` y `/cats/:id` exigen la cabecera `X-API-Key`.

**Desde la terminal (curl):**

```bash
# Sin cabecera → 401
curl http://localhost:3000/cats

# Con cabecera correcta → 200
curl http://localhost:3000/cats -H "X-API-Key: secreto"

# Con cabecera incorrecta → 401
curl http://localhost:3000/cats -H "X-API-Key: wrong"

# Los archivos estáticos siguen sirviéndose sin cabecera
curl http://localhost:3000/cats.html
```

**Desde el navegador:**

1. Abre `http://localhost:3000/cats.html` (la página carga; los archivos estáticos no piden auth).
2. La lista de gatos muestra "Error al cargar" porque el JS llama a `/cats` y la API sí pide auth.
3. Pulsa **🔑 API key** en la barra de navegación, escribe `secreto` y acepta. La key se guarda en `localStorage`.
4. Recarga la página (F5). La lista de gatos carga correctamente y puedes crear/editar/borrar.
5. Para borrar la key: pulsa 🔑 otra vez y deja el campo vacío.

**6. Logs de peticiones**

Con el servidor arrancado y peticiones llegando, los logs se escriben en:

```bash
type logs\server.log
```

Formato: `[timestamp] METHOD /path -> status`.

**7. Empaquetar y ejecutar el jar**

```bash
mvn package
java -jar server.jar
```

### Estructura del proyecto

```
src/main/java/http/
├── Main.java                      # Punto de entrada, registra rutas
├── Config.java                    # Configuración desde variables de entorno
├── shared/
│   ├── HttpRequest.java           # POJO de petición HTTP
│   ├── HttpResponse.java          # Wrapper de OutputStream con método send()
│   ├── HttpParser.java            # Parsea bytes TCP → HttpRequest / ParsedResponse
│   └── HttpBuilder.java           # Construye strings HTTP crudos
├── client/
│   ├── HttpClient.java            # Librería cliente (TCP → HTTP)
│   └── Cli.java                   # CLI interactivo
└── server/
    ├── HttpServer.java            # ServerSocket + pool de hilos
    ├── Router.java                # Enrutador por método + path con params
    ├── Handler.java               # Interfaz funcional para handlers
    ├── middleware/
    │   ├── Middleware.java        # Interfaz funcional para middlewares
    │   ├── LoggerMiddleware.java  # Logger de peticiones
    │   └── AuthMiddleware.java    # Autenticación por API key
    └── routes/
        ├── StaticHandler.java     # Sirve archivos estáticos desde public/
        ├── Cat.java               # POJO de gato con serialización JSON
        └── CatsHandler.java       # CRUD completo en memoria
public/
├── index.html                     # Página de inicio
├── cats.html                      # Gestión visual de gatos (CRUD)
├── style.css                      # Estilos compartidos
└── app.js                         # Funciones JS compartidas (fetch API + UI)
logs/
└── server.log                     # Generado en tiempo de ejecución
```

### Endpoints disponibles

#### Páginas estáticas

```
GET /
GET /index.html
GET /cats.html
```

#### API REST — Gatos

```
GET    /cats           Lista todos los gatos
GET    /cats/:id       Obtiene un gato por ID
POST   /cats           Crea un gato nuevo
PUT    /cats/:id       Actualiza un gato existente
DELETE /cats/:id       Elimina un gato
```

Formato de un gato:

```json
{
  "id": 1,
  "name": "Hercules",
  "breed": "European",
  "age": 3
}
```

### Errores comunes

| Código | Cuándo ocurre                                     |
| ------ | ------------------------------------------------- |
| 400    | Body JSON inválido o campos obligatorios ausentes |
| 401    | API key incorrecta o no incluida                  |
| 404    | Recurso o ruta no encontrada                      |
| 405    | Método no permitido en esa ruta                   |
| 500    | Error inesperado en el servidor                   |

### Distribución de tareas

| Miembro   | Responsabilidad                                                                                                                                                                               |
| --------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Sebastián | Estructura completa del proyecto, modelos HTTP (`HttpRequest`, `HttpResponse`, `HttpBuilder`, `HttpParser`), servidor TCP (`HttpServer`, `Router`, `StaticHandler`), `Main`, `Config` y tests |
| Samuel    | `Cat.java` (serialización JSON manual), endpoint `GET /cats` y `LoggerMiddleware`                                                                                                             |
| Alberto   | Endpoints `GET /cats/:id` y `DELETE /cats/:id`, y `AuthMiddleware`                                                                                                                            |
| Julio     | Endpoints `POST /cats` y `PUT /cats/:id`, cliente HTTP (`HttpClient`) y CLI interactivo (`Cli`)                                                                                               |

---

## English

### Requirements

- Java 17 or higher
- Maven 3.6 or higher

### Compile

```bash
mvn compile
```

### Start the server

```bash
mvn exec:java
```

When starting, the server prompts for a port:

```
Puerto [3000]:
```

Press Enter to use 3000 or type another port and press Enter.

To enable API key authentication:

```bash
API_KEY=my-secret-key mvn exec:java
```

Logs are written to `logs/server.log`. To disable:

```bash
LOGGING_DISABLED=true mvn exec:java
```

### Start the client

```bash
mvn exec:java
```

Interactive CLI: prompts for method, URL, optional headers, and body (for POST/PUT). Type `exit` to quit.

### How to test the project

**1. Automated tests (JUnit 5)**

```bash
mvn test
```

Expected output: `Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`.

Covers full CRUD, HTTP errors (400/404/405), static files, the 7 `AuthMiddleware` scenarios (empty key, static paths bypass auth, API paths require a valid header) and our own `HttpClient` against the server.

**2. Web in the browser**

With the server running (`mvn exec:java`), open:

- `http://localhost:3000/` — home page
- `http://localhost:3000/cats.html` — visual cat management (CRUD)

**3. REST API with curl**

```bash
# List cats
curl http://localhost:3000/cats

# Get one
curl http://localhost:3000/cats/1

# Create
curl -X POST http://localhost:3000/cats -H "Content-Type: application/json" -d "{\"name\":\"Neko\",\"breed\":\"Japanese Bobtail\",\"age\":2}"

# Partial update
curl -X PUT http://localhost:3000/cats/1 -H "Content-Type: application/json" -d "{\"age\":10}"

# Delete
curl -X DELETE http://localhost:3000/cats/1

# Unknown route → 404
curl http://localhost:3000/no-such-path

# Method not allowed → 405
curl -X PATCH http://localhost:3000/cats
```

**4. Custom CLI client**

In another terminal, with the server running:

```bash
mvn exec:java
```

Prompts for method, URL, headers and body. Type `EXIT` as method to quit.

**5. API key authentication**

Start the server with the key enabled (PowerShell):

```bash
$env:API_KEY="secret"; mvn exec:java
```

⚠️ **Important:** `$env:API_KEY` persists for the entire PowerShell session, not just for the command. If you later start `mvn exec:java` without the prefix, auth will still be enabled. To disable it:

```bash
Remove-Item Env:API_KEY    # or:  $env:API_KEY=""
```

To check the current state:

```bash
$env:API_KEY               # empty = auth disabled
```

Static routes (`/`, `/cats.html`, `/style.css`, `/app.js`) stay public so the browser can load the page. Only `/cats` and `/cats/:id` require the `X-API-Key` header.

**From the terminal (curl):**

```bash
# Without header → 401
curl http://localhost:3000/cats

# With correct header → 200
curl http://localhost:3000/cats -H "X-API-Key: secret"

# With wrong header → 401
curl http://localhost:3000/cats -H "X-API-Key: wrong"

# Static files still served without header
curl http://localhost:3000/cats.html
```

**From the browser:**

1. Open `http://localhost:3000/cats.html` (the page loads; static files don't require auth).
2. The cat list shows "Error al cargar" because the JS calls `/cats`, which does require auth.
3. Click **🔑 API key** in the navigation bar, type `secret` and confirm. The key is stored in `localStorage`.
4. Reload the page (F5). The cat list loads correctly and you can create/edit/delete.
5. To clear the key: click 🔑 again and leave the field empty.

**6. Request logs**

With the server running and requests coming in, logs are written to:

```bash
type logs\server.log
```

Format: `[timestamp] METHOD /path -> status`.

**7. Package and run the jar**

```bash
mvn package
java -jar server.jar
```

### Project structure

```
src/main/java/http/
├── Main.java                      # Entry point, registers all routes
├── Config.java                    # Configuration from env vars
├── shared/
│   ├── HttpRequest.java           # HTTP request POJO
│   ├── HttpResponse.java          # OutputStream wrapper with send()
│   ├── HttpParser.java            # Parses TCP bytes → HttpRequest / ParsedResponse
│   └── HttpBuilder.java           # Builds raw HTTP strings
├── client/
│   ├── HttpClient.java            # Client library (TCP → HTTP)
│   └── Cli.java                   # Interactive CLI
└── server/
    ├── HttpServer.java            # ServerSocket + thread pool
    ├── Router.java                # Method + path routing with params
    ├── Handler.java               # Functional interface for route handlers
    ├── middleware/
    │   ├── Middleware.java        # Functional interface for middlewares
    │   ├── LoggerMiddleware.java  # Request logger
    │   └── AuthMiddleware.java    # API key authentication
    └── routes/
        ├── StaticHandler.java     # Serves static files from public/
        ├── Cat.java               # Cat POJO with JSON serialization
        └── CatsHandler.java       # In-memory CRUD
public/
├── index.html                     # Home page
├── cats.html                      # Visual cat management (CRUD)
├── style.css                      # Shared styles
└── app.js                         # Shared JS (fetch API + UI helpers)
```

### Available endpoints

#### Static pages

```
GET /
GET /index.html
GET /cats.html
```

#### REST API — Cats

```
GET    /cats           List all cats
GET    /cats/:id       Get a cat by ID
POST   /cats           Create a new cat
PUT    /cats/:id       Update an existing cat (partial update supported)
DELETE /cats/:id       Delete a cat
```

Cat format:

```json
{
  "id": 1,
  "name": "Hercules",
  "breed": "European",
  "age": 3
}
```

### HTTP status codes

| Code | When it happens                              |
| ---- | -------------------------------------------- |
| 400  | Invalid JSON body or missing required fields |
| 401  | Wrong or missing API key                     |
| 404  | Resource or route not found                  |
| 405  | Method not allowed on that path              |
| 500  | Unexpected server error                      |

### Task distribution

| Member    | Responsibility                                                                                                                                                                       |
| --------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Sebastián | Full project scaffolding, HTTP models (`HttpRequest`, `HttpResponse`, `HttpBuilder`, `HttpParser`), TCP server (`HttpServer`, `Router`, `StaticHandler`), `Main`, `Config` and tests |
| Samuel    | `Cat.java` (manual JSON serialization), `GET /cats` endpoint and `LoggerMiddleware`                                                                                                  |
| Alberto   | `GET /cats/:id` and `DELETE /cats/:id` endpoints, and `AuthMiddleware`                                                                                                               |
| [Name 4]  | `POST /cats` and `PUT /cats/:id` endpoints, HTTP client (`HttpClient`) and interactive CLI (`Cli`)                                                                                   |

### How it works

Every connection comes in as raw bytes on a `ServerSocket`. The server hands each socket to a thread from a fixed pool of 20, so requests are truly concurrent. `HttpParser` reads byte by byte until it finds the `\r\n\r\n` header terminator, then reads exactly `Content-Length` bytes for the body. The result is a plain `HttpRequest` object that the router matches against registered patterns using regex. Handlers receive `HttpRequest` and `HttpResponse`, call `res.send()`, and the raw HTTP string goes back over the socket.

No frameworks. No HTTP libraries. Just sockets, threads, and string parsing.
