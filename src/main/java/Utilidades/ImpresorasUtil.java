package Utilidades;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.util.ArrayList;
import java.util.List;

public class ImpresorasUtil {

    public static List<PrintService> listar() {
        List<PrintService> disponibles = new ArrayList<>();
        for (PrintService s : PrintServiceLookup.lookupPrintServices(null, null)) {
            String n = s.getName().toLowerCase();
            if (n.contains("pdf") || n.contains("xps") || n.contains("onenote")) continue;
            disponibles.add(s);
        }
        return disponibles;
    }
}