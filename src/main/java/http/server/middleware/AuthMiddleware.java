package http.server.middleware;

import http.shared.HttpRequest;
import http.shared.HttpResponse;

/**
 * Middleware de autenticación por API key.
 *
 * ESTADO: stub. De momento deja pasar todas las peticiones sin comprobar nada.
 */
public class AuthMiddleware implements Middleware {

    /**
     * TODO: Alberto
     * Si Config.API_KEY está vacío → next.run() y salir.
     * Leer cabecera "x-api-key". Si no coincide → 401 y cortar la cadena.
     * Si coincide → next.run().
     */
    @Override
    public void apply(HttpRequest req, HttpResponse res, Runnable next) {
        next.run();
    }
}
