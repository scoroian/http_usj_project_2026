package http.client;

import http.shared.HttpParser.ParsedResponse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Cliente HTTP interactivo por línea de comandos.
 *
 * Se arranca con: mvn exec:java -Dexec.mainClass="http.client.Cli"
 */
public class Cli {

    /**
     * TODO: Julio
     * Bucle que pregunta método, URL, cabeceras y body (para POST/PUT),
     * usa HttpClient.send() para enviar la petición y muestra la respuesta formateada.
     * Sale cuando el usuario escribe "EXIT" como método.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("HTTP Client CLI");
        System.out.println("Escribe EXIT como método para salir.");

        while (true) {
            try {
                System.out.print("\nMethod (GET/POST/PUT/DELETE/HEAD): ");
                String method = scanner.nextLine().trim().toUpperCase();

                if (method.equals("EXIT")) {
                    System.out.println("Saliendo...");
                    break;
                }

                if (method.isEmpty()) {
                    System.out.println("El método no puede estar vacío.");
                    continue;
                }

                System.out.print("URL: ");
                String url = scanner.nextLine().trim();

                if (url.isEmpty()) {
                    System.out.println("La URL no puede estar vacía.");
                    continue;
                }

                Map<String, String> extraHeaders = readHeaders(scanner);

                String body = "";
                if (method.equals("POST") || method.equals("PUT")) {
                    System.out.print("Body (JSON): ");
                    body = scanner.nextLine();
                }

                ParsedResponse response = HttpClient.send(method, url, extraHeaders, body);
                printResponse(response);

            } catch (Exception e) {
                System.out.println("Error sending request: " + e.getMessage());
            }
        }
    }

    private static Map<String, String> readHeaders(Scanner scanner) {
        Map<String, String> headers = new LinkedHashMap<>();

        System.out.print("Extra headers [Enter to skip]: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return headers;
        }

        String[] pairs = input.split(",");

        for (String pair : pairs) {
            int colon = pair.indexOf(":");

            if (colon == -1) {
                System.out.println("Header ignorado por formato inválido: " + pair.trim());
                continue;
            }

            String name = pair.substring(0, colon).trim();
            String value = pair.substring(colon + 1).trim();

            if (!name.isEmpty() && !value.isEmpty()) {
                headers.put(name, value);
            }
        }

        return headers;
    }

    private static void printResponse(ParsedResponse response) {
        if (response == null) {
            System.out.println("Respuesta inválida o vacía.");
            return;
        }

        System.out.println("\n--- Response ---");
        System.out.println("Status: " + response.status() + " " + response.statusText());

        System.out.println("Headers:");
        response.headers().forEach((key, value) ->
            System.out.println(key + ": " + value)
        );

        String body = response.body() == null ? "" : response.body();
        if (body.length() > 500) {
            body = body.substring(0, 500) + "...";
        }

        System.out.println("Body: " + body);
        System.out.println("----------------");
    }
}
