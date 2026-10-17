package Utilidades;

import javax.swing.*;
import java.io.*;
import java.net.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public class VerificadorActualizacion {

    // 📁 Último release publicado en GitHub
    private static final String URL_ULTIMO_RELEASE =
        "https://api.github.com/repos/victorluis0292/vhaoPDV-releases/releases/latest";

    public static void verificar() {
        new Thread(() -> {   // 🔥 Evita congelar Swing
            System.out.println("🚀 Iniciando verificación de actualización...");

            try {
                String contenido = leerDesdeUrl(URL_ULTIMO_RELEASE);
                JSONObject json = new JSONObject(contenido);

                // Tag "v1.5.1" o "v.1.5" → "1.5.1" / "1.5"
                String versionRemota = json.getString("tag_name").replaceFirst("^[vV]\\.?", "");

                String notas = json.optString("body", "").trim();
                if (notas.isEmpty()) notas = "Sin notas disponibles";
                if (notas.length() > 500) notas = notas.substring(0, 500) + "...";
                final String notasFinal = notas;

                // Buscar el .exe adjunto al release
                String urlDescarga = null;
                JSONArray assets = json.getJSONArray("assets");
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject a = assets.getJSONObject(i);
                    if (a.getString("name").toLowerCase().endsWith(".exe")) {
                        urlDescarga = a.getString("browser_download_url");
                        break;
                    }
                }
                if (urlDescarga == null) {
                    throw new IOException("El release no tiene un .exe adjunto");
                }
                final String urlFinal = urlDescarga;

                String versionLocal = ConfigApp.getVersion();
                int cmp = compararVersiones(versionRemota, versionLocal);

                // Remota menor → ignorar
                if (cmp < 0) {
                    System.out.println("⚠ La versión remota (" + versionRemota +
                                       ") es menor que la local (" + versionLocal +
                                       "). Se ignora.");
                    return;
                }

                // Iguales
                if (cmp == 0) {
                    System.out.println("✅ Ya tienes la versión más reciente.");
                    return;
                }

                // Remota mayor → preguntar
                SwingUtilities.invokeLater(() -> {
                    int opcion = JOptionPane.showConfirmDialog(null,
                        "Hay una nueva versión disponible (" + versionRemota + ").\n\n" +
                        "Notas: " + notasFinal + "\n\n¿Deseas descargarla ahora?",
                        "Actualización disponible",
                        JOptionPane.YES_NO_OPTION
                    );

                    if (opcion == JOptionPane.YES_OPTION) {
                        descargarYReemplazar(urlFinal, versionRemota);
                    }
                });

            } catch (Exception e) {
                System.err.println("❌ Error al verificar actualización:");
                e.printStackTrace();
            }

        }).start();
    }

    private static String leerDesdeUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("User-Agent", "VHAO-PDV-Updater");
        conn.setRequestProperty("Accept", "application/vnd.github+json");

        if (conn.getResponseCode() != 200) {
            throw new IOException("Respuesta no válida del servidor: " + conn.getResponseCode());
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
            }
            return sb.toString();
        }
    }

    private static void descargarYReemplazar(String urlDescarga, String versionRemota) {
        LoaderUpdateSystemPDV loader = new LoaderUpdateSystemPDV(null, "Descargando actualización...");
        loader.mostrar();

        new Thread(() -> {
            File temporal = null;
            try {
                String nombreArchivo = "VHAO PDV Online " + versionRemota + ".exe";
                File destino = new File(nombreArchivo);
                temporal = new File(nombreArchivo + ".tmp");

                // Descargar primero a un archivo temporal
                URL url = new URL(urlDescarga);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "VHAO-PDV-Updater");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(30000);

                String tipo = conn.getContentType();
                if (conn.getResponseCode() != 200 || (tipo != null && tipo.contains("text/html"))) {
                    throw new IOException("El servidor devolvió HTML en lugar del .exe. "
                            + "Revisa que el release tenga el instalador adjunto.");
                }

                try (InputStream in = conn.getInputStream();
                     FileOutputStream out = new FileOutputStream(temporal)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long total = 0;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        total += bytesRead;
                    }
                    System.out.println("✅ Descarga completada (" + total / 1024 + " KB)");
                }

                // Solo ahora que la descarga salió bien, borrar versiones anteriores
                File[] archivos = new File(".").listFiles((dir, name) ->
                        (name.startsWith("VHAO PDV Online") || name.startsWith("VHAO.PDV-")) && name.endsWith(".exe"));
                if (archivos != null) {
                    for (File f : archivos) {
                        if (f.delete()) {
                            System.out.println("🗑 Archivo antiguo eliminado: " + f.getName());
                        }
                    }
                }

                // Renombrar el temporal al nombre final
                if (!temporal.renameTo(destino)) {
                    throw new IOException("No se pudo guardar el archivo descargado.");
                }

                // Ejecutar instalador automáticamente
                Runtime.getRuntime().exec("java -jar Updater1.jar \"" + nombreArchivo + "\"");

                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                        "✅ Nueva versión descargada y ejecutada.\nArchivo: " + nombreArchivo,
                        "Actualización completada", JOptionPane.INFORMATION_MESSAGE));

                // Cerrar la app actual
                System.exit(0);

            } catch (Exception e) {
                if (temporal != null && temporal.exists()) temporal.delete();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                        "❌ Error durante la descarga: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE));
                e.printStackTrace();
            } finally {
                loader.cerrar();
            }
        }).start();
    }

    /**
     * 🔹 Compara dos versiones tipo "1.2.3"
     * Devuelve >0 si v1 > v2, 0 si son iguales, <0 si v1 < v2
     */
    private static int compararVersiones(String v1, String v2) {
        String[] partes1 = v1.split("\\.");
        String[] partes2 = v2.split("\\.");
        int longitud = Math.max(partes1.length, partes2.length);

        for (int i = 0; i < longitud; i++) {
            int p1 = i < partes1.length ? aNumero(partes1[i]) : 0;
            int p2 = i < partes2.length ? aNumero(partes2[i]) : 0;
            if (p1 != p2) return p1 - p2;
        }
        return 0;
    }

    // Convierte "5" o "5-beta" en 5; si no hay dígitos, 0
    private static int aNumero(String parte) {
        String soloDigitos = parte.replaceAll("[^0-9]", "");
        return soloDigitos.isEmpty() ? 0 : Integer.parseInt(soloDigitos);
    }
}