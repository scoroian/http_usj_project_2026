package http.server.routes;

import http.TestServer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests end-to-end de la API REST de gatos.
 *
 * Estos tests arrancan el HttpServer del proyecto en un hilo daemon y hacen peticiones
 * reales por TCP usando java.net.http.HttpClient (el cliente nativo de Java, NO el del
 * proyecto). Usar un cliente externo nos garantiza que estamos midiendo el comportamiento
 * real del servidor y no la combinación servidor + cliente propio.
 *
 * Los tests están ordenados con @Order porque comparten estado (el almacén de gatos
 * persiste entre tests). Los IDs creados en tests anteriores se reutilizan en los
 * siguientes para no acoplar cada test a un número mágico.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CatsApiTest {

    private static final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2))
        .build();

    @BeforeAll
    static void setUp() throws Exception {
        TestServer.start();
    }

    /**
     * Comprueba que la lista inicial trae los 2 gatos precargados en el constructor
     * de CatsHandler (Hercules y Luna).
     */
    @Test
    @Order(1)
    @DisplayName("GET /cats devuelve 200 y la lista de gatos en JSON")
    void getAll_devuelveListaDeGatos() throws Exception {
        HttpResponse<String> res = get("/cats");

        assertEquals(200, res.statusCode());
        assertTrue(res.body().startsWith("["), "El body debería ser un array JSON");
        assertTrue(res.body().contains("\"name\":\"Hercules\""), "Falta Hercules en la respuesta");
        assertTrue(res.body().contains("\"name\":\"Luna\""), "Falta Luna en la respuesta");
    }

    /**
     * GET /cats/1 → Hercules. El gato con ID 1 lo precarga el constructor.
     */
    @Test
    @Order(2)
    @DisplayName("GET /cats/:id devuelve el gato concreto")
    void getOne_devuelveGato() throws Exception {
        HttpResponse<String> res = get("/cats/1");

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"id\":1"));
        assertTrue(res.body().contains("\"name\":\"Hercules\""));
    }

    /**
     * Un ID inexistente debe devolver 404 con el mensaje de error en JSON.
     */
    @Test
    @Order(3)
    @DisplayName("GET /cats/:id devuelve 404 cuando el gato no existe")
    void getOne_inexistente_devuelve404() throws Exception {
        HttpResponse<String> res = get("/cats/9999");

        assertEquals(404, res.statusCode());
        assertTrue(res.body().contains("Cat not found"));
    }

    /**
     * POST con body válido crea un gato nuevo y devuelve 201 con el gato (id asignado).
     */
    @Test
    @Order(4)
    @DisplayName("POST /cats crea un gato nuevo y responde 201")
    void post_creaGato() throws Exception {
        String body = "{\"name\":\"Neko\",\"breed\":\"Japonés\",\"age\":2}";
        HttpResponse<String> res = post("/cats", body);

        assertEquals(201, res.statusCode());
        assertTrue(res.body().contains("\"name\":\"Neko\""));
        assertTrue(res.body().contains("\"breed\":\"Japonés\""));
        assertTrue(res.body().contains("\"age\":2"));
        // El servidor asigna un id; debería ser >= 3 porque AtomicInteger arranca en 3
        assertTrue(res.body().matches(".*\"id\":[3-9]\\d*.*"), "El id asignado debería ser >= 3");
    }

    /**
     * POST sin alguno de los campos obligatorios → 400.
     */
    @Test
    @Order(5)
    @DisplayName("POST /cats con campos faltantes responde 400")
    void post_sinCamposObligatorios_devuelve400() throws Exception {
        String body = "{\"name\":\"X\"}"; // falta breed y age
        HttpResponse<String> res = post("/cats", body);

        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("required"));
    }

    /**
     * PUT parcial: solo actualiza el campo enviado, los demás se conservan.
     */
    @Test
    @Order(6)
    @DisplayName("PUT /cats/:id hace actualización parcial")
    void put_actualizaParcial() throws Exception {
        // Antes: Hercules, European, age 3
        String body = "{\"age\":10}";
        HttpResponse<String> res = put("/cats/1", body);

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"age\":10"), "El age debería estar actualizado");
        assertTrue(res.body().contains("\"name\":\"Hercules\""), "El name debería conservarse");
        assertTrue(res.body().contains("\"breed\":\"European\""), "El breed debería conservarse");
    }

    /**
     * PUT sobre un ID inexistente → 404 (no se crea uno nuevo).
     */
    @Test
    @Order(7)
    @DisplayName("PUT /cats/:id devuelve 404 cuando el gato no existe")
    void put_inexistente_devuelve404() throws Exception {
        HttpResponse<String> res = put("/cats/9999", "{\"age\":5}");

        assertEquals(404, res.statusCode());
    }

    /**
     * DELETE de un gato existente → 204 sin body, y luego GET → 404.
     */
    @Test
    @Order(8)
    @DisplayName("DELETE /cats/:id elimina el gato y responde 204")
    void delete_eliminaGato() throws Exception {
        HttpResponse<String> deleted = delete("/cats/2");

        assertEquals(204, deleted.statusCode());
        assertTrue(deleted.body().isEmpty(), "204 no debe tener body");

        // Confirmamos que ya no existe
        HttpResponse<String> after = get("/cats/2");
        assertEquals(404, after.statusCode());
    }

    /**
     * DELETE sobre un ID que no existe → 404.
     */
    @Test
    @Order(9)
    @DisplayName("DELETE /cats/:id devuelve 404 cuando el gato no existe")
    void delete_inexistente_devuelve404() throws Exception {
        HttpResponse<String> res = delete("/cats/9999");

        assertEquals(404, res.statusCode());
    }

    /**
     * Una ruta que no existe debe devolver 404 con error JSON, no un 500 ni hang.
     */
    @Test
    @Order(10)
    @DisplayName("Ruta desconocida devuelve 404")
    void rutaDesconocida_devuelve404() throws Exception {
        HttpResponse<String> res = get("/no-existe");

        assertEquals(404, res.statusCode());
        assertTrue(res.body().contains("Not found"));
    }

    /**
     * Una ruta válida con un método no registrado debe devolver 405, no 404.
     * Esto distingue nuestro router de los que simplemente devuelven 404 para todo.
     */
    @Test
    @Order(11)
    @DisplayName("Método no permitido devuelve 405")
    void metodoNoPermitido_devuelve405() throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(TestServer.BASE_URL + "/cats"))
            .method("PATCH", HttpRequest.BodyPublishers.noBody())
            .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, res.statusCode());
        assertTrue(res.body().contains("Method not allowed"));
    }

    // ---- helpers ----

    private static HttpResponse<String> get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(TestServer.BASE_URL + path))
            .GET()
            .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(String path, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(TestServer.BASE_URL + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> put(String path, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(TestServer.BASE_URL + path))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> delete(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(TestServer.BASE_URL + path))
            .DELETE()
            .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
