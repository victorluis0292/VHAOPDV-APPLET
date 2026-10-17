package Vista;

public class EmptyBusquedaInicial extends EstadoVacioPanel {

    // 👇 Cambia aquí la imagen y las leyendas
    private static final String IMAGEN    = "/Img/buscar.jpg";
    private static final String TITULO    = "Teclea algún nombre de producto o código";
    private static final String SUBTITULO = "";   // vacío = no se muestra

    public EmptyBusquedaInicial() {
        super(IMAGEN, 150, TITULO, SUBTITULO);
    }
}