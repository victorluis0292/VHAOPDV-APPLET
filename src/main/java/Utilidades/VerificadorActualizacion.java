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

    // Extrae solo el número de versión (ej. 1.5.8) de cualquier tag
    private static final Pattern PATRON_VERSION = Pattern.compile("(\\d+(?:\\.\\d+)*)");

    public static void verificar() {
        new Thread(() -> {   // 🔥 Evita congelar Swing
            System.out.println("🚀 Iniciando verificación de actualización...");

            try {
                String contenido = leerDesdeUrl(URL_ULTIMO_RELEASE);
                JSONObject json = new JSONObject(contenido);

                // La versión se toma SOLO del tag y se normaliza
                // Acepta "1.5.8", "v1.5.8", "V.1.5.8", "version 1.5.8", " 1.5.8 "
                String tagRemoto = json.getString("tag_name");
                String versionRemota = normalizarVersion(tagRemoto);
                String versionLocal = normalizarVersion(ConfigApp.getVersion());

                int cmp = compararVersiones(versionRemota, versionLocal);
                System.out.println("Tag remoto: '" + tagRemoto + "' | remota: " + versionRemota
                        + " | local: " + versionLocal + " | cmp=" + cmp);

                // La comparación se hace ANTES de buscar el .exe,
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

                // NUEVO: buscar el .exe LIGERO (el que NO lleva "conjdk" en el nombre)
                String urlDescarga = null;
                JSONArray assets = json.getJSONArray("assets");
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject a = assets.getJSONObject(i);
                    String nombre = a.getString("name").toLowerCase();
                    if (nombre.endsWith(".exe") && !nombre.contains("conjdk")) {
                        urlDescarga = a.getString("browser_download_url");
                        break;
                    }
                }
                if (urlDescarga == null) {
                    throw new IOException("El release " + versionRemota
                            + " no tiene un instalador ligero (.exe sin 'ConJDK') adjunto");
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
                // Descarga con reintentos y reanudación (Range)
                // ------------------------------------------------------------
                final int MAX_INTENTOS = 8;
                long tamano = -1;
                int intento = 0;

                while (true) {
                    // NUEVO: mensaje según el intento
                    loader.setMensaje(intento == 0
                            ? "Descargando actualización..."
                            : "Reintentando descarga (" + intento + "/" + MAX_INTENTOS + ")...");

                    long yaDescargado = temporal.exists() ? temporal.length() : 0;

                    // Seguir redirecciones (la URL firmada puede caducar, se pide de nuevo cada intento)
                    String urlActual = urlDescarga;
                    int codigo = 0;
                    for (int salto = 0; salto <= 5; salto++) {
                        System.out.println("🌐 Pidiendo [" + salto + "]: " + urlActual);

                        conn = (HttpURLConnection) new URL(urlActual).openConnection();
                        conn.setInstanceFollowRedirects(false);
                        conn.setRequestProperty("User-Agent", "VHAO-PDV-Updater");
                        conn.setRequestProperty("Accept", "application/octet-stream");
                        if (yaDescargado > 0) {
                            conn.setRequestProperty("Range", "bytes=" + yaDescargado + "-");
                        }
                        conn.setConnectTimeout(10000);
                        conn.setReadTimeout(30000);

                        codigo = conn.getResponseCode();
                        System.out.println("   HTTP   : " + codigo);

                        if (codigo == 301 || codigo == 302 || codigo == 303
                                || codigo == 307 || codigo == 308) {
                            String dest = conn.getHeaderField("Location");
                            conn.disconnect();
                            if (dest == null || dest.isEmpty()) {
                                throw new IOException("Redirección sin destino (HTTP " + codigo + ")");
                            }
                            urlActual = new URL(new URL(urlActual), dest).toString();
                            continue;
                        }
                        break;   // no es redirección: ya tenemos la respuesta final
                    }

                    // 416 = el rango pedido no es válido (archivo parcial viejo/corrupto): empezar de cero
                    if (codigo == 416) {
                        conn.disconnect();
                        temporal.delete();
                        System.out.println("⚠ Archivo parcial inválido, se reinicia la descarga.");
                        continue;
                    }

                    boolean parcial = (codigo == 206);
                    if (codigo != 200 && codigo != 206) {
                        throw new IOException("Descarga rechazada (HTTP " + codigo + "). "
                                + "Si el release es recién publicado, espera unos minutos y vuelve a intentar.");
                    }

                    String tipo = conn.getContentType();
                    if (tipo != null && tipo.toLowerCase().contains("text/html")) {
                        throw new IOException("Descarga rechazada (tipo: " + tipo + ").");
                    }

                    // Tamaño total
                    if (parcial) {
                        // Content-Range: bytes 1000-9999/10000
                        String cr = conn.getHeaderField("Content-Range");
                        if (cr != null && cr.contains("/")) {
                            tamano = Long.parseLong(cr.substring(cr.indexOf('/') + 1).trim());
                        }
                    } else {
                        tamano = conn.getContentLengthLong();
                        yaDescargado = 0;          // el servidor ignoró Range: empezar de cero
                    }

                    System.out.println("⬇ Intento " + (intento + 1) + " | HTTP " + codigo
                            + " | desde " + yaDescargado + " de " + tamano);

                    try (InputStream in = conn.getInputStream();
                         FileOutputStream out = new FileOutputStream(temporal, parcial)) {  // append si es 206
                        byte[] buffer = new byte[65536];
                        int n;
                        long total = yaDescargado;
                        long ultimoLog = total;
                        long ultimaUI = 0;                      // NUEVO
                        loader.setProgreso(total, tamano);      // NUEVO: punto de partida

                        while ((n = in.read(buffer)) != -1) {
                            out.write(buffer, 0, n);
                            total += n;

                            // NUEVO: refrescar la barra cada ~200 ms
                            long ahora = System.currentTimeMillis();
                            if (ahora - ultimaUI >= 200) {
                                ultimaUI = ahora;
                                loader.setProgreso(total, tamano);
                            }

                            if (total - ultimoLog >= 5L * 1024 * 1024) {   // log cada 5 MB
                                ultimoLog = total;
                                if (tamano > 0) {
                                    System.out.println("⬇ " + (total * 100 / tamano) + "% ("
                                            + total / 1048576 + " / " + tamano / 1048576 + " MB)");
                                } else {
                                    System.out.println("⬇ " + total / 1048576 + " MB");
                                }
                            }
                        }
                        loader.setProgreso(total, tamano);      // NUEVO: deja la barra al 100 %
                    } catch (IOException ex) {
                        System.out.println("⚠ Conexión interrumpida: " + ex.getMessage());
                    } finally {
                        conn.disconnect();
                    }

                    long enDisco = temporal.length();
                    if (tamano > 0 && enDisco == tamano) {
                        System.out.println("✅ Descarga completada (" + enDisco / 1024 + " KB)");
                        break;                                  // completa
                    }
                    if (tamano > 0 && enDisco > tamano) {
                        temporal.delete();                      // corrupto
                        throw new IOException("Archivo descargado más grande de lo esperado.");
                    }

                    intento++;
                    if (intento >= MAX_INTENTOS) {
                        throw new IOException("Descarga incompleta tras " + MAX_INTENTOS
                                + " intentos: " + enDisco + " de " + tamano + " bytes.");
                    }
                    System.out.println("🔄 Reintentando (" + intento + "/" + MAX_INTENTOS + ")...");
                    Thread.sleep(2000);
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

                // NUEVO: aviso antes de lanzar el instalador
                loader.setMensaje("Iniciando instalador...");

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

    // Extrae solo la parte numérica de un tag/versión ("v1.5.8" → "1.5.8").
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
            if (p1 != p2) return Integer.compare(p1, p2);
        }
        return 0;
    }

    // Convierte "5" o "5-beta" en 5; si no hay dígitos, 0
    private static int aNumero(String parte) {
        String soloDigitos = parte.replaceAll("[^0-9]", "");
        return soloDigitos.isEmpty() ? 0 : Integer.parseInt(soloDigitos);
    }
}