package http;

import http.server.HttpServer;
import http.server.Router;
import http.server.middleware.Middleware;
import http.server.routes.CatsHandler;
import http.server.routes.StaticHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Helper compartido entre las clases de test.
 * Arranca un HttpServer en un puerto fijo dentro de un hilo daemon, con todas las rutas
 * registradas y SIN middlewares (ni Logger ni Auth) para que los tests no dependan
 * de variables de entorno ni escriban en disco.
 *
 * El servidor se arranca una sola vez por JVM. Las llamadas posteriores a start()
 * son no-op (idempotente).
 */
public class TestServer {

    /**
     * Puerto fijo para los tests. Si está ocupado el arranque fallará y hay que cambiarlo.
     * No usamos 0 (puerto aleatorio) porque HttpServer.start() bloquea y no expone el puerto real.
     */
    public static final int PORT = 39001;
    public static final String HOST = "127.0.0.1";
    public static final String BASE_URL = "http://" + HOST + ":" + PORT;

    private static boolean started = false;

    /**
     * Arranca el servidor en background si aún no estaba arrancado.
     * Espera hasta que el puerto acepta conexiones antes de devolver el control.
     */
    public static synchronized void start() throws Exception {
        if (started) return;

        Router router = buildRouter();
        // Sin middlewares: queremos probar solo la cadena handler + router en estos tests.
        // AuthMiddleware se prueba aparte instanciándolo directamente.
        List<Middleware> middlewares = List.of();

        HttpServer server = new HttpServer(router, middlewares);

        Thread serverThread = new Thread(() -> {
            try {
                server.start(PORT, HOST);
            } catch (IOException e) {
                // El servidor se cierra al terminar la JVM; ignoramos la excepción
            }
        }, "test-http-server");
        serverThread.setDaemon(true);
        serverThread.start();

        waitForPortReady();
        started = true;
    }

    /**
     * Devuelve un Router con todas las rutas reales del proyecto, igual que en Main.
     * Se expone como método estático para que los tests que necesiten un router limpio
     * (por ejemplo, los tests de middleware en aislamiento) puedan reutilizarlo.
     */
    public static Router buildRouter() {
        Router router = new Router();

        // Páginas y assets estáticos
        router.get("/",           StaticHandler::serve);
        router.get("/index.html", StaticHandler::serve);
        router.get("/cats.html",  StaticHandler::serve);
        router.get("/style.css",  StaticHandler::serve);
        router.get("/app.js",     StaticHandler::serve);

        // API REST de gatos. Cada test recibe un CatsHandler nuevo a través del wrapper,
        // pero aquí compartimos uno porque el servidor es único para toda la suite.
        CatsHandler cats = new CatsHandler();
        router.get("/cats",         cats::getAll);
        router.get("/cats/:id",     cats::getOne);
        router.post("/cats",        cats::create);
        router.put("/cats/:id",     cats::update);
        router.delete("/cats/:id",  cats::remove);

        return router;
    }

    /**
     * Hace polling al puerto durante un máximo de 3 segundos hasta que la conexión TCP
     * tiene éxito. Sin esto, los primeros tests pueden ejecutarse antes de que el
     * ServerSocket esté escuchando.
     */
    private static void waitForPortReady() throws Exception {
        for (int i = 0; i < 30; i++) {
            try (Socket s = new Socket(HOST, PORT)) {
                return;
            } catch (IOException e) {
                Thread.sleep(100);
            }
        }
        throw new IllegalStateException("El servidor de tests no arrancó en 3 segundos");
    }
}
