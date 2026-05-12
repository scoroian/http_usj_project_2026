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
}
