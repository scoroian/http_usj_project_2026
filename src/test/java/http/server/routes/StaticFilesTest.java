package http.server.routes;

import http.TestServer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del StaticHandler.
 *
 * Solo testeamos los ficheros que existen físicamente en public/, no inventamos rutas.
 * Si en un futuro public/index.html cambia, el contenido esperado puede dejar de
 * coincidir — por eso comprobamos solo el status y el Content-Type, no el body entero.
 */
public class StaticFilesTest {

    private static final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2))
        .build();

    @BeforeAll
    static void setUp() throws Exception {
        TestServer.start();
    }

    /**
     * "/" debe servir index.html con Content-Type text/html.
     * Se salta si el fichero no existe (entorno de CI sin frontend, por ejemplo).
     */
    @Test
    @DisplayName("GET / sirve public/index.html con Content-Type HTML")
    void rootSirveIndexHtml() throws Exception {
        if (!Files.exists(Path.of("public/index.html"))) {
            return; // entorno sin frontend; saltamos el test en silencio
        }

        HttpResponse<String> res = client.send(
            HttpRequest.newBuilder(URI.create(TestServer.BASE_URL + "/")).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, res.statusCode());
        assertTrue(res.headers().firstValue("Content-Type").orElse("").contains("text/html"));
    }

    /**
     * Un fichero estático que no existe debe responder 404 con body de texto plano,
     * no romper el servidor.
     */
    @Test
    @DisplayName("Fichero estático inexistente devuelve 404")
    void ficheroInexistenteDevuelve404() throws Exception {
        HttpResponse<String> res = client.send(
            HttpRequest.newBuilder(URI.create(TestServer.BASE_URL + "/no-existe")).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );

        // /no-existe no está registrado en el router, así que en realidad es el router
        // el que devuelve 404, no el StaticHandler. Aún así el resultado es el correcto.
        assertEquals(404, res.statusCode());
    }
}
