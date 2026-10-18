package Modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Promocion {

    private int id;
    private int idEmpresa;
    private String descripcion;
    private int cantidadRequerida = 2;
    private double precioPaquete;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activa = true;
    private List<Integer> idProductos = new ArrayList<>();
    private String nombresProductos = ""; // solo para mostrar en la tabla

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(int idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getCantidadRequerida() { return cantidadRequerida; }
    public void setCantidadRequerida(int cantidadRequerida) { this.cantidadRequerida = cantidadRequerida; }

    public double getPrecioPaquete() { return precioPaquete; }
    public void setPrecioPaquete(double precioPaquete) { this.precioPaquete = precioPaquete; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public List<Integer> getIdProductos() { return idProductos; }
    public void setIdProductos(List<Integer> idProductos) { this.idProductos = idProductos; }

    public String getNombresProductos() { return nombresProductos; }
    public void setNombresProductos(String nombresProductos) { this.nombresProductos = nombresProductos; }
}