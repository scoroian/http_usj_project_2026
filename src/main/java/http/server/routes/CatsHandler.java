package http.server.routes;

import http.shared.HttpRequest;
import http.shared.HttpResponse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handler CRUD para el recurso /cats.
 * Almacén en memoria thread-safe con ConcurrentHashMap + AtomicInteger.
 *
 * ESTADO: estructura y almacén listos. Cada endpoint es un stub que no responde.
 * Mientras los stubs estén vacíos, el cliente recibirá una respuesta vacía o timeout.
 */
public class CatsHandler {

    private final ConcurrentHashMap<Integer, Cat> cats = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(3);

    public CatsHandler() {
        cats.put(1, new Cat(1, "Hercules", "European", 3));
        cats.put(2, new Cat(2, "Luna", "Siamese", 5));
    }

    /**
     * TODO: Samuel — GET /cats
     * Devuelve la lista completa de gatos en JSON con status 200.
     */
    public void getAll(HttpRequest req, HttpResponse res) {
    }

    /**
     * TODO: Alberto — GET /cats/:id
     * Devuelve un gato por ID, o 404 si no existe.
     */
    public void getOne(HttpRequest req, HttpResponse res) {
    }

    /**
     * TODO: Miembro 4 — POST /cats
     * Crea un gato nuevo. Valida name, breed y age. 400 si falta algo, 201 si se crea.
     */
    public void create(HttpRequest req, HttpResponse res) {
    }

    /**
     * TODO: Miembro 4 — PUT /cats/:id
     * Actualización parcial: conserva campos no enviados. 404 si no existe.
     */
    public void update(HttpRequest req, HttpResponse res) {
    }

    /**
     * TODO: Alberto — DELETE /cats/:id
     * Elimina un gato. 204 sin body si se eliminó, 404 si no existía.
     */
    public void remove(HttpRequest req, HttpResponse res) {
    }

    /**
     * Convierte el string del parámetro :id a entero.
     * Devuelve -1 si no es un número válido (no coincidirá con ningún ID real).
     */
    private int parseId(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return -1; }
    }
}
