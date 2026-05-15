package http.client;

import http.shared.HttpBuilder;
import http.shared.HttpParser;
import http.shared.HttpParser.ParsedResponse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Cliente HTTP sobre TCP puro.
 */
public class HttpClient {

    /**
     * TODO: Julio
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
        URI uri = new URI(url);

        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 80 : uri.getPort();

        String path = uri.getRawPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        if (uri.getRawQuery() != null) {
            path += "?" + uri.getRawQuery();
        }

        String hostHeader = uri.getPort() == -1 ? host : host + ":" + port;
        String request = HttpBuilder.buildRequest(method, path, hostHeader, extraHeaders, body);

        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(10_000);

            OutputStream out = socket.getOutputStream();
            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();

            InputStream in = socket.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            byte[] chunk = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }

            String rawResponse = buffer.toString(StandardCharsets.UTF_8);

            return HttpParser.parseResponse(rawResponse);
        }
    }
}
