package http.server.middleware;

import http.shared.HttpRequest;
import http.shared.HttpResponse;

/**
 * Middleware de logging.
 *
 * ESTADO: stub. De momento solo deja pasar la petición (next.run()) para que
 * el servidor funcione mientras Samuel implementa la lógica real.
 */
public class LoggerMiddleware implements Middleware {

    /**
     * TODO: Samuel
     * Registrar la petición usando res.setOnSend() para capturar el status code
     * y escribir la línea en consola + fichero logs/server.log.
     * Formato: [timestamp] METHOD /path -> status
     */
    @Override
    public void apply(HttpRequest req, HttpResponse res, Runnable next) {
        next.run();
    }
}
