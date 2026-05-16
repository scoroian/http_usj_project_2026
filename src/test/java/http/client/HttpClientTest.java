package http.client;

import http.TestServer;
import http.shared.HttpParser.ParsedResponse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del cliente HTTP propio del proyecto.
 *
 * En CatsApiTest usamos java.net.http.HttpClient (el cliente nativo de Java) para que
 * los tests del servidor no dependan de nuestro cliente. Aquí hacemos lo contrario:
 * apuntamos nuestro HttpClient contra el mismo servidor de tests para verificar que
 * sabe hablar HTTP/1.1 correctamente.
 */
public class HttpClientTest {

    @BeforeAll
    static void setUp() throws Exception {
        TestServer.start();
    }

    /**
     * Nuestro HttpClient envía un GET, recibe la respuesta del servidor (también nuestro)
     * y la parsea sin perder ningún campo.
     */
    @Test
    @DisplayName("HttpClient envía un GET y parsea la respuesta")
    void send_get_parseaRespuesta() throws Exception {
        ParsedResponse res = HttpClient.send("GET", TestServer.BASE_URL + "/cats", null, "");

        assertNotNull(res, "La respuesta no debería ser null");
        assertEquals(200, res.status());
        assertEquals("OK", res.statusText());
        assertTrue(res.body().startsWith("["), "El body debería ser un array JSON");
        assertTrue(res.headers().containsKey("content-length"), "Falta cabecera Content-Length");
    }

    /**
     * Un POST con body JSON debería crear el recurso. Aprovechamos para verificar que
     * HttpClient añade Content-Length correctamente (si no, el servidor no leería el body).
     */
    @Test
    @DisplayName("HttpClient envía un POST con body y recibe 201")
    void send_post_creaRecurso() throws Exception {
        String body = "{\"name\":\"ClientCat\",\"breed\":\"Test\",\"age\":1}";
        ParsedResponse res = HttpClient.send("POST", TestServer.BASE_URL + "/cats", null, body);

        assertNotNull(res);
        assertEquals(201, res.status());
        assertTrue(res.body().contains("\"name\":\"ClientCat\""));
    }
}
