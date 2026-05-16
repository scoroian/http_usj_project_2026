package http.server.middleware;

import http.shared.HttpRequest;
import http.shared.HttpResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del AuthMiddleware en aislamiento.
 *
 * No arrancamos un servidor: instanciamos el middleware directamente y le pasamos
 * peticiones simuladas. Usamos el constructor con API key explícita para no depender
 * de la variable de entorno API_KEY (que solo se lee al arrancar la JVM).
 *
 * Cubrimos los tres escenarios del middleware:
 *  - API key vacía → todo pasa.
 *  - API key configurada + ruta de archivo estático → pasa sin necesidad de cabecera.
 *  - API key configurada + ruta de API → exige cabecera correcta.
 */
public class AuthMiddlewareTest {

    /**
     * Sin API key configurada, el middleware debe dejar pasar todo.
     */
    @Test
    @DisplayName("Sin API_KEY: deja pasar cualquier petición")
    void sinApiKey_dejaPasar() {
        AuthMiddleware mw = new AuthMiddleware("");

        AtomicBoolean siguiente = new AtomicBoolean(false);
        mw.apply(reqGet("/cats"), nuevaRes(), () -> siguiente.set(true));

        assertTrue(siguiente.get(), "next.run() debería haberse llamado");
    }

    /**
     * Con API key activa, los archivos estáticos son públicos.
     * Si bloqueásemos /index.html, el navegador no podría cargar el JS que envía la cabecera.
     */
    @Test
    @DisplayName("Con API_KEY: /index.html pasa sin cabecera (es estático)")
    void conApiKey_pathEstatico_pasaSinCabecera() {
        AuthMiddleware mw = new AuthMiddleware("secreto");

        AtomicBoolean siguiente = new AtomicBoolean(false);
        mw.apply(reqGet("/index.html"), nuevaRes(), () -> siguiente.set(true));

        assertTrue(siguiente.get());
    }

    /**
     * /cats.html empieza por "/cats" pero NO es una ruta de API.
     * Esto es lo que cazaba el bug inicial: el navegador no podía abrir la página.
     */
    @Test
    @DisplayName("Con API_KEY: /cats.html pasa sin cabecera (a pesar de empezar por /cats)")
    void conApiKey_catsHtml_pasaSinCabecera() {
        AuthMiddleware mw = new AuthMiddleware("secreto");

        AtomicBoolean siguiente = new AtomicBoolean(false);
        mw.apply(reqGet("/cats.html"), nuevaRes(), () -> siguiente.set(true));

        assertTrue(siguiente.get());
    }

    /**
     * /cats sin cabecera X-API-Key con auth activa → 401 y se corta la cadena.
     */
    @Test
    @DisplayName("Con API_KEY: /cats sin cabecera devuelve 401")
    void conApiKey_apiSinCabecera_devuelve401() {
        AuthMiddleware mw = new AuthMiddleware("secreto");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AtomicBoolean siguiente = new AtomicBoolean(false);
        mw.apply(reqGet("/cats"), new HttpResponse(out), () -> siguiente.set(true));

        assertFalse(siguiente.get(), "next.run() NO debería haberse llamado");
        String written = out.toString(StandardCharsets.UTF_8);
        assertTrue(written.startsWith("HTTP/1.1 401"), "Debería responder 401");
        assertTrue(written.contains("Invalid or missing API key"));
    }

    /**
     * /cats con cabecera incorrecta → también 401.
     */
    @Test
    @DisplayName("Con API_KEY: cabecera incorrecta devuelve 401")
    void conApiKey_cabeceraIncorrecta_devuelve401() {
        AuthMiddleware mw = new AuthMiddleware("secreto");
        HttpRequest req = reqGet("/cats");
        req.headers.put("x-api-key", "wrong");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AtomicBoolean siguiente = new AtomicBoolean(false);
        mw.apply(req, new HttpResponse(out), () -> siguiente.set(true));

        assertFalse(siguiente.get());
        assertTrue(out.toString(StandardCharsets.UTF_8).startsWith("HTTP/1.1 401"));
    }

    /**
     * /cats con cabecera correcta → pasa.
     */
    @Test
    @DisplayName("Con API_KEY: cabecera correcta deja pasar")
    void conApiKey_cabeceraCorrecta_pasa() {
        AuthMiddleware mw = new AuthMiddleware("secreto");
        HttpRequest req = reqGet("/cats");
        req.headers.put("x-api-key", "secreto");

        AtomicBoolean siguiente = new AtomicBoolean(false);
        mw.apply(req, nuevaRes(), () -> siguiente.set(true));

        assertTrue(siguiente.get());
    }

    /**
     * Las subrutas de la API (/cats/1) también están protegidas.
     */
    @Test
    @DisplayName("Con API_KEY: /cats/1 sin cabecera devuelve 401")
    void conApiKey_subRecursoSinCabecera_devuelve401() {
        AuthMiddleware mw = new AuthMiddleware("secreto");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mw.apply(reqGet("/cats/1"), new HttpResponse(out), () -> {});

        assertTrue(out.toString(StandardCharsets.UTF_8).startsWith("HTTP/1.1 401"));
    }

    // ---- helpers ----

    private static HttpRequest reqGet(String path) {
        HttpRequest req = new HttpRequest();
        req.method = "GET";
        req.path = path;
        req.headers = new HashMap<>();
        return req;
    }

    private static HttpResponse nuevaRes() {
        return new HttpResponse(new ByteArrayOutputStream());
    }
}
