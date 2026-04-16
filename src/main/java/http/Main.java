package http;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = askPort();
        System.out.println("Servidor arrancando en el puerto " + port + "...");
        // TODO: iniciar servidor HTTP
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
