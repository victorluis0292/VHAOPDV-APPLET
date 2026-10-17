package Utilidades;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheImagenes {

    private static final File DIR = crearDir();
    private static final Map<String, BufferedImage> originales = new ConcurrentHashMap<>();
    private static final Map<String, ImageIcon> escaladas = new ConcurrentHashMap<>();
    private static final Map<String, Long> fallos = new ConcurrentHashMap<>();
    private static final long REINTENTO_MS = 60_000;

    private static File crearDir() {
        String base = System.getenv("APPDATA");
        File raiz = (base != null)
                ? new File(base, "VHAOPDVOnline")
                : new File(System.getProperty("user.home"), ".vhaopdv");
        File d = new File(raiz, "imgcache");
        d.mkdirs();
        return d;
    }

    /** Imagen original: memoria → disco → internet. Devuelve null si no se pudo. */
    public static BufferedImage obtener(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) return null;
        String url = urlString.trim();

        BufferedImage img = originales.get(url);
        if (img != null) return img;

        // Si falló hace poco (sin internet, URL mala), no reintentar en cada llamada
        Long ultimoFallo = fallos.get(url);
        if (ultimoFallo != null && System.currentTimeMillis() - ultimoFallo < REINTENTO_MS) return null;

        File archivo = new File(DIR, hash(url) + ".img");
        try {
            if (!archivo.exists()) descargar(url, archivo);
            img = ImageIO.read(archivo);
            if (img == null) {            // archivo corrupto
                archivo.delete();
                fallos.put(url, System.currentTimeMillis());
                return null;
            }
            originales.put(url, img);
            fallos.remove(url);
            return img;
        } catch (Exception e) {
            System.out.println("CacheImagenes: no se pudo cargar " + url + " -> " + e.getMessage());
            archivo.delete();
            fallos.put(url, System.currentTimeMillis());
            return null;
        }
    }

    /** Imagen escalada a tamano x tamano, también cacheada. null si no hay imagen. */
    public static ImageIcon icono(String urlString, int tamano) {
        if (urlString == null || urlString.trim().isEmpty()) return null;
        String clave = urlString.trim() + "#" + tamano;

        ImageIcon icono = escaladas.get(clave);
        if (icono != null) return icono;

        BufferedImage img = obtener(urlString);
        if (img == null) return null;

        icono = new ImageIcon(img.getScaledInstance(tamano, tamano, Image.SCALE_SMOOTH));
        escaladas.put(clave, icono);
        return icono;
    }

    private static void descargar(String url, File destino) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        File tmp = new File(destino.getPath() + ".tmp");
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(tmp)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        }
        Files.move(tmp.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static String hash(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-1").digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(s.hashCode());
        }
    }
}