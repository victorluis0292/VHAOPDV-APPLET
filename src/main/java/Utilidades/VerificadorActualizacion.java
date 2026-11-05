package Utilidades;

import javax.swing.*;
import java.io.*;
import java.net.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerificadorActualizacion {

    // 📁 Último release publicado en GitHub
    private static final String URL_ULTIMO_RELEASE =
        "https://api.github.com/repos/victorluis0292/vhaoPDV-releases/releases/latest";
    // true = solo descarga y muestra diagnóstico, NO ejecuta el instalador ni cierra la app
    private static final boolean MODO_PRUEBA = false;

    // CAMBIO: patrón para extraer solo el número de versión (ej. 1.5.8) de cualquier tag
    private static final Pattern PATRON_VERSION = Pattern.compile("(\\d+(?:\\.\\d+)*)");

    public static void verificar() {
        new Thread(() -> {   // 🔥 Evita congelar Swing
            System.out.println("🚀 Iniciando verificación de actualización...");

            try {
                String contenido = leerDesdeUrl(URL_ULTIMO_RELEASE);
                JSONObject json = new JSONObject(contenido);

                // CAMBIO: la versión se toma SOLO del tag y se normaliza
                // Acepta "1.5.8", "v1.5.8", "V.1.5.8", "version 1.5.8", " 1.5.8 "
                String tagRemoto = json.getString("tag_name");
                String versionRemota = normalizarVersion(tagRemoto);
                String versionLocal = normalizarVersion(ConfigApp.getVersion());

                int cmp = compararVersiones(versionRemota, versionLocal);
                System.out.println("Tag remoto: '" + tagRemoto + "' | remota: " + versionRemota
                        + " | local: " + versionLocal + " | cmp=" + cmp);

                // CAMBIO: la comparación se hace ANTES de buscar el .exe,
                // así, si ya estás al día, no marca error por falta de adjunto
                if (cmp < 0) {
                    System.out.println("⚠ La versión remota (" + versionRemota +
                                       ") es menor que la local (" + versionLocal +
                                       "). Se ignora.");
                    return;
                }

                if (cmp == 0) {
                    System.out.println("✅ Ya tienes la versión más reciente.");
                    return;
                }

                // Remota mayor → ahora sí necesitamos el instalador
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
                    throw new IOException("El release " + versionRemota + " no tiene un .exe adjunto");
                }
                final String urlFinal = urlDescarga;

                // Preguntar al usuario
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
            HttpURLConnection conn = null;
            try {
                // Carpeta con permiso de escritura (Program Files no lo permite)
                File carpeta = new File(System.getProperty("java.io.tmpdir"), "VHAO_PDV_Update");
                if (!carpeta.exists() && !carpeta.mkdirs()) {
                    throw new IOException("No se pudo crear la carpeta de descarga: " + carpeta);
                }

                String nombreArchivo = "VHAO PDV Online " + versionRemota + ".exe";
                File destino = new File(carpeta, nombreArchivo);
                temporal = new File(carpeta, nombreArchivo + ".tmp");

                // ------------------------------------------------------------
                // Abrir conexión siguiendo redirecciones a mano (máx. 5)
                // ------------------------------------------------------------
                String urlActual = urlDescarga;
                int codigo = 0;

                for (int salto = 0; salto <= 5; salto++) {
                    System.out.println("🌐 Pidiendo [" + salto + "]: " + urlActual);

                    conn = (HttpURLConnection) new URL(urlActual).openConnection();
                    conn.setInstanceFollowRedirects(false);
                    conn.setRequestProperty("User-Agent", "VHAO-PDV-Updater");
                    conn.setRequestProperty("Accept", "application/octet-stream");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(30000);

                    codigo = conn.getResponseCode();
                    System.out.println("   HTTP   : " + codigo);

                    if (codigo == 301 || codigo == 302 || codigo == 303
                            || codigo == 307 || codigo == 308) {
                        String destinoRedir = conn.getHeaderField("Location");
                        conn.disconnect();
                        if (destinoRedir == null || destinoRedir.isEmpty()) {
                            throw new IOException("Redirección sin destino (HTTP " + codigo + ")");
                        }
                        // Resuelve rutas relativas si las hubiera
                        urlActual = new URL(new URL(urlActual), destinoRedir).toString();
                        continue;
                    }
                    break;   // no es redirección: ya tenemos la respuesta final
                }

                String tipo = conn.getContentType();
                long tamano = conn.getContentLengthLong();

                System.out.println("   Tipo   : " + tipo);
                System.out.println("   Tamaño : " + tamano + " bytes");
                System.out.println("   URL fin: " + urlActual);

                if (codigo != 200 || (tipo != null && tipo.toLowerCase().contains("text/html"))) {
                    throw new IOException("Descarga rechazada (HTTP " + codigo
                            + ", tipo: " + tipo + "). Si el release es recién publicado, "
                            + "espera unos minutos y vuelve a intentar.");
                }

                // ------------------------------------------------------------
                // Descargar primero a un archivo temporal
                // ------------------------------------------------------------
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

                    // Si el servidor dijo el tamaño y no coincide, la descarga quedó cortada
                    if (tamano > 0 && total != tamano) {
                        throw new IOException("Descarga incompleta: recibidos " + total
                                + " de " + tamano + " bytes.");
                    }
                }

                // Borrar instaladores de descargas anteriores (solo en la carpeta de descarga)
                File[] viejos = carpeta.listFiles((dir, name) ->
                        name.toLowerCase().endsWith(".exe") && !name.equals(nombreArchivo));
                if (viejos != null) {
                    for (File f : viejos) {
                        if (f.delete()) {
                            System.out.println("🗑 Instalador anterior eliminado: " + f.getName());
                        }
                    }
                }

                // Renombrar el temporal al nombre final
                if (destino.exists()) destino.delete();
                if (!temporal.renameTo(destino)) {
                    throw new IOException("No se pudo guardar el archivo descargado.");
                }

                // ------------------------------------------------------------
                // Modo prueba: solo confirma que la descarga funciona
                // ------------------------------------------------------------
                if (MODO_PRUEBA) {
                    System.out.println("🧪 MODO_PRUEBA: archivo descargado en " + destino.getAbsolutePath());
                    System.out.println("🧪 No se ejecuta el instalador ni se cierra la app.");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                            "Descarga correcta (modo prueba).\n" + destino.getAbsolutePath(),
                            "Prueba de actualización", JOptionPane.INFORMATION_MESSAGE));
                    return;
                }

                // Ejecutar el instalador (pide permisos de administrador y reabre la app al terminar)
                new ProcessBuilder("cmd", "/c", "start", "", destino.getAbsolutePath(),
                        "/SILENT", "/CLOSEAPPLICATIONS", "/NORESTART").start();

                // Cerrar la app actual para que el instalador pueda reemplazar los archivos
                System.exit(0);

            } catch (Exception e) {
                if (temporal != null && temporal.exists()) temporal.delete();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                        "❌ Error durante la descarga: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE));
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
                loader.cerrar();
            }
        }).start();
    }

    // CAMBIO: extrae solo la parte numérica de un tag/versión ("v1.5.8" → "1.5.8").
    // Si no hay un número de versión válido, lanza error en lugar de comparar basura.
    private static String normalizarVersion(String texto) throws IOException {
        if (texto == null) {
            throw new IOException("Versión vacía");
        }
        Matcher m = PATRON_VERSION.matcher(texto.trim());
        if (!m.find()) {
            throw new IOException("Formato de versión no válido: '" + texto + "'");
        }
        return m.group(1);
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
            if (p1 != p2) return Integer.compare(p1, p2);   // CAMBIO: evita desbordes con restas
        }
        return 0;
    }

    // Convierte "5" o "5-beta" en 5; si no hay dígitos, 0
    private static int aNumero(String parte) {
        String soloDigitos = parte.replaceAll("[^0-9]", "");
        return soloDigitos.isEmpty() ? 0 : Integer.parseInt(soloDigitos);
    }
}