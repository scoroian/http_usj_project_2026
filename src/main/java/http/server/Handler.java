package http.server;

import http.shared.HttpRequest;
import http.shared.HttpResponse;

/**
 * Interfaz funcional que representa un handler de ruta.
 * Cada endpoint del servidor implementa esta interfaz (o se pasa como lambda/method reference).
 *
 * Es equivalente al concepto de "route handler" en Express (Node.js) o @RequestMapping en Spring.
 * Al ser @FunctionalInterface, se puede usar con lambdas: router.get("/", (req, res) -> { ... })
 */
@FunctionalInterface
public interface Handler {

    /**
     * Procesa la petición HTTP y escribe la respuesta.
     *
     * @param req petición ya parseada con método, ruta, cabeceras, body y params
     * @param res wrapper del socket de salida; llamar a res.send() o res.json() envía la respuesta
     */
    void handle(HttpRequest req, HttpResponse res);
}
