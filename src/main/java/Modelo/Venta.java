package Modelo;

public class Venta {
    private int id;
    private int folio;
    private int idEmpresa;
    private int cliente;
    private String vendedor;
    private double total;
    private double subtotal;
    private double pagacon;
    private double cambio;
    private double comision;
    private String fecha;       // para la fecha en formato "dd/MM/yyyy"
    private String tipopago;
    private String fechaHora;   // datetime de la venta
    private int idTurno;
    private double efectivo;
private double tarjeta;
    // Dentro de la clase Venta
private String nombre_cli; // Nombre del cliente
private String nombreEmpresa; // nombre de la empresa

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFolio() { return folio; }
    public void setFolio(int folio) { this.folio = folio; }

    public int getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(int idEmpresa) { this.idEmpresa = idEmpresa; }

    public int getCliente() { return cliente; }
    public void setCliente(int cliente) { this.cliente = cliente; }

    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getPagaCon() { return pagacon; }
    public void setPagaCon(double pagacon) { this.pagacon = pagacon; }

    public double getCambio() { return cambio; }
    public void setCambio(double cambio) { this.cambio = cambio; }

    public double getComision() { return comision; }
    public void setComision(double comision) { this.comision = comision; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getTipopago() { return tipopago; }
    public void setTipopago(String tipopago) { this.tipopago = tipopago; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public int getIdTurno() { return idTurno; }
    public void setIdTurno(int idTurno) { this.idTurno = idTurno; }
    public String getNombre_cli() {
    return nombre_cli;
}

public void setNombre_cli(String nombre_cli) {
    this.nombre_cli = nombre_cli;
}
public double getEfectivo() { return efectivo; }
public void setEfectivo(double efectivo) { this.efectivo = efectivo; }

public double getTarjeta() { return tarjeta; }
public void setTarjeta(double tarjeta) { this.tarjeta = tarjeta; }


public String getNombreEmpresa() {
    return nombreEmpresa;
}

public void setNombreEmpresa(String nombreEmpresa) {
    this.nombreEmpresa = nombreEmpresa;
}

private boolean cancelada;

public boolean isCancelada() { return cancelada; }
public void setCancelada(boolean cancelada) { this.cancelada = cancelada; }
}


