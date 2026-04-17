package http.shared;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Construye mensajes HTTP en crudo (String) para enviarlos por el socket TCP.
 * No realiza ninguna E/S: solo devuelve el String con el formato HTTP correcto
 * (línea de estado/request line + cabeceras + CRLF + body).
 * Lo usan tanto el servidor (buildResponse) como el cliente (buildRequest).
 */
public class HttpBuilder {

    /**
     * Tabla de textos de estado HTTP más comunes.
     * Si el código no está aquí, buildResponse usa "Unknown".
     */
    private static final Map<Integer, String> STATUS_TEXTS = Map.of(
        200, "OK",
        201, "Created",
        204, "No Content",
        304, "Not Modified",
        400, "Bad Request",
        401, "Unauthorized",
        403, "Forbidden",
        404, "Not Found",
        405, "Method Not Allowed",
        500, "Internal Server Error"
    );

    /**
     * Construye una petición HTTP/1.1 lista para enviar por el socket.
     * Añade automáticamente Host, Connection: close y Content-Length.
     *
     * @param method       método HTTP ("GET", "POST"...)
     * @param path         ruta con query string si la hay ("/cats?limit=10")
     * @param host         valor de la cabecera Host (hostname:puerto)
     * @param extraHeaders cabeceras adicionales (X-API-Key, etc.) o null
     * @param body         cuerpo de la petición o null si no hay body
     * @return String con el mensaje HTTP completo separado por CRLF
     */
    public static String buildRequest(String method, String path, String host,
                                      Map<String, String> extraHeaders, String body) {
        String bodyStr = body == null ? "" : body;

        // LinkedHashMap para mantener el orden de las cabeceras
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Host", host);
        headers.put("Connection", "close");
        headers.put("Content-Length", String.valueOf(bodyStr.getBytes(StandardCharsets.UTF_8).length));
        if (!bodyStr.isEmpty()) headers.put("Content-Type", "application/json");
        if (extraHeaders != null) headers.putAll(extraHeaders);

        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(path).append(" HTTP/1.1\r\n");
        headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\r\n"));
        sb.append("\r\n").append(bodyStr);
        return sb.toString();
    }

    /**
     * Construye una respuesta HTTP/1.1 lista para escribir en el socket del cliente.
     * Añade automáticamente Content-Length y Connection: close.
     *
     * @param status       código de estado HTTP (200, 404...)
     * @param extraHeaders cabeceras adicionales (Content-Type, etc.) o null
     * @param body         cuerpo de la respuesta o null si no hay body
     * @return String con el mensaje HTTP completo separado por CRLF
     */
    public static String buildResponse(int status, Map<String, String> extraHeaders, String body) {
        String bodyStr = body == null ? "" : body;
        String statusText = STATUS_TEXTS.getOrDefault(status, "Unknown");
        int contentLength = bodyStr.getBytes(StandardCharsets.UTF_8).length;

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Length", String.valueOf(contentLength));
        headers.put("Connection", "close");
        if (extraHeaders != null) headers.putAll(extraHeaders);

        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status).append(" ").append(statusText).append("\r\n");
        headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\r\n"));
        sb.append("\r\n").append(bodyStr);
        return sb.toString();
    }
}
