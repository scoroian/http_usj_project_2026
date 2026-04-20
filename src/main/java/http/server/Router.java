package http.server;

import http.shared.HttpRequest;
import http.shared.HttpResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enrutador HTTP: mapea pares (método + patrón de ruta) a su Handler.
 *
 * Convierte patrones estilo Express ("/cats/:id") a expresiones regulares
 * ("^/cats/([^/]+)$") para poder hacer matching y extraer parámetros de ruta.
 *
 * Cuando llega una petición:
 *  1. Busca la primera ruta cuyo regex coincida con la ruta limpia (sin query string).
 *  2. Si el método también coincide → extrae params y llama al handler.
 *  3. Si la ruta existe pero el método no → 405 Method Not Allowed.
 *  4. Si la ruta no existe → 404 Not Found.
 */
public class Router {

    private record Route(String method, Pattern regex, List<String> paramNames, Handler handler) {}

    private final List<Route> routes = new ArrayList<>();

    private void add(String method, String pattern, Handler handler) {
        List<String> paramNames = new ArrayList<>();
        java.util.regex.Matcher m = Pattern.compile(":([^/]+)").matcher(pattern);
        String regexStr = m.replaceAll(mr -> {
            paramNames.add(mr.group(1));
            return "([^/]+)";
        });
        routes.add(new Route(method, Pattern.compile("^" + regexStr + "$"), paramNames, handler));
    }

    public void get(String path, Handler h)    { add("GET",    path, h); }
    public void post(String path, Handler h)   { add("POST",   path, h); }
    public void put(String path, Handler h)    { add("PUT",    path, h); }
    public void delete(String path, Handler h) { add("DELETE", path, h); }
    public void head(String path, Handler h)   { add("HEAD",   path, h); }

    public void handle(HttpRequest req, HttpResponse res) {
        String cleanPath = req.path.contains("?") ? req.path.substring(0, req.path.indexOf('?')) : req.path;

        for (Route route : routes) {
            Matcher m = route.regex().matcher(cleanPath);
            if (!m.matches()) continue;
            if (!route.method().equals(req.method)) continue;

            Map<String, String> params = new HashMap<>();
            for (int i = 0; i < route.paramNames().size(); i++) {
                params.put(route.paramNames().get(i), m.group(i + 1));
            }
            req.params = params;
            route.handler().handle(req, res);
            return;
        }

        boolean pathExists = routes.stream()
            .anyMatch(r -> r.regex().matcher(cleanPath).matches());

        if (pathExists) {
            res.json(405, "{\"error\":\"Method not allowed\"}");
        } else {
            res.json(404, "{\"error\":\"Not found\"}");
        }
    }
}
