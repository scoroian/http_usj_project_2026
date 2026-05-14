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
     * Serializa este gato a JSON.
     * Escapa comillas y barras en los strings para no romper el JSON.
     * Ejemplo de salida: {"id":1,"name":"Hercules","breed":"European","age":3}
     *
     * @return String JSON del gato
     */
    public String toJson() {
        return String.format("{\"id\":%d,\"name\":\"%s\",\"breed\":\"%s\",\"age\":%d}",
            id, escape(name), escape(breed), age);
    }

    /**
     * Serializa una colección de gatos a un array JSON.
     * Ejemplo: [{"id":1,...},{"id":2,...}]
     *
     * @param cats colección de gatos a serializar
     * @return String JSON con el array completo
     */
    public static String listToJson(Collection<Cat> cats) {
        return "[" + cats.stream().map(Cat::toJson).collect(Collectors.joining(",")) + "]";
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
