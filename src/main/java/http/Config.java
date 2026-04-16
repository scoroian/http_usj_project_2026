package http;

public class Config {
    public static final int    SERVER_PORT      = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000"));
    public static final String SERVER_HOST      = "0.0.0.0";
    public static final String API_KEY          = System.getenv().getOrDefault("API_KEY", "");
    public static final boolean LOGGING_ENABLED = !Boolean.parseBoolean(System.getenv().getOrDefault("LOGGING_DISABLED", "false"));
    public static final String LOG_FILE         = "logs/server.log";
}
