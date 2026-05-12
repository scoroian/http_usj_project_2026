package http.server.middleware;

import http.shared.HttpRequest;
import http.shared.HttpResponse;

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
    @Override
    public void apply(HttpRequest req, HttpResponse res, Runnable next) {
        next.run();
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
