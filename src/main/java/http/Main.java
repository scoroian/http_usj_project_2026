package http;

import http.server.HttpServer;
import http.server.Router;
import http.server.middleware.AuthMiddleware;
import http.server.middleware.LoggerMiddleware;
import http.server.routes.CatsHandler;
import http.server.routes.StaticHandler;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = askPort();

        Router router = new Router();

        // Páginas y assets estáticos
        router.get("/",           StaticHandler::serve);
        router.get("/index.html", StaticHandler::serve);
        router.get("/cats.html",  StaticHandler::serve);
        router.get("/style.css",  StaticHandler::serve);
        router.get("/app.js",     StaticHandler::serve);

        // API REST de gatos
        CatsHandler cats = new CatsHandler();
        router.get("/cats",         cats::getAll);
        router.get("/cats/:id",     cats::getOne);
        router.post("/cats",        cats::create);
        router.put("/cats/:id",     cats::update);
        router.delete("/cats/:id",  cats::remove);

        var middlewares = List.of(new LoggerMiddleware(), new AuthMiddleware());

        HttpServer server = new HttpServer(router, middlewares);
        server.start(port, Config.SERVER_HOST);
    }

    private static int askPort() {
        int defaultPort = Config.SERVER_PORT;
        System.out.print("Puerto [" + defaultPort + "]: ");
        String input = new Scanner(System.in).nextLine().trim();
        if (input.isEmpty()) return defaultPort;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Puerto inválido, usando " + defaultPort);
            return defaultPort;
        }
    }
}
