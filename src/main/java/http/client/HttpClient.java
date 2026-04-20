package http.client;

import http.shared.HttpParser.ParsedResponse;

import java.util.Map;

/**
 * Cliente HTTP sobre TCP puro.
 *
 * ESTADO: stub. send() devuelve null.
 */
public class HttpClient {

    /**
     * TODO: Miembro 4
     * Parsear la URL (host, puerto, path + query), abrir Socket TCP,
     * construir la petición con HttpBuilder, escribirla en el socket,
     * leer la respuesta hasta EOF, parsear con HttpParser.parseResponse().
     *
     * @param method       "GET", "POST", "PUT", "DELETE"...
     * @param url          URL completa ("http://localhost:3000/cats")
     * @param extraHeaders cabeceras adicionales o null
     * @param body         cuerpo o "" si no hay
     * @return ParsedResponse con status, headers y body
     */
    public static ParsedResponse send(String method, String url,
                                      Map<String, String> extraHeaders, String body) throws Exception {
        return null;
    }
}
