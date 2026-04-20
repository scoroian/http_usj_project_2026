package http.server.middleware;

import http.shared.HttpRequest;
import http.shared.HttpResponse;

/**
 * Interfaz funcional que representa un middleware HTTP.
 * Un middleware es un paso intermedio que se ejecuta antes del handler de ruta.
 * Puede:
 *  - Leer o modificar la petición (req).
 *  - Interceptar la respuesta (res).
 *  - Llamar a next.run() para pasar el control al siguiente middleware o al handler.
 *  - NO llamar a next.run() para cortar la cadena (p.ej. si la auth falla).
 *
 * Equivalente a app.use() en Express o a los filtros de Spring Security.
 */
@FunctionalInterface
public interface Middleware {

    /**
     * Ejecuta la lógica del middleware.
     *
     * @param req  petición HTTP entrante
     * @param res  respuesta HTTP saliente (para cortar la cadena si hace falta)
     * @param next callback que pasa el control al siguiente middleware o al handler
     */
    void apply(HttpRequest req, HttpResponse res, Runnable next);
}
