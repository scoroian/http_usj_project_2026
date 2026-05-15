package http.server.middleware;

import http.Config;
import http.shared.HttpRequest;
import http.shared.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/**
 * Middleware de logging.
 *
 * ESTADO: stub. De momento solo deja pasar la petición (next.run()) para que
 * el servidor funcione mientras Samuel implementa la lógica real.
 */
public class LoggerMiddleware implements Middleware {

    /**
     * TODO: Samuel
     * Registrar la petición usando res.setOnSend() para capturar el status code
     * y escribir la línea en consola + fichero logs/server.log.
     * Formato: [timestamp] METHOD /path -> status
     */
    /**
     * Registra el callback de logging en la respuesta y pasa el control al siguiente middleware.
     * El log real se escribe cuando el handler llama a res.send() (dentro del callback).
     */
    @Override
    public void apply(HttpRequest req, HttpResponse res, Runnable next) {
        String timestamp = Instant.now().toString();
        String prefix = "[" + timestamp + "] " + req.method + " " + req.path;

        // Se ejecuta cuando el handler llama a res.send() — captura el status code real
        res.setOnSend(status -> {
            String line = prefix + " -> " + status;
            System.out.println(line);
            if (Config.LOGGING_ENABLED) appendToLog(line + "\n");
        });

        next.run(); // continúa la cadena hacia el siguiente middleware o el router
    }
    /**
     * Añade una línea al fichero de log en disco.
     * Crea el directorio "logs/" si no existe.
     * Usa APPEND para no sobreescribir entradas anteriores.
     *
     * @param text línea de texto a añadir (ya incluye el salto de línea)
     */
    private void appendToLog(String text) {
        try {
            Path logFile = Path.of(Config.LOG_FILE);
            Files.createDirectories(logFile.getParent());
            Files.writeString(logFile, text, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Log write error: " + e.getMessage());
        }
    }
}
