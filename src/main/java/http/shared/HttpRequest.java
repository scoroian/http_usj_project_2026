package http.shared;

import java.util.HashMap;
import java.util.Map;

/**
 * POJO (objeto plano de datos) que representa una petición HTTP ya parseada.
 * HttpParser rellena sus campos y luego el Router y los Handlers lo leen.
 * No tiene lógica: es solo un contenedor de datos.
 */
public class HttpRequest {

    /** Método HTTP: "GET", "POST", "PUT", "DELETE", "HEAD"... */
    public String method;

    /** Ruta completa tal como viene en la primera línea, p.ej. "/cats/3?foo=bar". */
    public String path;

    /** Versión del protocolo, normalmente "HTTP/1.1". */
    public String version;

    /**
     * Cabeceras HTTP en minúsculas como clave, p.ej. "content-type" -> "application/json".
     * El parser las normaliza a minúsculas para facilitar la comparación.
     */
    public Map<String, String> headers = new HashMap<>();

    /** Cuerpo de la petición. Vacío ("") si no hay body o Content-Length es 0. */
    public String body = "";

    /**
     * Parámetros de ruta extraídos por el Router.
     * P.ej. para la ruta "/cats/:id" con la URL "/cats/5", params = {"id": "5"}.
     * El Router lo rellena justo antes de llamar al Handler.
     */
    public Map<String, String> params = new HashMap<>();
}
