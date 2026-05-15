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
        int id = parseId(req.params.get("id"));
        Cat cat = cats.get(id);

        if (cat == null) {
            res.json(404, "{\"error\":\"Cat not found\"}");
            return;
        }

        res.json(200, cat.toJson());
    }

    /**
     * TODO: Julio — POST /cats
     * Crea un gato nuevo. Valida name, breed y age. 400 si falta algo, 201 si se crea.
     */
    public void create(HttpRequest req, HttpResponse res) {
        String name = Cat.extractString(req.body, "name");
        String breed = Cat.extractString(req.body, "breed");
        Integer age = Cat.extractInt(req.body, "age");

        if (name == null || breed == null || age == null) {
            res.json(400, "{\"error\":\"name, breed and age are required\"}");
            return;
        }

        int id = nextId.getAndIncrement();
        Cat cat = new Cat(id, name, breed, age);
        cats.put(id, cat);

        res.json(201, cat.toJson());
    }

    /**
     * TODO: Julio — PUT /cats/:id
     * Actualización parcial: conserva campos no enviados. 404 si no existe.
     */
    public void update(HttpRequest req, HttpResponse res) {
        int id = parseId(req.params.get("id"));
        Cat existingCat = cats.get(id);

        if (existingCat == null) {
            res.json(404, "{\"error\":\"Cat not found\"}");
            return;
        }

        String name = Cat.extractString(req.body, "name");
        String breed = Cat.extractString(req.body, "breed");
        Integer age = Cat.extractInt(req.body, "age");

        Cat updatedCat = new Cat(
            id,
            name != null ? name : existingCat.name,
            breed != null ? breed : existingCat.breed,
            age != null ? age : existingCat.age
        );

        cats.put(id, updatedCat);

        res.json(200, updatedCat.toJson());
    }

    /**
     * TODO: Alberto — DELETE /cats/:id
     * Elimina un gato. 204 sin body si se eliminó, 404 si no existía.
     */
    public void remove(HttpRequest req, HttpResponse res) {
        int id = parseId(req.params.get("id"));

        if (!cats.containsKey(id)) {
            res.json(404, "{\"error\":\"Cat not found\"}");
            return;
        }

        cats.remove(id);
        res.send(204, null, "");
    }

    /**
     * Convierte el string del parámetro :id a entero.
     * Devuelve -1 si no es un número válido (no coincidirá con ningún ID real).
     */
    private int parseId(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
