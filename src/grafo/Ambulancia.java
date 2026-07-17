package grafo;

public class Ambulancia {
    private int id;
    private Vertice localizacaoAtual;
    private StatusAmbulancia status;

    // NOVOS: Atributos para animação contínua no mapa
    private double latitudeAtual;
    private double longitudeAtual;

    public Ambulancia(int id, Vertice localizacaoAtual) {
        this.id = id;
        this.localizacaoAtual = localizacaoAtual;
        this.status = StatusAmbulancia.DISPONIVEL;
        
        // Inicializa na posição exata da base
        if (localizacaoAtual != null) {
            this.latitudeAtual = localizacaoAtual.getLatitude();
            this.longitudeAtual = localizacaoAtual.getLongitude();
        }
    }

    public void setLocalizacaoAtual(Vertice localizacaoAtual) {
        this.localizacaoAtual = localizacaoAtual;
        if (localizacaoAtual != null) {
            this.latitudeAtual = localizacaoAtual.getLatitude();
            this.longitudeAtual = localizacaoAtual.getLongitude();
        }
    }

    // NOVOS GETTERS E SETTERS PARA A ANIMAÇÃO
    public double getLatitudeAtual() { return latitudeAtual; }
    public double getLongitudeAtual() { return longitudeAtual; }
    public void setPosicaoVisual(double lat, double lon) {
        this.latitudeAtual = lat;
        this.longitudeAtual = lon;
    }

    public int getId() { return id; }
    public Vertice getLocalizacaoAtual() { return localizacaoAtual; }
    public StatusAmbulancia getStatus() { return status; }
    public boolean isDisponivel() { return status == StatusAmbulancia.DISPONIVEL; }
    public boolean estaEmAtendimento() { return status == StatusAmbulancia.EM_ATENDIMENTO; }
    
    public void setStatus(StatusAmbulancia status) {
        this.status = status;
    }

    public boolean iniciarAtendimento() {
        if (!isDisponivel()) return false;
        setStatus(StatusAmbulancia.EM_ATENDIMENTO);
        return true;
    }

    public boolean finalizarAtendimento() {
        if (!estaEmAtendimento()) return false;
        setStatus(StatusAmbulancia.DISPONIVEL);
        return true;
    }
}