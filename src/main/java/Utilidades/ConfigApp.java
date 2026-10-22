package Utilidades;

import java.io.*;
import java.util.Properties;
import javax.swing.*;

public class ConfigApp {

    // 🔹 Versión de la app: súbela a mano en cada release
    public static final String VERSION_ACTUAL = "1.5.5";

    // Carpeta de configuración en APPDATA
    private static final File CONFIG_DIR = new File(System.getenv("APPDATA"), "VHAOPDVOnline");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "config.properties");

    // Variables en memoria
    private static int idEmpresa = 0;
    private static String correo = "";

    // 🔹 Guarda los datos de login (empresa y correo)
    public static void guardarDatos(int idEmp, String correoUser) {
        idEmpresa = idEmp;
        correo = correoUser;

        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();

        Properties props = new Properties();
        props.setProperty("idEmpresa", String.valueOf(idEmpresa));
        props.setProperty("correo", correo);

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Datos de login");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error guardando datos de login: " + e.getMessage());
        }
    }

    // 🔹 Obtiene el correo guardado
    public static String getCorreo() {
        if (!correo.isEmpty()) return correo;

        if (CONFIG_FILE.exists()) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                props.load(in);
                correo = props.getProperty("correo", "");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return correo;
    }

    // 🔹 Obtiene el ID de empresa guardado
    public static int getIdEmpresa() {
        if (idEmpresa != 0) return idEmpresa;

        if (CONFIG_FILE.exists()) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                props.load(in);
                idEmpresa = Integer.parseInt(props.getProperty("idEmpresa", "0"));
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return idEmpresa;
    }

    // 🔹 Limpia solo los datos en memoria (sesión temporal)
    public static void limpiarDatosSesionTemporal() {
        idEmpresa = 0;
        correo = "";
    }

    // 🔹 Elimina completamente los datos guardados (archivo + memoria)
    public static void limpiarDatos() {
        limpiarDatosSesionTemporal();
        if (CONFIG_FILE.exists()) {
            CONFIG_FILE.delete();
        }
    }

    // 🔹 Versión de la app (constante dentro del programa)
    public static String getVersion() {
        return VERSION_ACTUAL;
    }

    // 🔹 Cambiar de empresa desde el login
    public static void cambiarDeEmpresa(JFrame frame, JTextField txtCorreo,
                                        JTextField txtIdEmpresa, JPasswordField txtPass) {
        boolean empresaDetectada = txtIdEmpresa != null && !txtIdEmpresa.getText().trim().isEmpty();

        // Limpiar datos de login
        limpiarDatosPorCambioEmpresa();

        // Limpiar campos visuales
        if (txtCorreo != null) txtCorreo.setText("");
        if (txtIdEmpresa != null) txtIdEmpresa.setText("");
        if (txtPass != null) txtPass.setText("");

        // Habilitar edición
        if (txtCorreo != null) txtCorreo.setEditable(true);
        if (txtIdEmpresa != null) txtIdEmpresa.setEditable(true);

        // Mensaje informativo
        if (empresaDetectada) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Cambio de empresa realizado.\nIngresa los nuevos datos de la empresa.",
                    "Cambio de empresa",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "No se detectó empresa en login.\nIngresa los datos de la empresa para continuar.",
                    "Empresa no detectada",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    public static void limpiarDatosPorCambioEmpresa() {
        idEmpresa = 0;
        correo = "";
        if (CONFIG_FILE.exists()) CONFIG_FILE.delete();
    }
}