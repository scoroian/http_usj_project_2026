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
}
