package http.server.routes;

import http.shared.HttpRequest;
import http.shared.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Handler que sirve ficheros estáticos desde el directorio "public/".
 * Soporta HTML, CSS, JS, JSON, imágenes PNG/JPG e iconos.
 * Protege contra path traversal.
 */
public class StaticHandler {

    private static final Map<String, String> MIME_TYPES = Map.of(
        ".html", "text/html; charset=utf-8",
        ".css",  "text/css",
        ".js",   "application/javascript",
        ".json", "application/json",
        ".png",  "image/png",
        ".jpg",  "image/jpeg",
        ".ico",  "image/x-icon",
        ".txt",  "text/plain"
    );

    private static final Path PUBLIC_DIR = Path.of("public").toAbsolutePath();

    public static void serve(HttpRequest req, HttpResponse res) {
        String urlPath = req.path.equals("/") ? "/index.html" : req.path;
        Path filePath = PUBLIC_DIR.resolve("." + urlPath).normalize();

        if (!filePath.startsWith(PUBLIC_DIR)) {
            res.send(403, Map.of("Content-Type", "text/plain"), "Forbidden");
            return;
        }

        if (!Files.exists(filePath)) {
            res.send(404, Map.of("Content-Type", "text/plain"), "File not found");
            return;
        }

        try {
            String content = Files.readString(filePath);
            String ext = getExtension(filePath.getFileName().toString());
            String contentType = MIME_TYPES.getOrDefault(ext, "application/octet-stream");
            res.send(200, Map.of("Content-Type", contentType), content);
        } catch (IOException e) {
            res.send(500, Map.of("Content-Type", "text/plain"), "Error reading file");
        }
    }

    private static String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? "" : filename.substring(dot);
    }
}
