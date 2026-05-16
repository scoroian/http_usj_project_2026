package http.server.middleware;

import http.shared.HttpRequest;
import http.shared.HttpResponse;
import http.Config;

/**
 * Middleware de autenticación por API key.
 *
 * ESTADO: stub. De momento deja pasar todas las peticiones sin comprobar nada.
 */
public class AuthMiddleware implements Middleware {

    private final String apiKey;

    /** Constructor por defecto: lee la API key de Config.API_KEY (variable de entorno). */
    public AuthMiddleware() {
        this(Config.API_KEY);
    }

    /**
     * Constructor explícito.
     * Permite pasar una API key concreta (lo usan los tests para no depender de la
     * variable de entorno API_KEY, que solo se lee al arrancar la JVM).
     */
    public AuthMiddleware(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void apply(HttpRequest req, HttpResponse res, Runnable next) {
        if (apiKey.isEmpty()) {
            next.run();
            return;
        }

        // Solo protegemos la API REST: /cats y /cats/:id.
        // Los archivos estáticos (HTML/CSS/JS) son públicos para que el navegador
        // pueda cargar la página que luego envía la cabecera X-API-Key.
        // Hay que excluir /cats.html porque empieza por "/cats" pero es estático.
        String path = req.path.contains("?") ? req.path.substring(0, req.path.indexOf('?')) : req.path;
        boolean isApiPath = path.equals("/cats") || path.startsWith("/cats/");
        if (!isApiPath) {
            next.run();
            return;
        }

        String provided = req.headers.get("x-api-key");
        if (!apiKey.equals(provided)) {
            res.json(401, "{\"error\":\"Invalid or missing API key\"}");
            return;
        }

        next.run();
    }
}
