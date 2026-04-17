package http.shared;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parsea bytes TCP crudos y los convierte en objetos Java utilizables.
 * Tiene dos modos:
 *  - parseRequest(): para el servidor, lee del InputStream del socket byte a byte.
 *  - parseResponse(): para el cliente, parsea el String completo recibido tras cerrar conexión.
 */
public class HttpParser {

    /**
     * Lee una petición HTTP completa del InputStream del socket y la convierte en HttpRequest.
     * Proceso:
     *  1. Lee byte a byte hasta encontrar la secuencia \r\n\r\n (fin de cabeceras).
     *  2. Parsea la request line (método, ruta, versión).
     *  3. Parsea las cabeceras línea a línea.
     *  4. Lee exactamente Content-Length bytes de body.
     *
     * @param in InputStream del socket TCP del cliente
     * @return HttpRequest con todos los campos rellenos, o null si la conexión se cerró sin datos
     * @throws IOException si hay un error de lectura de red
     */
    public static HttpRequest parseRequest(InputStream in) throws IOException {
        byte[] headerBytes = readUntilHeaderEnd(in);
        if (headerBytes == null) return null;

        String headerSection = new String(headerBytes, StandardCharsets.UTF_8);
        String[] lines = headerSection.split("\r\n");

        // Primera línea: "GET /cats HTTP/1.1"
        String[] requestLine = lines[0].split(" ", 3);
        if (requestLine.length < 3) return null;

        Map<String, String> headers = parseHeaders(lines);

        // Leer el body si hay Content-Length
        int contentLength = Integer.parseInt(headers.getOrDefault("content-length", "0"));
        String body = readBody(in, contentLength);

        HttpRequest req = new HttpRequest();
        req.method  = requestLine[0];
        req.path    = requestLine[1];
        req.version = requestLine[2];
        req.headers = headers;
        req.body    = body;
        return req;
    }

    /**
     * Parsea una respuesta HTTP recibida como String completo.
     * El cliente la usa después de leer todos los bytes hasta que el servidor cierra la conexión.
     *
     * @param raw String completo de la respuesta HTTP tal como llegó por el socket
     * @return ParsedResponse con status, cabeceras y body, o null si el formato es inválido
     */
    public static ParsedResponse parseResponse(String raw) {
        int headerEnd = raw.indexOf("\r\n\r\n");
        if (headerEnd == -1) return null;

        String[] lines = raw.substring(0, headerEnd).split("\r\n");

        // Primera línea: "HTTP/1.1 200 OK"
        String[] statusParts = lines[0].split(" ", 3);
        int status = Integer.parseInt(statusParts[1]);
        String statusText = statusParts.length > 2 ? statusParts[2] : "";

        Map<String, String> headers = parseHeaders(lines);
        String body = raw.substring(headerEnd + 4); // +4 para saltar el \r\n\r\n

        return new ParsedResponse(status, statusText, headers, body);
    }

    /**
     * Lee el InputStream.
     * Cuando los últimos 4 bytes leídos son \r\n\r\n, significa que han llegado
     * todas las cabeceras y para de leer.
     * Este método NO lee el body (eso lo hace readBody con Content-Length).
     *
     * @param in InputStream del socket
     * @return bytes de las cabeceras sin el \r\n\r\n final, o null si el stream se cerró
     * @throws IOException si hay error de red
     */
    private static byte[] readUntilHeaderEnd(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int[] last4 = {0, 0, 0, 0}; // para detectar \r\n\r\n
        int b;

        while ((b = in.read()) != -1) {
            buf.write(b);
            // insertar el byte nuevo
            last4[0] = last4[1]; last4[1] = last4[2]; last4[2] = last4[3]; last4[3] = b;
            if (last4[0] == '\r' && last4[1] == '\n' && last4[2] == '\r' && last4[3] == '\n') {
                byte[] all = buf.toByteArray();
                // Devolver los bytes sin los 4 \r\n\r\n
                byte[] result = new byte[all.length - 4];
                System.arraycopy(all, 0, result, 0, result.length);
                return result;
            }
        }
        return null; // el cliente cerró la conexión antes de terminar las cabeceras
    }

    /**
     * Lee exactamente contentLength bytes del InputStream para obtener el body.
     * Usa un bucle porque read() puede devolver menos bytes de los pedidos
     * si el SO no los tiene todos aún en el buffer.
     *
     * @param in            InputStream del socket
     * @param contentLength número exacto de bytes a leer (valor de Content-Length)
     * @return body como String UTF-8, o "" si contentLength <= 0
     * @throws IOException si hay error de red
     */
    private static String readBody(InputStream in, int contentLength) throws IOException {
        if (contentLength <= 0) return "";
        byte[] body = new byte[contentLength];
        int totalRead = 0;
        while (totalRead < contentLength) {
            int n = in.read(body, totalRead, contentLength - totalRead);
            if (n == -1) break; // conexión cerrada antes de recibir todo el body
            totalRead += n;
        }
        return new String(body, 0, totalRead, StandardCharsets.UTF_8);
    }

    /**
     * Parsea las líneas de cabecera (desde la línea 1 en adelante, saltándose la request/status line).
     * Normaliza los nombres de cabecera a minúsculas para que la comparación sea case-insensitive.
     *
     * @param lines array de líneas del bloque de cabeceras; lines[0] es la request/status line
     * @return mapa nombre-en-minúsculas -> valor de la cabecera
     */
    private static Map<String, String> parseHeaders(String[] lines) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon == -1) continue;
            String name  = lines[i].substring(0, colon).trim().toLowerCase();
            String value = lines[i].substring(colon + 1).trim();
            headers.put(name, value);
        }
        return headers;
    }

    /**
     * Record inmutable que encapsula una respuesta HTTP parseada.
     * Lo usa el cliente (HttpClient + Cli) para acceder al resultado de la petición.
     */
    public record ParsedResponse(int status, String statusText, Map<String, String> headers, String body) {}
}
