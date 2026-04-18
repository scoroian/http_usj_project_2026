package http.shared;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Wrapper del OutputStream del socket que expone métodos cómodos para enviar respuestas HTTP.
 * Garantiza que la respuesta solo se envía una vez (flag "sent").
 * También permite que un middleware registre un callback para capturar el status code
 * sin tener que modificar el handler.
 */
public class HttpResponse {

    /** Stream TCP del socket al que se escribirá la respuesta. */
    private final OutputStream out;

    /** Evita enviar la respuesta dos veces si el handler llama a send() varias veces. */
    private boolean sent = false;

    /**
     * Callback opcional que se ejecuta justo antes de escribir la respuesta.
     * Lo usa LoggerMiddleware para capturar el status code sin acoplarse al handler.
     */
    private Consumer<Integer> onSend = null;

    /** Crea la respuesta asociada al OutputStream del socket. */
    public HttpResponse(OutputStream out) {
        this.out = out;
    }

    /**
     * Registra un callback que recibe el status code cuando se llama a send().
     * Permite que el middleware de logging sepa qué código devolvió el handler.
     *
     * @param callback función que recibe el status code (p.ej. 200, 404...)
     */
    public void setOnSend(Consumer<Integer> callback) {
        this.onSend = callback;
    }

    /**
     * Construye y envía la respuesta HTTP completa por el socket.
     * Solo se ejecuta la primera vez; llamadas posteriores se ignoran.
     *
     * @param status      código HTTP (200, 404, 500...)
     * @param headers     cabeceras adicionales (Content-Type, etc.)
     * @param body        cuerpo de la respuesta como String
     */
    public void send(int status, Map<String, String> headers, String body) {
        if (sent) return;
        sent = true;

        // Notifica al middleware antes de escribir
        if (onSend != null) onSend.accept(status);

        String raw = HttpBuilder.buildResponse(status, headers, body);
        try {
            out.write(raw.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            System.err.println("Error writing response: " + e.getMessage());
        }
    }

    /**
     * Atajo para respuestas JSON: pone Content-Type: application/json automáticamente.
     *
     * @param status   código HTTP
     * @param jsonBody body ya serializado como String JSON
     */
    public void json(int status, String jsonBody) {
        send(status, Map.of("Content-Type", "application/json"), jsonBody);
    }
}
