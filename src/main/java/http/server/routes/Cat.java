package http.server.routes;

import java.util.Collection;
import java.util.stream.Collectors;

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
     * Busca el patrón "key":"valor" y devuelve el valor entre comillas.
     * Solo funciona con valores simples (sin objetos ni arrays anidados).
     *
     * @param json String JSON de donde extraer
     * @param key  nombre del campo a buscar
     * @return valor del campo, o null si el campo no existe
     */
    public static String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf('"', start);
        return end == -1 ? null : json.substring(start, end);
    }

    /**
     * TODO: Samuel
     * Extrae el valor de un campo numérico entero del JSON.
     * Busca el patrón "key":valor y lee dígitos consecutivos.
     * Soporta negativos (lee el '-' inicial si lo hay).
     *
     * @param json String JSON de donde extraer
     * @param key  nombre del campo a buscar
     * @return valor entero, o null si el campo no existe o no es un número válido
     */
    public static Integer extractInt(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try { return Integer.parseInt(json.substring(start, end)); }
        catch (NumberFormatException e) { return null; }
    }
    /**
     * Escapa los caracteres especiales de JSON en un String.
     * Escapa '\' y '"' para que el JSON resultante sea válido.
     *
     * @param s String a escapar (puede ser null)
     * @return String escapado, o "" si era null
     */
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    
}
