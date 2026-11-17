package Modelo;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Conexion {

    private static HikariDataSource dataSource;
    private static boolean inicializado = false;

    // 🔹 Entorno por defecto
    private static String environment = "local";

    // 🔹 Cambiar el entorno dinámicamente
    public static void setEnvironment(String env) {
        environment = env;
    }

    // 🔹 Obtener el entorno actual
    public static String getEnvironment() {
        return environment;
    }

    // 🔹 Inicializa el pool HikariCP
    private static void inicializarPool() {

        // Si ya existe el pool, no lo volvemos a crear
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        synchronized (Conexion.class) {

            // Segunda comprobación para evitar crear dos pools
            if (dataSource != null && !dataSource.isClosed()) {
                return;
            }

            try {

                // 🔹 Seleccionar archivo según el entorno
              String file = environment.equalsIgnoreCase("prod")
        ? "config/config.prod.properties"   // si es prod → archivo de prod
        : "config/config.local.properties"; // si no → archivo local

                try (InputStream input =
                             Conexion.class.getClassLoader().getResourceAsStream(file)) {

                    if (input == null) {
                        throw new IOException(
                                "Archivo de configuración no encontrado: " + file
                        );
                    }

                    Properties props = new Properties();
                    props.load(input);

                    // =========================================================
                    // 🔹 CONFIGURACIÓN HIKARICP
                    // =========================================================

                    HikariConfig config = new HikariConfig();

                    // Datos de conexión
                    config.setJdbcUrl(props.getProperty("jdbc.url"));
                    config.setUsername(props.getProperty("db.user"));
                    config.setPassword(props.getProperty("db.password"));

                    // =========================================================
                    // 🔹 POOL DE CONEXIONES
                    // =========================================================

                    // Máximo de conexiones simultáneas
                    config.setMaximumPoolSize(10);

                    // Conexiones mínimas que Hikari mantiene disponibles
                    config.setMinimumIdle(0);

                    /*
                     * Tu servidor MySQL tiene:
                     *
                     * wait_timeout = 20 segundos
                     *
                     * Por eso no conviene mantener conexiones inactivas
                     * durante varios minutos.
                     */

                    // 🔹 Cerrar conexiones inactivas después de 10 segundos
                    config.setIdleTimeout(15_000);


                    // 🔹 Reciclar conexiones antes del wait_timeout de MySQL
                    config.setMaxLifetime(18_000);

                    // 🔹 Esperar máximo 30 segundos por una conexión
                    config.setMaxLifetime(30_000);

                    // 🔹 Tiempo máximo para validar una conexión
                    config.setValidationTimeout(5_000);
                    
                    // Necesario cuando minimumIdle=0: evita que Hikari se queje
                    config.setKeepaliveTime(0);

                    // =========================================================
                    // 🔹 CREAR POOL
                    // =========================================================

                    dataSource = new HikariDataSource(config);

                    inicializado = true;

                    logInfo(
                            "Pool HikariCP inicializado correctamente. "
                                    + "Entorno: " + environment
                    );
                }

            } catch (Exception e) {

                inicializado = false;

                logError(
                        "Error al inicializar pool HikariCP",
                        e
                );
            }
        }
    }

    // =========================================================
    // 🔹 OBTENER CONEXIÓN
    // =========================================================

    public static Connection getConnection() throws SQLException {

        inicializarPool();

        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Pool HikariCP no inicializado.");
        }

        return dataSource.getConnection();
    }

    // =========================================================
    // 🔹 VERIFICAR CONEXIÓN
    // =========================================================

    public static boolean hayConexion() {

        try {

            inicializarPool();

            if (dataSource == null || dataSource.isClosed()) {
                return false;
            }

            try (Connection con = dataSource.getConnection()) {

                return con != null && !con.isClosed();
            }

        } catch (SQLException e) {

            logError(
                    "Sin conexión a la base de datos",
                    e
            );

            return false;
        }
    }

    // =========================================================
    // 🔹 VERIFICAR INTERNET RÁPIDAMENTE
    // =========================================================

    public static boolean hayInternetRapido() {

        try {

            java.net.URL url =
                    new java.net.URL("https://www.google.com");

            java.net.HttpURLConnection con =
                    (java.net.HttpURLConnection) url.openConnection();

            con.setConnectTimeout(2_000);
            con.setReadTimeout(2_000);

            con.connect();

            int responseCode = con.getResponseCode();

            con.disconnect();

            return responseCode == 200;

        } catch (Exception e) {

            return false;
        }
    }

    // =========================================================
    // 🔹 CERRAR POOL
    // =========================================================

    public static void closeDataSource() {

        if (dataSource != null && !dataSource.isClosed()) {

            try {

                dataSource.close();

                inicializado = false;

                logInfo(
                        "Pool HikariCP cerrado correctamente."
                );

            } catch (Exception e) {

                logError(
                        "Error al cerrar pool HikariCP",
                        e
                );
            }
        }
    }

    // =========================================================
    // 🔹 ESTADO DEL POOL
    // =========================================================

    public static boolean estaInicializado() {

        return inicializado
                && dataSource != null
                && !dataSource.isClosed();
    }

    // =========================================================
    // 🔹 LOG INFO
    // =========================================================

    public static void logInfo(String mensaje) {

        writeLog(
                "INFO",
                mensaje
        );
    }

    // =========================================================
    // 🔹 LOG ERROR
    // =========================================================

    public static void logError(
            String mensaje,
            Exception e
    ) {

        writeLog(
                "ERROR",
                mensaje + " - "
                        + (e != null
                        ? e.getMessage()
                        : "")
        );
    }

    // =========================================================
    // 🔹 ESCRIBIR LOG
    // =========================================================

    private static void writeLog(
            String tipo,
            String mensaje
    ) {

        try {

            Path logDir =
                    Paths.get("logs");

            // Crear carpeta logs si no existe
            if (!Files.exists(logDir)) {

                Files.createDirectories(logDir);
            }

            Path logFile =
                    logDir.resolve("conexion_log.txt");

            try (
                    FileWriter fw =
                            new FileWriter(
                                    logFile.toFile(),
                                    true
                            );

                    PrintWriter pw =
                            new PrintWriter(fw)
            ) {

                pw.println(
                        LocalDateTime.now()
                                + " - "
                                + tipo
                                + " - "
                                + mensaje
                );
            }

        } catch (IOException ex) {

            System.err.println(
                    "No se pudo escribir en el log: "
                            + ex.getMessage()
            );
        }
    }

    // =========================================================
    // 🔹 SABER SI ESTAMOS EN LOCAL
    // =========================================================

    public static boolean esLocal() {

        return environment.equalsIgnoreCase("local");
    }
}