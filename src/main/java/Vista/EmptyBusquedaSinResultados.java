package Vista;

public class EmptyBusquedaSinResultados extends EstadoVacioPanel {

    // 👇 Cambia aquí la imagen y las leyendas
    private static final String IMAGEN   = "/Img/grillosindatos.png";
    private static final String TITULO   = "No se encontraron resultados en la búsqueda";
    private static final String PREFIJO_TERMINO = "Sin coincidencias para: ";

    public EmptyBusquedaSinResultados() {
        super(IMAGEN, 150, TITULO, "");
    }

    /** Muestra debajo del título lo que se buscó. Con null o vacío lo oculta. */
    public void setTermino(String termino) {
        setSubtitulo(termino == null || termino.isEmpty() ? "" : PREFIJO_TERMINO + termino);
    }
}