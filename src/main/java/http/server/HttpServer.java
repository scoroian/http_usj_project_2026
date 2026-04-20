package http.server;

import http.Config;
import http.shared.HttpParser;
import http.shared.HttpRequest;
import http.shared.HttpResponse;
import http.server.middleware.Middleware;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor HTTP sobre TCP puro.
 * Abre un ServerSocket en el puerto indicado y despacha cada conexión entrante
 * a un hilo del pool. Para cada conexión:
 *  1. Parsea la petición HTTP con HttpParser.
 *  2. Ejecuta la cadena de middlewares.
 *  3. Al final de la cadena, el Router elige el handler correcto.
 */
public class HttpServer {

    private final Router router;
    private final List<Middleware> middlewares;

    public HttpServer(Router router, List<Middleware> middlewares) {
        this.router = router;
        this.middlewares = middlewares;
    }

    public void start(int port, String host) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(host, port));

        ExecutorService pool = Executors.newFixedThreadPool(20);

        String displayHost = host.equals("0.0.0.0") ? "localhost" : host;
        System.out.println("Server listening on http://" + displayHost + ":" + port);
        if (!Config.API_KEY.isEmpty()) {
            System.out.println("API key auth enabled (X-API-Key required)");
        }

        while (!serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();
            pool.submit(() -> handleConnection(socket));
        }
    }

    private void handleConnection(Socket socket) {
        try (socket) {
            HttpRequest req = HttpParser.parseRequest(socket.getInputStream());
            if (req == null) return;

            HttpResponse res = new HttpResponse(socket.getOutputStream());
            runMiddlewares(0, req, res, () -> router.handle(req, res));

        } catch (Exception e) {
            if (e.getMessage() != null && !e.getMessage().contains("Connection reset")) {
                System.err.println("Connection error: " + e.getMessage());
            }
        }
    }

    private void runMiddlewares(int index, HttpRequest req, HttpResponse res, Runnable last) {
        if (index >= middlewares.size()) {
            last.run();
        } else {
            middlewares.get(index).apply(req, res,
                () -> runMiddlewares(index + 1, req, res, last));
        }
    }
}
