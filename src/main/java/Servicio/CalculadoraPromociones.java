package Servicios;

import Modelo.Promocion;
import java.util.*;

public class CalculadoraPromociones {

    public static class Linea {
        public final int idProducto;
        public final double cantidad;
        public final double precio;
        public Linea(int idProducto, double cantidad, double precio) {
            this.idProducto = idProducto; this.cantidad = cantidad; this.precio = precio;
        }
    }

    public static class Aplicada {
        public final int idPromocion;
        public final String descripcion;
        public final int paquetes;
        public final double descuento;
        public Aplicada(int idPromocion, String descripcion, int paquetes, double descuento) {
            this.idPromocion = idPromocion; this.descripcion = descripcion;
            this.paquetes = paquetes; this.descuento = descuento;
        }
    }

    public static List<Aplicada> calcular(List<Linea> lineas, List<Promocion> promos) {
        Map<Integer, Integer> piezas = new HashMap<>();
        Map<Integer, Double> precios = new HashMap<>();
        for (Linea l : lineas) {
            if (l.cantidad <= 0) continue;
            piezas.merge(l.idProducto, (int) Math.floor(l.cantidad), Integer::sum);
            precios.put(l.idProducto, l.precio);
        }

        List<Aplicada> resultado = new ArrayList<>();
        for (Promocion p : promos) {
            if (p.getCantidadRequerida() <= 0) continue;

            List<Double> pool = new ArrayList<>();
            for (int idProd : p.getIdProductos()) {
                int n = piezas.getOrDefault(idProd, 0);
                for (int k = 0; k < n; k++) pool.add(precios.get(idProd));
            }

            int paquetes = pool.size() / p.getCantidadRequerida();
            if (paquetes == 0) continue;

            Collections.sort(pool); // si los precios difieren, entran al paquete las piezas más baratas
            int usadas = paquetes * p.getCantidadRequerida();
            double normal = 0;
            for (int k = 0; k < usadas; k++) normal += pool.get(k);

            double descuento = Math.round((normal - paquetes * p.getPrecioPaquete()) * 100) / 100.0;
            if (descuento <= 0) continue; // la promo no conviene, no se aplica

            resultado.add(new Aplicada(p.getId(), p.getDescripcion(), paquetes, descuento));
        }
        return resultado;
    }
}