package http.server.routes;

import java.util.Collection;

/**
 * POJO que representa un gato en el sistema.
 * Incluye serialización/deserialización JSON manual (sin Gson ni Jackson).
 *
 * ESTADO: campos y constructor listos. Los métodos JSON son stubs.
 * La referencia completa está en src_original/main/java/http/server/routes/Cat.java
 */
public class Cat {
    public int id;
    public String name;
    public String breed;
    public int age;

    public Cat(int id, String name, String breed, int age) {
        this.id = id; this.name = name; this.breed = breed; this.age = age;
    }

    /**
     * TODO: Samuel
     * Serializa este gato a JSON: {"id":1,"name":"...","breed":"...","age":3}
     */
    public String toJson() {
        return "";
    }

    /**
     * TODO: Samuel
     * Serializa una colección de gatos a un array JSON: [{...},{...}]
     */
    public static String listToJson(Collection<Cat> cats) {
        return "[]";
    }

    /**
     * TODO: Samuel
     * Extrae el valor de un campo String del JSON.
     * Busca el patrón "key":"valor".
     */
    public static String extractString(String json, String key) {
        return null;
    }

    /**
     * TODO: Samuel
     * Extrae el valor de un campo numérico entero del JSON.
     */
    public static Integer extractInt(String json, String key) {
        return null;
    }
}
